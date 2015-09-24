package org.jetbrains.PrinterGenerator.generators

import kotlin.test.*
import org.jetbrains.PrinterGenerator.generators.*
import org.jetbrains.generators.LanguageInfo
import java.io.File

val importList = "resources/generators/ImportList.txt"
val componentsPath = "testData/generators/input/components/"
val inputPathGlobal = "testData/generators/input/"
val outputPathGlobal = "testData/generators/output/"

fun main(args: Array<String>) {

    generateComponents(componentsPath, outputPathGlobal)
    generatePrinterFiles(inputPathGlobal, outputPathGlobal)
}

fun generateComponents(inputPath: String, outputPath: String) {
    val langInfoParser = LanguageInfo.getInstance(inputPathGlobal + "language.xml") // TODO: no hardcode
    val folder : File = File(inputPath);
    val listOfFiles : Array<File> = folder.listFiles();
    val parser: StaXParser
    try {
        parser = StaXParser(importList, langInfoParser!!.psiPackage, langInfoParser.factory)
    } catch (e: NullPointerException) {
        println("Incorrect input in language.xml")
        return
    }
    val dir = File(outputPath + "components")
    if (!dir.exists()) { dir.mkdirs() }
    val compsOutputPath = outputPath + "components/"
    for (file in listOfFiles) {
        if (file.isFile()) {
            File(compsOutputPath + file.getName().replaceAfterLast(".", "kt")).writeText(parser.readXml(file.getPath()).toString())
        }
    }
}
fun generatePrinterFiles(inputPath: String, outputPath: String) {
    val langInfoParser = LanguageInfo.getInstance()
    val parser: PrinterFilesParser = PrinterFilesParser()
    val printerFiles = parser.readXml(inputPath + "printer.xml")

    var dir = File(outputPath + "printer")
    if (!dir.exists()) { dir.mkdirs() }
    val printerOutputPath = outputPath + "printer/"
    File(printerOutputPath + langInfoParser?.language + "Printer.kt").writeText(printerFiles.toString())

    dir = File(outputPath + "templateBase")
    if (!dir.exists()) { dir.mkdirs() }
    File(outputPath + "templateBase/" + langInfoParser?.language + "PsiElementComponent.kt")
            .writeText(printerFiles?.getPsiElementComponent() ?: "")
}
