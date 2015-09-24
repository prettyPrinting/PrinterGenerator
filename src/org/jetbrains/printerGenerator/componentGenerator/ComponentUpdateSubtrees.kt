package org.jetbrains.printerGenerator.componentGenerator

import org.jetbrains.prettyPrinter.core.util.string.replaceAllInsertPlace
import java.io.File

public class ComponentUpdateSubtrees (
        val psiComponentClass: String
        , val subtrees : List<ComponentSubtree>
        , val specificCode : String?
)
{
    override public fun toString(): String{

        val updateSubtreesCodeTemplate = File("resources/generators/ComponentUpdateSubtrees.txt").readText()

        val updateSubtreesParametersList = listOf(
                Pair("@COMP_CLASS@"         , psiComponentClass                ),
                Pair("@UPDATE_SUBTREES@"    , specificCode ?: "return variants")
        )

        return updateSubtreesCodeTemplate.replaceAllInsertPlace(updateSubtreesParametersList)
    }
}