package org.jetbrains.PrinterGenerator.generators
/**
 * Created by Aleksei on 5/21/2015.
 */
import kotlin.test.*
import org.jetbrains.PrinterGenerator.generators.*
import java.io.File

val importList = "testData/generators/input/import.txt"

fun main(args: Array<String>) {
    generateComponents("testData/generators/input/TestFolder/", "testData/generators/output/")
}

fun generateComponents(inputPath: String, outputPath: String) {
    val folder : File = File(inputPath);
    val listOfFiles : Array<File> = folder.listFiles();
    val parser: StaXParser = StaXParser(importList, "com.intellij.psi", "PsiElementFactory")
    for (file in listOfFiles) {
        if (file.isFile()) {
            File(outputPath + file.getName().replaceAfterLast(".", "kt")).writeText(parser.readXml(file.getPath()).toString())
        }
    }
}
fun runGenerator() {
    val path = "testData/generators/input/if.xml"
    val parser: StaXParser = StaXParser(importList, "com.intellij.psi", "PsiElementFactory")
    File("testData/generators/output/out.kt").writeText(parser.readXml(path).toString())
    assertEquals("", "", "Generator doesn't work")
}

fun runPrinterGenerator() {
    val path = "testData/generators/input/printer.xml"
    val parser: PrinterFilesParser = PrinterFilesParser()
    File("testData/generators/output/outPrinter.kt").writeText(parser.readXml(path).toString())
    assertEquals("", "", "Generator doesn't work")
}

fun generateStatement_noSubtrees() {
    val path = "testData/generators/input/noSubtrees.xml"
    val parser: StaXParser = StaXParser(importList, "com.intellij.psi", "PsiElementFactory")
    val text = parser.readXml(path).toString()
    assertTrue(!text.contains("_TAG"), "Statement without subtrees wasn't generated correctly")
}

fun generateStatement_Subtrees_noCodeBlocks() {
    val path = "testData/generators/input/subtree_NoCB.xml"
    val parser: StaXParser = StaXParser(importList, "com.intellij.psi", "PsiElementFactory")
    val text = parser.readXml(path).toString()
    assertTrue(text.contains("_TAG") && !(text.contains("CodeBlock") || text.contains("CBtoInsertPlaceMap"))
            , "Statement with subtrees and without CB wasn't generated correctly")
}

fun generateStatement_Subtrees_CodeBlocks() {
    val path = "testData/generators/input/subtree_CB.xml"
    val parser: StaXParser = StaXParser(importList, "com.intellij.psi", "PsiElementFactory")
    val text = parser.readXml(path).toString()
    assertTrue(text.contains("_TAG") && text.contains("CodeBlock") && text.contains("CBtoInsertPlaceMap")
            , "Statement with subtrees and CB wasn't generated correctly")
}

fun generateStatement_Subtrees_HasSeveralElem() {
    val path = "testData/generators/input/subtreeList.xml"
    val parser: StaXParser = StaXParser(importList, "com.intellij.psi", "PsiElementFactory")
    val text = parser.readXml(path).toString()
    assertTrue(text.contains("_TAG") && text.contains("map") && text.contains("fold")
            , "Statement with subtrees which consists of several elements wasn't generated correctly")
}

fun generateStatement_Subtrees_Hardcode() {
    val path = "testData/generators/input/subtrees_Hardcode.xml"
    val parser: StaXParser = StaXParser(importList, "com.intellij.psi", "PsiElementFactory")
    val text = parser.readXml(path).toString()
    assertTrue(text.contains("_TAG") && text.contains("PsiNewExpression")
            && text.contains(", ClassBodyOwner")
            && text.contains("EnumConstantInitializer") && text.contains("CLASS_BODY_TAG")
            , "Statement with subtrees and hardcode in xml wasn't generated correctly")
}