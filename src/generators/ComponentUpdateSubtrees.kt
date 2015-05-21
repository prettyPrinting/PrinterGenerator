package org.jetbrains.PrinterGenerator.generators

import java.io.File

/**
 * Created by Aleksei on 3/19/2015.
 */
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