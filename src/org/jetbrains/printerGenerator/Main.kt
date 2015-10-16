package org.jetbrains.printerGenerator

import java.io.File

fun main(args: Array<String>) {
    val xmlDir = File("../printerXMLs/")
    val inputPath: String
    val outputPath: String
    if (xmlDir.exists()) {
        inputPath = "../printerXMLs/"
        outputPath = "../gen/"
        for (dir in xmlDir.listFiles()) {
            val dirName = dir.getName()
            val newInputPath = inputPath + dirName + "/"
            val newOutputPath = outputPath + dirName + "/"
            File(newOutputPath).exists() || File(newOutputPath).mkdirs()
            LanguageInfo.getInstance(newInputPath + "language.xml")
            generateComponents(newInputPath + "components/", newOutputPath)
            generatePrinterFiles(newInputPath, newOutputPath)
            LanguageInfo.clean()
        }
    } else {
        inputPath = "testData/generators/input/"
        outputPath = "testData/generators/output/"
        val componentsPath = "testData/generators/input/components/"
        generateComponents(componentsPath, outputPath)
        generatePrinterFiles(inputPath, outputPath)
    }

}

fun generateComponents(inputPath: String, outputPath: String) {
    val langInfoParser = LanguageInfo.getInstance()
    val folder : File = File(inputPath);
    val listOfFiles : Array<File> = folder.listFiles();
    val parser: StaXParser
    try {
        parser = StaXParser(langInfoParser!!.factory)
    } catch (e: NullPointerException) {
        println("Incorrect input in language.xml")
        return
    }
    val dir = File(outputPath + "components")
    if (!dir.exists()) { dir.mkdirs() }
    val compsOutputPath = outputPath + "components/"
    for (file in listOfFiles) {
        if (file.isFile()) {
            if (file.getName() != "langFile.xml") {
                File(compsOutputPath + file.getName().replaceAfterLast(".", "kt")).writeText(parser.readXml(file.getPath()).toString())
            } else {
                File(compsOutputPath + langInfoParser?.language + "File.kt").writeText(parser.readXml(file.getPath()).toString())
            }
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
