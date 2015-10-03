package org.jetbrains.printerGenerator.componentGenerator

import org.jetbrains.prettyPrinter.core.util.string.replaceAllInsertPlace
import java.io.File

public class ComponentIsTemplSuit (
        val psiComponentClass: String
        , val subtrees : List<ComponentSubtree>
        , val specificCode : String?
        , val isList : String
)
{
    override public fun toString(): String{

        val isTemplSuitCodeTemplate: String
        if (!isList.toBoolean()) {
            isTemplSuitCodeTemplate = File("resources/generators/ComponentIsTemplSuit.txt").readText()
        } else {
            isTemplSuitCodeTemplate = File("resources/generators/ComponentIsTemplSuitList.txt").readText()
        }

        val isTemplSuitParametersList = listOf(
                Pair("@COMP_CLASS@"     , psiComponentClass             ),
                Pair("@TEMPL_SUIT@"     , specificCode ?: "return true" )
        )

        return isTemplSuitCodeTemplate.replaceAllInsertPlace(isTemplSuitParametersList)
    }
}