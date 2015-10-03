package org.jetbrains.printerGenerator.componentGenerator

import org.jetbrains.prettyPrinter.core.util.string.replaceAllInsertPlace
import java.io.File

public class ComponentGetList (
        public val compClass    : String
        , val psiSubtreeGet     : String
)
{
    override public fun toString(): String{

        val getListText = File("resources/generators/ComponentGetList.txt").readText()

        val addParameterList = listOf(
                Pair("@SUBTREE_GET@"    , psiSubtreeGet     ),
                Pair("@COMP_CLASS@"     , compClass         )
        )

        return getListText.replaceAllInsertPlace(addParameterList)
    }
}