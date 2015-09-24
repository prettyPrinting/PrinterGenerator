package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentGetTemplate (
        val psiComponentClass: String
        , val subtrees : List<ComponentSubtree>
        , val specificCode : String?
)
{
    override public fun toString(): String{

        val getTemplateCodeTemplate = File("resources/generators/ComponentGetTemplate.txt").readText()


        val subtreesToInsPlace = {
            acc: String, subtree: ComponentSubtree ->
            when (Pair(subtree.isCodeBlock, subtree.isRequired)){
                Pair(false, false) ->
                    acc + "add${subtree.name.capitalize()}ToInsertPlaceMap(newP, insertPlaceMap, negShift)\n"
                Pair(false, true)  ->
                    acc + "if (!add${subtree.name
                            .capitalize()}ToInsertPlaceMap(newP, insertPlaceMap, negShift)) { return null }\n"
                Pair(true, false)  ->
                    acc + "addCBtoInsertPlaceMap(newP.get${subtree
                            .psiSubtreeGet}(), ${subtree.name.toUpperCase()}_TAG, insertPlaceMap, text)\n"
                else                                        ->
                    acc + "if (!addCBtoInsertPlaceMap(newP.get${subtree.psiSubtreeGet}(), ${subtree
                            .name.toUpperCase()}_TAG, insertPlaceMap, text)) { return null }\n"
            }
        }

        val getTemplateParametersList = listOf(
                Pair("@COMP_CLASS@"     , psiComponentClass                     ),
                Pair("@ADD_SUBTREES@"   , subtrees.fold("", subtreesToInsPlace) ),
                Pair("@SPECIFIC_CODE@"  , specificCode ?: "")
        )

        return getTemplateCodeTemplate.replaceAllInsertPlace(getTemplateParametersList)
    }
}