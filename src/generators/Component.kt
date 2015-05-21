package org.jetbrains.PrinterGenerator.generators

import java.io.File
import org.jetbrains.format.*

/**
 * Created by Aleksei on 3/5/2015.
 */
public class Component (
          val name                : String
        , val compPackage       : String
        , val psiComponentClass : String
        , val predecessors      : String?
        , val specificImport    : String?
        , val subtrees          : List<ComponentSubtree>
        , val getNewElement     : String
        , val updateSubtrees    : String
        , val prepareSubtrees   : String
        , val getTags           : String
        , val isTemplSuit       : String
        , val getTemplate       : String
        , val specificCode      : String?
)
{
    override public fun toString(): String {

        val componentCodeTemplate = File("resources/generators/Component.txt").readText()

        val declTags = {
            (acc: String, subtree: ComponentSubtree) ->
            acc + "final val ${subtree.name.toUpperCase()}_TAG: String\n    get() = \"${subtree.name.toLowerCase()}\"\n"
        }

        val genSubtrees = {
            (acc: String, subtree: ComponentSubtree) ->
            when (subtree.isCodeBlock) {
                false    -> acc + subtree.toString()
                else     -> acc
            }
        }

        val importList = File("resources/generators/ImportList.txt").readText()

        val parametersList = listOf(
                Pair("@NAME_CC@"            , name.capitalize()             ),
                Pair("@IMPORT_LIST@"        , importList                    ),
                Pair("@COMP_PACKAGE@"       , compPackage                   ),
                Pair("@COMP_CLASS@"         , psiComponentClass             ),
                Pair("@PREDECESSORS@"       , predecessors ?: ""            ),
                Pair("@SPECIFIC_IMPORT@"     , specificImport ?: ""         ),
                Pair("@DECL_TAGS@"          , subtrees.fold("", declTags)   ),
                Pair("@GEN_SUBTREES@"       , subtrees.fold("", genSubtrees)),
                Pair("@GET_NEW_ELEM@"       , getNewElement                 ),
                Pair("@UPDATE_SUBTREES@"    , updateSubtrees                ),
                Pair("@PREPARE_SUBTREES@"   , prepareSubtrees               ),
                Pair("@GET_TAGS@"           , getTags                       ),
                Pair("@IS_TEMPL_SUIT@"      , isTemplSuit                   ),
                Pair("@GET_TEMPLATE@"       , getTemplate                   ),
                Pair("@SPECIFIC_CODE@"      , specificCode ?: ""            )
        )
        return componentCodeTemplate.replaceAllInsertPlace(parametersList)
    }
}