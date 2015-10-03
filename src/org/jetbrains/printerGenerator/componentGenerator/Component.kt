package org.jetbrains.printerGenerator.componentGenerator
import org.jetbrains.prettyPrinter.core.util.string.replaceAllInsertPlace
import java.io.File
import org.jetbrains.format.*
import org.jetbrains.printerGenerator.LanguageInfo

public class Component (
          val name              : String
        , val psiComponentClass : String
        , val isList            : String
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
        val componentCodeTemplate: String
        if (isList.toBoolean()) {
            componentCodeTemplate = File("resources/generators/ListComponent.txt").readText()
        } else {
            componentCodeTemplate = File("resources/generators/Component.txt").readText()
        }

        val declTags = {
            acc: String, subtree: ComponentSubtree ->
            acc + "final val ${subtree.name.toUpperCase()}_TAG: String\n    get() = \"${subtree.name.toLowerCase()}\"\n"
        }

        val genSubtrees = {
            acc: String, subtree: ComponentSubtree ->
            when (subtree.isCodeBlock) {
                false    -> acc + subtree.toString()
                else     -> acc
            }
        }

        val getList =
                if (isList.toBoolean())
                    ComponentGetList(psiComponentClass, subtrees.get(0).psiSubtreeGet).toString()
                else
                    ""

        val importList = File("resources/generators/ImportList.txt").readText()

        val langInfo = LanguageInfo.getInstance()

        val parametersList = listOf(
                Pair("@IMPORT_LIST@"        , importList                    ),
                Pair("@FACTORY@"            , langInfo?.factory ?: ""       ),
                Pair("@FACTORY_PACKAGE@"    , langInfo?.factoryPackage ?: ""),
                Pair("@LANG@"               , langInfo?.language ?: ""      ),
                Pair("@LANG_PACKAGE@"       , langInfo?.langPackage ?: ""   ),
                Pair("@COMP_PACKAGE@"       , langInfo?.psiPackage ?: ""    ),
                Pair("@NAME_CC@"            , name.capitalize()             ),
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
                Pair("@SPECIFIC_CODE@"      , specificCode ?: ""            ),
                Pair("@GET_LIST@"           , getList                       )
        )
        return componentCodeTemplate.replaceAllInsertPlace(parametersList)
    }
}