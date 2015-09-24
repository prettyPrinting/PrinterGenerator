package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentPrepareSubtrees (
        val psiComponentClass: String
        , val subtrees : List<ComponentSubtree>
        , val specificCode : String?
)
{
    override public fun toString(): String{

        val prepSubtreesCodeTemplate = File("resources/generators/ComponentPrepareSubtrees.txt").readText()

        val prepSubtrees = {
            acc: String, subtree: ComponentSubtree ->
            when(subtree.isCodeBlock) {
                false   ->  acc + "prepare${subtree.name.capitalize()}Variants(p, variants, context)\n"
                else    ->  acc + "preparePossibleCodeBlockPart(p.get${subtree
                        .psiSubtreeGet}(), ${subtree.name.toUpperCase()}_TAG, variants, context)\n"
            }
        }

        val prepSubtreesParametersList = listOf(
                Pair("@COMP_CLASS@"     , psiComponentClass                              ),
                Pair("@PREP_SUBTREES@"  , subtrees.fold("", prepSubtrees)),
                Pair("@SPECIFIC_CODE@"  , specificCode ?: "")
        )

        return prepSubtreesCodeTemplate.replaceAllInsertPlace(prepSubtreesParametersList)
    }
}