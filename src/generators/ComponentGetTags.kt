package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentGetTags (
        val psiComponentClass: String
        , val subtrees : List<ComponentSubtree>
        , val specificCode : String?
)
{
    override public fun toString(): String{

        val getTagsCodeTemplate = File("resources/generators/ComponentGetTags.txt").readText()

        val getTags = {
            acc: String, subtree: ComponentSubtree ->
            when {
                !subtree.isCodeBlock && !subtree.hasSeveralElem   ->
                    acc + "if (p.get${subtree.psiSubtreeGet}() != null) { set.add(${subtree
                        .name.toUpperCase()}_TAG) }\n"
                !subtree.isCodeBlock && subtree.hasSeveralElem    ->
                    acc + "if (p.get${subtree.psiSubtreeGet}() != null && !p.get${subtree.psiSubtreeGet}().isEmpty())" +
                            " { set.add(${subtree.name.toUpperCase()}_TAG) }\n"
                else    ->  acc + "addPossibleCodeBlockTag(set, p.get${subtree.psiSubtreeGet}(), ${subtree
                        .name.toUpperCase()}_TAG)\n"

            }
        }

        val getTagsParametersList = listOf(
                Pair("@COMP_CLASS@"     , psiComponentClass                         ),
                Pair("@GET_TAGS@"       , subtrees.fold("", getTags)),
                Pair("@SPECIFIC_CODE@"  , specificCode ?: "")
        )

        return getTagsCodeTemplate.replaceAllInsertPlace(getTagsParametersList)
    }
}