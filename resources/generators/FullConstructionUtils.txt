package com.intellij;

import org.jetbrains.likePrinter.templateBase.template.*
import com.intellij.openapi.util.TextRange
import org.jetbrains.likePrinter.util.base.*
import org.jetbrains.likePrinter.util.string.*
import org.jetbrains.likePrinter.util.box.Box

/**
 * User: anlun
 */
public trait FullConstructionUtils {
    final val FULL_CONSTRUCTION_TAG: String
        get() = "full construction"
    final val JUNK_TEXT: String
        get() = "A"

    public fun getFullConstructionInsertPlaceMap(): Map<String, SmartInsertPlace> =
            mapOf(Pair(FULL_CONSTRUCTION_TAG
                    , SmartInsertPlace(TextRange(0, JUNK_TEXT.size)
                                     , InsertPlace.STARTS_WITH_NEW_LINE
                                     , Box.getEverywhereSuitable()
                      )
                  )
            )

    public fun getFullConstructionTagPlaceToLineNumberMap(): Map<TagPlaceLine, Int> {
        val contentRelation = getContentRelation(JUNK_TEXT, getFullConstructionInsertPlaceMap())
        return contentRelation.first
    }

    final public fun getFullConstructionLineEquationMap(): Map<Int, LineEquation> {
        val contentRelation = getContentRelation(JUNK_TEXT, getFullConstructionInsertPlaceMap())
        return contentRelation.second
    }

    public fun getContentRelation(
            text: String, insertPlaceMap: Map<String, SmartInsertPlace>
    ): Pair< Map<TagPlaceLine, Int>
             , Map<Int, LineEquation>
           >
}