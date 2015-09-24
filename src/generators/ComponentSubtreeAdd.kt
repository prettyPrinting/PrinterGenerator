package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentSubtreeAdd (
        public val name         : String
        , val psiSubtreeGet     : String
        , val isEverywhereSuit  : Boolean
        , val specificCode      : String?
)
{
    override public fun toString(): String{

        val addCodeTemplate = File("resources/generators/ComponentSubtreeAdd.txt").readText()

        val everywhereSuit =
                if (isEverywhereSuit) { "Box.getEverywhereSuitable())\n" }
                else                  { "$name!!.toBox())\n" }

        val addParameterList = listOf(
                Pair("@SUBTREE_GET@"    , psiSubtreeGet     ),
                Pair("@NAME@"           , name              ),
                Pair("@NAME_CAP@"       , name.toUpperCase()),
                Pair("@EVERYWHERE_SUIT@", everywhereSuit    )
        )

        return specificCode ?: addCodeTemplate.replaceAllInsertPlace(addParameterList)
    }
}