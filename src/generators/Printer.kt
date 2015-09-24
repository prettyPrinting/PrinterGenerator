package org.jetbrains.PrinterGenerator.generators

import org.jetbrains.generators.LanguageInfo
import java.io.File

public class Printer (
          val specificImport    : String?
        , val fileClassName     : String?
        , val filePsiClass      : String?
        , val defaultFromText   : String?
        , val components        : List<ComponentData>
)
{
    //fun getFullConstructionUtils() = File("resources/generators/FullConstructionUtils.txt").readText()
    //fun getFormatListFillUtils() = File("resources/generators/FormatListFillUtils.txt").readText()
    //fun getFileTemplate() = File("resources/generators/template.txt").readText()

    fun getPsiElementComponent(): String {
        val PsiElementComponentTemplate = File("resources/generators/PsiElementComponent.txt").readText()

        val langInfo = LanguageInfo.getInstance()

        val parametersList = listOf(
            Pair("@LANG@", langInfo?.language ?: ""),
            Pair("@LANG_PACKAGE@", langInfo?.langPackage ?: "")
        )

        return PsiElementComponentTemplate.replaceAllInsertPlace(parametersList)
    }

    override public fun toString(): String {

        val componentCodeTemplate = File("resources/generators/Printer.txt").readText()

        val compDecl = {
            acc: String, component: ComponentData ->
            acc +
                    "public val ${component.name}: ${component.name?.capitalize()} = ${component.name?.capitalize()}(this)\n"
        }

        val applyTempl = {
            acc: String, component: ComponentData ->
            acc + "is ${component.psiClass} -> applyTmplt(p)\n"
        }

        val getVariants = {
            acc: String, component: ComponentData ->
            acc + "is ${component.psiClass} -> ${component.name}.getVariants(p, context)\n"
        }

        val getSaveVariants = {
            acc: String, component: ComponentData ->
            acc + "is ${component.psiClass} -> ${component.name}.getAndSaveTemplate(p)\n"
        }

        val fromText = {
            acc: String, component: ComponentData ->
            acc + "is ${component.psiClass} -> return factory.${component.fromText}(text, null)\n"
        }

        val getTmplt = {
            acc: String, component: ComponentData ->
                acc + "is ${component.psiClass} -> ${component.name}.getTmplt(p)\n"
        }

        val countTemplates = {
            acc: String, component: ComponentData ->
                acc + "${component.name}.getTemplates().size() +\n"
        }

        val factoryCreate = {
            acc: String, component: ComponentData ->
                acc + "is ${component.psiClass} -> factory.create" +
                        "${component.psiClass?.removePrefix("Psi")}FromText(text)\n"
        }

        val langInfo = LanguageInfo.getInstance()

        val parametersList = listOf(
                  Pair("@LANG@"                     , langInfo?.language ?: "")
                , Pair("@LANG_PACKAGE@"             , langInfo?.langPackage ?: "")
                , Pair("@COMP_PACKAGE@"             , langInfo?.psiPackage ?: "")
                , Pair("@SPECIFIC_IMPORT@"          , specificImport ?: "")
                , Pair("@FILE_CLASS@"               , filePsiClass ?: "")
                , Pair("@COMP_DECLARATIONS@"        , components.fold("", compDecl))
                , Pair("@FILE_COMP_PASC@"           , fileClassName ?: "")
                , Pair("@FILE_COMP@"                , fileClassName?.capitalize() ?: "")
                , Pair("@APPLY_TEMPLATE@"           , components.fold("", applyTempl))
                , Pair("@GET_VARIANTS@"             , components.fold("", getVariants))
                , Pair("@GET_SAVE_TEMPLATE@"        , components.fold("", getSaveVariants))
                , Pair("@FACTORY@"                  , langInfo?.factory ?: "")
                , Pair("@FACTORY_PACKAGE@"          , langInfo?.factoryPackage ?: "")
                , Pair("@CREATE_FROM_TEXT@"         , components.fold("", fromText))
                , Pair("@DEFAULT_FROM_TEXT@"        , defaultFromText ?: "")
                , Pair("@COUNT_TEMPLATES@"          , components.fold("", countTemplates) + " 0")
                , Pair("@GET_TMPLT@"                , components.fold("", getTmplt))
                , Pair("@FACTORY_CREATE@"           , components.fold("", factoryCreate))
        )

        return componentCodeTemplate.replaceAllInsertPlace(parametersList)
    }

}