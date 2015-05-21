package com.intellij.whileLang;

import com.intellij.psi.*
import com.intellij.openapi.project.Project

import org.jetbrains.likePrinter.util.base.*
import java.util.ArrayList
import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.likePrinter.util.psiElement.*
import com.intellij.openapi.command.impl.DummyProject
import org.jetbrains.likePrinter.templateBase.template.SmartInsertPlace
import org.jetbrains.likePrinter.templateBase.template.PsiTemplate
import org.jetbrains.likePrinter.components.statements.*
import org.jetbrains.likePrinter.components.*
import org.jetbrains.likePrinter.templateBase.template.Template
import org.jetbrains.likePrinter.components.expressions.*
import org.jetbrains.likePrinter.components.classes.*
import org.jetbrains.likePrinter.templateBase.template.PsiTemplateGen
import org.jetbrains.likePrinter.components.lists.*
import org.jetbrains.likePrinter.components.types.*
import org.jetbrains.likePrinter.components.variables.*
import java.util.HashMap
import com.intellij.util.IncorrectOperationException
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.codeInsight.documentation.DocumentationManager
import org.jetbrains.likePrinter.components.statements.SwitchLabelComponent
import org.jetbrains.likePrinter.printer.CommentConnectionUtils.CommentConnection
import org.jetbrains.likePrinter.printer.CommentConnectionUtils.VariantConstructionContext
import org.jetbrains.format.FormatSet.FormatSetType
import java.lang.management.ManagementFactory
import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.format.Format
import org.jetbrains.format.FormatSet
import org.jetbrains.format.SteppedFormatMap
import org.jetbrains.format.FormatMap3D_AF
import org.jetbrains.format.FormatMap3D
import org.jetbrains.format.FormatMap2D_LL
import org.jetbrains.format.FormatMap1D
import org.jetbrains.format.FormatList
import org.jetbrains.likePrinter.performUndoWrite


class Printer(
  templateFile: WhileFile?
, private val settings: PrinterSettings
): Memoization(), CommentConnectionUtils {
    companion object {
        public fun create(templateFile: WhileFile?, project: Project, width: Int): Printer =
                Printer(templateFile, PrinterSettings.createProjectSettings(width, project))
    }

    public fun hasToUseMultipleListElemVariants(): Boolean = settings.multipleListElemVariants
    public fun hasToUseMultipleExprStmtVariants(): Boolean = settings.multipleExprStmtVariants

    public fun setMultipleListElemVariantNeeds(f: Boolean) { settings.multipleListElemVariants = f }
    public fun setMultipleExprStmtVariantNeeds(f: Boolean) { settings.multipleExprStmtVariants = f }
    public fun setFormatSetType(f: FormatSetType) { settings.formatSetType = f }
    public fun setMaxWidth(f: Int) { settings.width = f }

    override public fun getMaxWidth(): Int = settings.width
    public fun    getProject(): Project   = settings.project
    public fun   getEmptySet(): FormatSet =
        when (settings.formatSetType) {
            FormatSetType.D1   -> FormatMap1D   (getMaxWidth())
            FormatSetType.D2   -> FormatMap2D_LL(getMaxWidth())
            FormatSetType.D3   -> FormatMap3D   (getMaxWidth())
            FormatSetType.D3AF -> FormatMap3D_AF(getMaxWidth())
            FormatSetType.List -> FormatList    (getMaxWidth())
            FormatSetType.SteppedD3AF -> SteppedFormatMap(FormatSet.stepInMap, getMaxWidth())

            else -> FormatMap3D(getMaxWidth())
        }
    override public fun getInitialSet(f: Format): FormatSet {
        val fs = getEmptySet()
        fs.add(f)
        return fs
    }

    //WARNING: must be declared before init!!!
    //COMPONENTS
    public val AssignStmtComponent: AssignStmtComponent = AssignStmtComponent(this)
    public val IfStmtComponent: IfStmtComponent = IfStmtComponent(this)
    public val ReadStmtComponent: ReadStmtComponent = ReadStmtComponent(this)
    public val SkipStmtComponent: SkipStmtComponent = SkipStmtComponent(this)
    public val WhileStmtComponent: WhileStmtComponent = WhileStmtComponent(this)
    public val StmtListComponent: StmtListComponent = StmtListComponent(this)
    public val WriteStmtComponent: WriteStmtComponent = WriteStmtComponent(this)
    public val BinaryExprComponent: BinaryExprComponent = BinaryExprComponent(this)
    public val ParenExprComponent: ParenExprComponent = ParenExprComponent(this)
    public val BinaryBexprComponent: BinaryBexprComponent = BinaryBexprComponent(this)
    public val ParenBexprComponent: ParenBexprComponent = ParenBexprComponent(this)
    public val NotBexprComponent: NotBexprComponent = NotBexprComponent(this)
    
    public val WhileFileComponent: WhileFileComponent = WhileFileComponent(this)

    public fun reprint(mFile: WhileFile) { reprintElementWithChildren(mFile) }

    init {
        if (templateFile != null) {
            fillTemplateLists(templateFile)
        }
    }

    /// public only for testing purposes!!!
    public fun reprintElementWithChildren(psiElement: PsiElement) {
        reprintElementWithChildren_AllMeaningful(psiElement) // variant for partial template
//        reprintElementWithChildren_OnlyWhileFile(psiElement) // variant for situations with full template
    }

    private fun reprintElementWithChildren_OnlyWhileFile(psiElement: PsiElement) {
        walker(psiElement) { p -> if (p is WhileFile) applyTmplt(p) }
    }

    private fun reprintElementWithChildren_AllMeaningful(psiElement: PsiElement) {
        walker(psiElement) { p ->
            when (p) {
                is PsiAssignStmt -> applyTmplt(p)
                is PsiIfStmt -> applyTmplt(p)
                is PsiReadStmt -> applyTmplt(p)
                is PsiSkipStmt -> applyTmplt(p)
                is PsiWhileStmt -> applyTmplt(p)
                is PsiStmtList -> applyTmplt(p)
                is PsiWriteStmt -> applyTmplt(p)
                is PsiBinaryExpr -> applyTmplt(p)
                is PsiParenExpr -> applyTmplt(p)
                is PsiBinaryBexpr -> applyTmplt(p)
                is PsiParenBexpr -> applyTmplt(p)
                is PsiNotBexpr -> applyTmplt(p)
                
                else -> 5 + 5
            }
        }
    }

    public fun getVariants(p: PsiElement, context: VariantConstructionContext = defaultContext()): FormatSet {
        val pCommentContext = getCommentContext(p)
        val widthToSuit = context.widthToSuit
        val variantConstructionContext = VariantConstructionContext(pCommentContext, widthToSuit)

        val mv = getMemoizedVariants(p)
        if (mv != null) { return surroundVariantsByAttachedComments(p, mv, context) }

        val resultWithoutOuterContextComments: FormatSet
        val templateVariant = getTemplateVariants(p, variantConstructionContext)
        if (templateVariant.isNotEmpty()) {
            resultWithoutOuterContextComments = surroundVariantsByAttachedComments(
                      p, templateVariant, variantConstructionContext
            )

            addToCache(p, resultWithoutOuterContextComments)
        } else {
            val s = p.getText() ?: ""
            if (s.contains(" ")) { log.add(s) }
            resultWithoutOuterContextComments = getVariantsByText(p)

            //TODO: For test purposes!!!
            addToCache(p, resultWithoutOuterContextComments)
        }

        val variants = surroundVariantsByAttachedComments(p, resultWithoutOuterContextComments, context)
        return variants
    }

    override public fun getVariantsByText(p: PsiElement): FormatSet {
        val offsetInStartLine = p.getOffsetInStartLine()
        val normalizedFillConstant = Math.max(p.getFillConstant(), 0)
        return getInitialSet(Format.text(p.getText(), offsetInStartLine + normalizedFillConstant))
    }

    private fun getTemplateVariants(p: PsiElement, context: VariantConstructionContext): FormatSet {
        val variants: FormatSet =
            when(p) {
                is PsiAssignStmt -> AssignStmtComponent.getVariants(p, context)
                is PsiIfStmt -> IfStmtComponent.getVariants(p, context)
                is PsiReadStmt -> ReadStmtComponent.getVariants(p, context)
                is PsiSkipStmt -> SkipStmtComponent.getVariants(p, context)
                is PsiWhileStmt -> WhileStmtComponent.getVariants(p, context)
                is PsiStmtList -> StmtListComponent.getVariants(p, context)
                is PsiWriteStmt -> WriteStmtComponent.getVariants(p, context)
                is PsiBinaryExpr -> BinaryExprComponent.getVariants(p, context)
                is PsiParenExpr -> ParenExprComponent.getVariants(p, context)
                is PsiBinaryBexpr -> BinaryBexprComponent.getVariants(p, context)
                is PsiParenBexpr -> ParenBexprComponent.getVariants(p, context)
                is PsiNotBexpr -> NotBexprComponent.getVariants(p, context)
                
                is WhileFile                    ->                    WhileFileComponent.getVariants(p, context)

                //Just cut from text
                else -> {
//                    println("AAA: ${p.getClass()}")
                    getEmptySet()
                }
            }

        return variants
    }

    public  fun areTemplatesFilled(): Boolean = areTemplatesFilled
    private var areTemplatesFilled  : Boolean = false

    public fun fillTemplateLists(templateFile: WhileFile) {
        areTemplatesFilled = true
        walker(templateFile, { p: PsiElement ->
            when (p) {
                is PsiAssignStmt -> AssignStmtComponent.getAndSaveTemplate(p)
                is PsiIfStmt -> IfStmtComponent.getAndSaveTemplate(p)
                is PsiReadStmt -> ReadStmtComponent.getAndSaveTemplate(p)
                is PsiSkipStmt -> SkipStmtComponent.getAndSaveTemplate(p)
                is PsiWhileStmt -> WhileStmtComponent.getAndSaveTemplate(p)
                is PsiStmtList -> StmtListComponent.getAndSaveTemplate(p)
                is PsiWriteStmt -> WriteStmtComponent.getAndSaveTemplate(p)
                is PsiBinaryExpr -> BinaryExprComponent.getAndSaveTemplate(p)
                is PsiParenExpr -> ParenExprComponent.getAndSaveTemplate(p)
                is PsiBinaryBexpr -> BinaryBexprComponent.getAndSaveTemplate(p)
                is PsiParenBexpr -> ParenBexprComponent.getAndSaveTemplate(p)
                is PsiNotBexpr -> NotBexprComponent.getAndSaveTemplate(p)
                
                else -> 5 + 5
            }
        })
    }

    private fun createElementFromText(p: PsiElement, text: String): PsiElement? {
        val factory = WhileElementFactory(getProject())
        if (factory == null) { return null }

        when (p) {
            is PsiMethod       -> return factory.      createMethodFromText(text, null)
            is PsiEnumConstant -> return factory.createEnumConstantFromText(text, null)
            is PsiAnonymousClass -> {
                val exp = factory.createExpressionFromText("new\n$text", null)
                if (exp !is PsiNewExpression) { return null }
                val newAnonymousClass = exp.getAnonymousClass()
                return newAnonymousClass
            }
            is PsiClass -> {
                val dummyClass = factory.createClassFromText(text, null)
                val allInnerClasses = dummyClass.getAllInnerClasses()
                val newClass = allInnerClasses[0]
                return newClass
            }
            is PsiParameter  -> return factory. createParameterFromText(text, null)
            is PsiAnnotation -> return factory.createAnnotationFromText(text, null)
//            is PsiJavaCodeReferenceElement -> return factory?.createReferenceFromText(text, null)
            is PsiField            -> return factory.        createFieldFromText(text, null)
            is PsiTypeElement      -> return factory.  createTypeElementFromText(text, null)
            is PsiCodeBlock        -> return factory.    createCodeBlockFromText(text, null)
            is PsiResourceVariable -> return factory.     createResourceFromText(text, null)
            is PsiTypeParameter    -> return factory.createTypeParameterFromText(text, null)


            is PsiExpression -> return factory.createExpressionFromText(text, null)
            else -> return factory.createStatementFromText(text, null)
        }
    }

    private fun applyTmplt(p: PsiElement) {
        val formatSet = getVariants(p)

        val threadMXBean = ManagementFactory.getThreadMXBean()!!
        val startTime = threadMXBean.getCurrentThreadCpuTime()
        val chosenFormat = formatSet.head()
        if (chosenFormat == null) { return }

        fun replaceElement(newElement: PsiElement) {
            getProject().performUndoWrite { p.replace(newElement) }
        }

        val startLineOffset = p.getOffsetInStartLine()
        val newElementText = chosenFormat.toText(startLineOffset, "")

        if (p is WhileFile) {
            val document = PsiDocumentManager.getInstance(getProject())?.getDocument(p)
            val oldDocSize = document?.getText()?.size
            if (document == null || oldDocSize == null) { return }
            getProject().performUndoWrite {
                document.replaceString(0, oldDocSize, newElementText)
            }
            return
        }

        val statement: PsiElement
        try {
            val createdStatement = createElementFromText(p, newElementText)
            if (createdStatement == null) { return }
            statement = createdStatement
        } catch (e: IncorrectOperationException) { return }

        if (p is PsiCodeBlock && statement is PsiBlockStatement) {
            renewCache(p, statement)
            replaceElement(statement.getCodeBlock())
            return
        }
        renewCache(p, statement)
        replaceElement(statement)

        val endTime = threadMXBean.getCurrentThreadCpuTime()
        replaceTime += endTime - startTime
    }
}

