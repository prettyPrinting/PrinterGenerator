package com.intellij;

import com.intellij.openapi.util.TextRange
import org.jetbrains.likePrinter.util.base.*
import com.intellij.psi.PsiElement
import org.jetbrains.likePrinter.util.psiElement.deleteSpaces
import org.jetbrains.format.Format
import org.jetbrains.likePrinter.printer.Printer
import java.util.ArrayList
import org.jetbrains.likePrinter.util.psiElement.toSmartInsertPlace
import com.intellij.psi.PsiBlockStatement
import org.jetbrains.likePrinter.util.psiElement.getVariants
import java.util.HashMap
import org.jetbrains.likePrinter.printer.CommentConnectionUtils.VariantConstructionContext
import java.util.Comparator
import org.jetbrains.likePrinter.util.string.*
import java.util.HashSet
import org.jetbrains.likePrinter.util.box.Box
import org.jetbrains.format.FormatSet

/**
 * User: anlun
 */

open class SmartInsertPlace(
  val range: TextRange

/**
 * If object need to be insert by Beside format concatenation, fillConstant must be -1.
 * If fillConstant is non-negative, it means the fill constant of block.
 */
, val fillConstant : Int
, val boxToSuit    : Box
) {
    public fun toInsertPlace(): InsertPlace = InsertPlace(range, fillConstant)

    open fun shiftRight(delta: Int) = SmartInsertPlace(range.shiftRight(delta), fillConstant, boxToSuit)

    protected fun boxToString(): String {
        if (boxToSuit.height == 0) { return "EmptyB"   }
        if (boxToSuit.height == 1) { return "OneLineB" }
        return "MultiB" // Also in case of everywhere suitable box
    }

    override public fun toString(): String = "#$fillConstant ${boxToString()}#"
}

open public class Template<T: SmartInsertPlace>(
  val text                   : String
, val insertPlaceMap         : Map<String, T>
, val tagPlaceToLineNumberMap: Map<TagPlaceLine, Int>
, val lineEquationMap        : Map<Int, LineEquation>
) {
    override public fun toString(): String {
        val replacementSet = insertPlaceMap.entrySet()
                                .map { e -> Pair(e.value.range, e.key + e.value.toString()) }
        val replacementList = replacementSet.toList()
        return text.replaceMultiple(replacementList)
    }
}

open public class PsiTemplateGen<T: PsiElement, SIP: SmartInsertPlace>(
  val psi                : T
, insertPlaceMap         : Map<String, SIP>
, tagPlaceToLineNumberMap: Map<TagPlaceLine, Int>
, lineEquationMap        : Map<Int, LineEquation>
): Template<SIP>(psi.getText() ?: "", insertPlaceMap, tagPlaceToLineNumberMap, lineEquationMap)

open public class PsiTemplate<T: SmartInsertPlace>(
  psi                    : PsiElement
, insertPlaceMap         : Map<String, T>
, tagPlaceToLineNumberMap: Map<TagPlaceLine, Int>
, lineEquationMap        : Map<Int, LineEquation>
): PsiTemplateGen<PsiElement, T>(psi, insertPlaceMap, tagPlaceToLineNumberMap, lineEquationMap)

open class ListInsertTemplate(
  val text           : String
, val insertPlaceList: List<InsertPlace>
)

fun insertPlaceListTemplateToFormatList(
         width: Int
,        tmplt: ListInsertTemplate
, elemVariants: List<FormatSet>
): FormatSet {
    val insertPlaceList = tmplt.insertPlaceList
    if (insertPlaceList.size != elemVariants.size) { return FormatSet.empty(width) }

    val fmtListsWithRanges = ArrayList<Pair<InsertPlace, FormatSet>>()
    for (i in 0..insertPlaceList.lastIndex) {
        fmtListsWithRanges.add(Pair(insertPlaceList[i], elemVariants[i]))
    }

    return insertToText(width, tmplt.text, fmtListsWithRanges)
}