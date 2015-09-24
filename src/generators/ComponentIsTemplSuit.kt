package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentIsTemplSuit (
        val psiComponentClass: String
        , val subtrees : List<ComponentSubtree>
        , val specificCode : String?
)
{
    override public fun toString(): String{

        val isTemplSuitCodeTemplate = File("resources/generators/ComponentIsTemplSuit.txt").readText()

        val isTemplSuitParametersList = listOf(
                Pair("@COMP_CLASS@"     , psiComponentClass             ),
                Pair("@TEMPL_SUIT@"     , specificCode ?: "return true" )
        )

        return isTemplSuitCodeTemplate.replaceAllInsertPlace(isTemplSuitParametersList)
    }
}