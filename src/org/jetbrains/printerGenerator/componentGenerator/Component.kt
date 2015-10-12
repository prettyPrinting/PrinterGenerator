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
        , val isFile            : String
)
{
    override public fun toString(): String {
        val componentCodeTemplate: String
        val parametersList: List<Pair<String, String>>

        if (isFile.toBoolean()) {
            componentCodeTemplate = File("resources/generators/FileComponent.txt").readText()

            val getSubtrees = {
                acc: String, subtree: ComponentSubtree ->
                acc + getFileSubtree(subtree)
            }

            val fileSubtreesVariants = {
                acc: String, subtree: ComponentSubtree ->
                acc + getFileSubtreesVariants(subtree)
            }

            val langInfo = LanguageInfo.getInstance()

            parametersList = listOf(
                    Pair("@LANG@"                   , langInfo?.language ?: ""      ),
                    Pair("@LANG_PACKAGE@"           , langInfo?.langPackage ?: ""   ),
                    Pair("@COMP_PACKAGE@"           , langInfo?.langPackage ?: ""    ),
                    Pair("@NAME_CC@"                , name.capitalize()             ),
                    Pair("@COMP_CLASS@"             , psiComponentClass             ),
                    Pair("@GET_SUBTREES@"           , subtrees.fold("", getSubtrees)),
                    Pair("@GET_SUBTREES_VARIANTS@"  , subtrees.fold("", fileSubtreesVariants))
            )

            return componentCodeTemplate.replaceAllInsertPlace(parametersList)
        } else if (isList.toBoolean()) {
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

        parametersList = listOf(
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

    private fun getFileSubtreesVariants(subtree: ComponentSubtree): String {
        val fileSubtreeVariantsTemplate = File("resources/generators/FileSubtreeVariants.txt").readText()

        val parametersList = listOf(
                Pair("@NAME@"               , subtree.name),
                Pair("@NAME_CC@"            , subtree.name.capitalize())
        )
        return fileSubtreeVariantsTemplate.replaceAllInsertPlace(parametersList)
    }

    private fun getFileSubtree(subtree: ComponentSubtree): String {
        val template = File("resources/generators/SubtreeGetVariants.txt").readText()

        val parametersList = listOf(
                Pair("@NAME_CC@"        , subtree.name.capitalize()),
                Pair("@COMP_CLASS@"     , subtree.psiComponentClass),
                Pair("@GET_SUBTREE@"    , subtree.getCode)
        )

        return template.replaceAllInsertPlace(parametersList)

    }
}