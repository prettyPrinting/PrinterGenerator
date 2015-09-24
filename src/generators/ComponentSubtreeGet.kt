package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentSubtreeGet (
        public val name             : String
        , val psiSubtreeGet         : String
        , val hasSeveralElements    : Boolean
        , val foldFunction          : String?
        , val specificCode          : String?
)
{
    override public fun toString(): String{

        val getCodeTemplate =
                if (hasSeveralElements) { File("resources/generators/ComponentSubtreeGetList.txt").readText() }
                else                    { File("resources/generators/ComponentSubtreeGet.txt").readText() }

        val getParameterList = listOf(
                Pair("@SUBTREE_GET@"    , psiSubtreeGet     ),
                Pair("@NAME@"           , name              ),
                Pair("@FOLD_FUNCTION@"  , foldFunction ?: "")
        )

        return specificCode ?: getCodeTemplate.replaceAllInsertPlace(getParameterList)
    }
}