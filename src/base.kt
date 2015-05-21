package org.jetbrains.PrinterGenerator.generators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.format.Format
import org.jetbrains.format.toFormat
import org.jetbrains.format.FormatSet
import org.jetbrains.format.FormatMap2D_FL
import org.jetbrains.format.FormatSet.FormatSetType
import org.jetbrains.format.FormatMap3D
import org.jetbrains.format.FormatMap3D_AF
import org.jetbrains.format.SteppedFormatMap
import org.jetbrains.format.FormatMap2D_LL
import org.jetbrains.format.FormatMap1D
import java.util.ArrayList

/**
 * User: anlun
 */

class IncorrectPsiElementException(str: String): Exception(str) {}

open class InsertPlace(
  val range: TextRange
, val fillConstant: Int
) {
    companion object {
        val STARTS_WITH_NEW_LINE      : Int = -1
        val DOESNT_START_WITH_NEW_LINE: Int =  0
    }

    fun shiftRight(delta: Int): InsertPlace =
            InsertPlace(range.shiftRight(delta), fillConstant)
}

fun insertFormatsToText(text: String, fmtsWithRanges: List<Pair<InsertPlace, Format>>): Format {
    val sortedRangeList = fmtsWithRanges.sortBy { p -> p.first.range.getStartOffset() }

    var curPos = 0
    var curFmt = Format.empty
    sortedRangeList.forEach { p ->
        val range = p.first.range
        val start = range.getStartOffset()
        val end   = range.getEndOffset()
        val fmt   = p.second

        if (curPos < start) { curFmt = curFmt + text.substring(curPos, start).toFormat() }
        val fillConstant = p.first.fillConstant
        if (fillConstant < 0) {
            curFmt = curFmt / fmt
        } else {
            curFmt = curFmt.addFillStyle(fmt, fillConstant)
        }
        curPos = end
    }
    val len = text.length
    if (curPos < len) { curFmt = curFmt + text.substring(curPos, len).toFormat() }
    return curFmt
}

fun insertToText(
        width: Int, text: String, fmtListsWithRanges: List<Pair<InsertPlace, FormatSet>>
): FormatSet {
    if (!FormatSet.usingNewInsertToText) { insertToText_old(width, text, fmtListsWithRanges) }
    return insertToText_new(width, text, fmtListsWithRanges)
}

private fun findNewLine(list: List<TextRange>, text: String): Int? {
    var firstNewLinePosition: Int? = null
    for (r in list) {
        val rText = text.substring(r.getStartOffset(), r.getEndOffset())
        val newLineSymbolPosition = rText.indexOf("\n")
        if (newLineSymbolPosition != -1) {
            firstNewLinePosition = newLineSymbolPosition + r.getStartOffset()
            break
        }
    }
    return firstNewLinePosition
}

private fun divideBy(r: TextRange, l: List<TextRange>): ArrayList<TextRange> {
    var curPos = r.getStartOffset()
    val endPos = r.getEndOffset()
    val result = ArrayList<TextRange>(l.size() + 1)

    for (p in l) {
        val pStart = p.getStartOffset()
        if (pStart > endPos) { return result }
        result.add(TextRange(curPos, pStart))
        curPos = p.getEndOffset()
    }
    if (curPos < endPos) { result.add(TextRange(curPos, endPos)) }
    return result
}


private fun insertToText_old(
        width: Int, text: String, fmtListsWithRanges: List<Pair<InsertPlace, FormatSet>>
): FormatSet = insertToTextInRange(text, fmtListsWithRanges, 0, text.length, FormatSet.initial(width))

public fun insertToText_new(
        width: Int, text: String, fmtListsWithRanges: List<Pair<InsertPlace, FormatSet>>
): FormatSet {
    val sortedFmtListsWithRanges = fmtListsWithRanges sortBy { p -> p.first.range.getStartOffset() }
    val insertPlaceRanges =  sortedFmtListsWithRanges map    { p -> p.first.range }

    val textRanges = divideBy(TextRange(0, text.length), insertPlaceRanges)
    val firstNLPos = findNewLine(textRanges          , text)
    val  lastNLPos = findNewLine(textRanges.reverse(), text)
    if (firstNLPos == null || lastNLPos == null) {
        return insertToTextInRange(text, sortedFmtListsWithRanges, 0, text.length, FormatSet.initial(width))
    }

    val beginFormatSet = insertToTextInRange(text, sortedFmtListsWithRanges, 0, firstNLPos, FormatMap2D_FL(width))
    val fs: FormatSet = when (FormatSet.defaultFormatSetType) {
        is FormatSetType.D3   -> FormatMap3D   (width)
        is FormatSetType.D3AF -> FormatMap3D_AF(width)
        is FormatSetType.SteppedD3AF -> SteppedFormatMap(FormatSet.stepInMap, width)
        else -> FormatMap3D(width)
    }
    fs.addAll(beginFormatSet)

    val nextAfterFirstNL = firstNLPos + 1
    if (firstNLPos >= lastNLPos) {
        val endFormatSet = insertToTextInRange(
                text, sortedFmtListsWithRanges, nextAfterFirstNL, text.length, FormatMap2D_LL(width))
        return fs - endFormatSet
    }

    val nextAfterLastNL = lastNLPos + 1
    val middleFormatSet = insertToTextInRange(
            text, sortedFmtListsWithRanges, nextAfterFirstNL,   lastNLPos, FormatMap1D   (width))
    val    endFormatSet = insertToTextInRange(
            text, sortedFmtListsWithRanges,  nextAfterLastNL, text.length, FormatMap2D_LL(width))
    return fs - middleFormatSet - endFormatSet
}

private fun insertToTextInRange(
        text: String, fmtListsWithRanges: List<Pair<InsertPlace, FormatSet>>
        , startPos: Int, endPos: Int, startSet: FormatSet
): FormatSet {
    val sortedRangeList = fmtListsWithRanges
            .filter { p -> val rangeStart = p.first.range.getStartOffset()
                rangeStart >= startPos && rangeStart < endPos }
            .sortBy { p -> p.first.range.getStartOffset() }

    var curPos = startPos
    var curFmtList = startSet; if (curFmtList.isEmpty()) { curFmtList.add(Format.empty) }

    for (p in sortedRangeList) {
        val range   = p.first.range
        val start   = range.getStartOffset()
        if (curPos < start) { curFmtList = curFmtList + text.substring(curPos, start).toFormat() }

        val fmtList      = p.second
        val fillConstant = p.first.fillConstant
        curFmtList = if (fillConstant < 0) curFmtList.addBeside   (fmtList)
        else                  curFmtList.addFillStyle(fmtList, fillConstant)
        curPos = range.getEndOffset()
    }
    if (curPos < endPos) { curFmtList = curFmtList + text.substring(curPos, endPos).toFormat() }

    return curFmtList
}
