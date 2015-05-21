package org.jetbrains.likePrinter.printer

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
  templateFile: PsiJavaFile?
, private val settings: PrinterSettings
): Memoization(), CommentConnectionUtils {
    companion object {
        public fun create(templateFile: PsiJavaFile?, project: Project, width: Int): Printer =
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
    public val ifComponent: IfComponent = IfComponent(this)
    public val whileComponent: WhileComponent = WhileComponent(this)
    public val doWhileComponent: DoWhileComponent = DoWhileComponent(this)
    public val forComponent: ForComponent = ForComponent(this)
    public val foreachComponent: ForeachComponent = ForeachComponent(this)
    public val switchComponent: SwitchComponent = SwitchComponent(this)
    public val synchronizedComponent: SynchronizedComponent = SynchronizedComponent(this)
    public val tryComponent: TryComponent = TryComponent(this)
    public val newComponent: NewComponent = NewComponent(this)
    public val lambdaComponent: LambdaComponent = LambdaComponent(this)
    public val postfixComponent: PostfixComponent = PostfixComponent(this)
    public val prefixComponent: PrefixComponent = PrefixComponent(this)
    public val assignmentComponent: AssignmentComponent = AssignmentComponent(this)
    public val classInitializerComponent: ClassInitializerComponent = ClassInitializerComponent(this)
    public val typeCastComponent: TypeCastComponent = TypeCastComponent(this)
    public val conditionalComponent: ConditionalComponent = ConditionalComponent(this)
    public val instanceOfComponent: InstanceOfComponent = InstanceOfComponent(this)
    public val arrayAccessComponent: ArrayAccessComponent = ArrayAccessComponent(this)
    public val switchLabelComponent: SwitchLabelComponent = SwitchLabelComponent(this)
    public val annotationMethodComponent: AnnotationMethodComponent = AnnotationMethodComponent(this)
    public val methodComponent: MethodComponent = MethodComponent(this)
    public val qualifiedComponent: QualifiedComponent = QualifiedComponent(this)
    public val polyadicComponent: PolyadicComponent = PolyadicComponent(this)
    public val referenceComponent: ReferenceComponent = ReferenceComponent(this)
    public val methodCallComponent: MethodCallComponent = MethodCallComponent(this)
    public val arrayInitializerComponent: ArrayInitializerComponent = ArrayInitializerComponent(this)
    public val declarationComponent: DeclarationComponent = DeclarationComponent(this)
    public val parenthesizedComponent: ParenthesizedComponent = ParenthesizedComponent(this)
    public val breakComponent: BreakComponent = BreakComponent(this)
    public val continueComponent: ContinueComponent = ContinueComponent(this)
    public val returnComponent: ReturnComponent = ReturnComponent(this)
    public val assertComponent: AssertComponent = AssertComponent(this)
    public val throwComponent: ThrowComponent = ThrowComponent(this)
    public val labeledComponent: LabeledComponent = LabeledComponent(this)
    public val resourceListComponent: ResourceListComponent = ResourceListComponent(this)
    public val referenceListComponent: ReferenceListComponent = ReferenceListComponent(this)
    public val parameterListComponent: ParameterListComponent = ParameterListComponent(this)
    public val typeParameterListComponent: TypeParameterListComponent = TypeParameterListComponent(this)
    public val expressionListComponent: ExpressionListComponent = ExpressionListComponent(this)
    public val annotationParameterListComponent: AnnotationParameterListComponent = AnnotationParameterListComponent(this)
    public val referenceParameterListComponent: ReferenceParameterListComponent = ReferenceParameterListComponent(this)
    public val typeParameterComponent: TypeParameterComponent = TypeParameterComponent(this)
    public val enumConstantInitializerComponent: EnumConstantInitializerComponent = EnumConstantInitializerComponent(this)
    public val typeElementComponent: TypeElementComponent = TypeElementComponent(this)
    public val anonymousClassComponent: AnonymousClassComponent = AnonymousClassComponent(this)
    public val classComponent: ClassComponent = ClassComponent(this)
    public val parameterComponent: ParameterComponent = ParameterComponent(this)
    public val enumConstantComponent: EnumConstantComponent = EnumConstantComponent(this)
    public val resourceVariableComponent: ResourceVariableComponent = ResourceVariableComponent(this)
    public val variableComponent: VariableComponent = VariableComponent(this)
    public val expressionStatementComponent: ExpressionStatementComponent = ExpressionStatementComponent(this)
    public val codeBlockComponent: CodeBlockComponent = CodeBlockComponent(this)
    public val nameValuePairComponent: NameValuePairComponent = NameValuePairComponent(this)
    public val annotationComponent: AnnotationComponent = AnnotationComponent(this)
    public val arrayInitializerMemberValueComponent: ArrayInitializerMemberValueComponent = ArrayInitializerMemberValueComponent(this)
    public val modifierListComponent: ModifierListComponent = ModifierListComponent(this)
    public val classObjectAccessComponent: ClassObjectAccessComponent = ClassObjectAccessComponent(this)
    public val packageComponent: PackageComponent = PackageComponent(this)
    public val javaCodeReferenceComponent: JavaCodeReferenceComponent = JavaCodeReferenceComponent(this)
    public val importStatementBaseComponent: ImportStatementBaseComponent = ImportStatementBaseComponent(this)
    public val importListComponent: ImportListComponent = ImportListComponent(this)
    
    public val javaFileComponent: JavaFileComponent = JavaFileComponent(this)

    public fun reprint(mFile: PsiJavaFile) { reprintElementWithChildren(mFile) }

    init {
        if (templateFile != null) {
            fillTemplateLists(templateFile)
        }
    }

    /// public only for testing purposes!!!
    public fun reprintElementWithChildren(psiElement: PsiElement) {
        reprintElementWithChildren_AllMeaningful(psiElement) // variant for partial template
//        reprintElementWithChildren_OnlyPsiJavaFile(psiElement) // variant for situations with full template
    }

    private fun reprintElementWithChildren_OnlyPsiJavaFile(psiElement: PsiElement) {
        walker(psiElement) { p -> if (p is PsiJavaFile) applyTmplt(p) }
    }

    private fun reprintElementWithChildren_AllMeaningful(psiElement: PsiElement) {
        walker(psiElement) { p ->
            when (p) {
                is PsiIfStatement -> applyTmplt(p)
                is PsiWhileStatement -> applyTmplt(p)
                is PsiDoWhileStatement -> applyTmplt(p)
                is PsiForStatement -> applyTmplt(p)
                is PsiForeachStatement -> applyTmplt(p)
                is PsiSwitchStatement -> applyTmplt(p)
                is PsiSynchronizedStatement -> applyTmplt(p)
                is PsiTryStatement -> applyTmplt(p)
                is PsiNewExpression -> applyTmplt(p)
                is PsiLambdaExpression -> applyTmplt(p)
                is PsiPostfixExpression -> applyTmplt(p)
                is PsiPrefixExpression -> applyTmplt(p)
                is PsiAssignmentExpression -> applyTmplt(p)
                is PsiClassInitializer -> applyTmplt(p)
                is PsiTypeCastExpression -> applyTmplt(p)
                is PsiConditionalExpression -> applyTmplt(p)
                is PsiInstanceOfExpression -> applyTmplt(p)
                is PsiArrayAccessExpression -> applyTmplt(p)
                is PsiSwitchLabelStatement -> applyTmplt(p)
                is PsiAnnotationMethod -> applyTmplt(p)
                is PsiMethod -> applyTmplt(p)
                is PsiQualifiedExpression -> applyTmplt(p)
                is PsiPolyadicExpression -> applyTmplt(p)
                is PsiReferenceExpression -> applyTmplt(p)
                is PsiMethodCallExpression -> applyTmplt(p)
                is PsiArrayInitializerExpression -> applyTmplt(p)
                is PsiDeclarationStatement -> applyTmplt(p)
                is PsiParenthesizedExpression -> applyTmplt(p)
                is PsiBreakStatement -> applyTmplt(p)
                is PsiContinueStatement -> applyTmplt(p)
                is PsiReturnStatement -> applyTmplt(p)
                is PsiAssertStatement -> applyTmplt(p)
                is PsiThrowStatement -> applyTmplt(p)
                is PsiLabeledStatement -> applyTmplt(p)
                is PsiResourceList -> applyTmplt(p)
                is PsiReferenceList -> applyTmplt(p)
                is PsiParameterList -> applyTmplt(p)
                is PsiTypeParameterList -> applyTmplt(p)
                is PsiExpressionList -> applyTmplt(p)
                is PsiAnnotationParameterList -> applyTmplt(p)
                is PsiReferenceParameterList -> applyTmplt(p)
                is PsiTypeParameter -> applyTmplt(p)
                is PsiEnumConstantInitializer -> applyTmplt(p)
                is PsiTypeElement -> applyTmplt(p)
                is PsiAnonymousClass -> applyTmplt(p)
                is PsiClass -> applyTmplt(p)
                is PsiParameter -> applyTmplt(p)
                is PsiEnumConstant -> applyTmplt(p)
                is PsiResourceVariable -> applyTmplt(p)
                is PsiVariable -> applyTmplt(p)
                is PsiExpressionStatement -> applyTmplt(p)
                is PsiBlockStatement -> applyTmplt(p)
                is PsiNameValuePair -> applyTmplt(p)
                is PsiAnnotation -> applyTmplt(p)
                is PsiArrayInitializerMemberValue -> applyTmplt(p)
                is PsiModifierList -> applyTmplt(p)
                is PsiClassObjectAccessExpression -> applyTmplt(p)
                is PsiPackageStatement -> applyTmplt(p)
                is PsiJavaCodeReferenceElement -> applyTmplt(p)
                is PsiImportStatementBase -> applyTmplt(p)
                is PsiImportList -> applyTmplt(p)
                
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
                is PsiIfStatement -> ifComponent.getVariants(p, context)
                is PsiWhileStatement -> whileComponent.getVariants(p, context)
                is PsiDoWhileStatement -> doWhileComponent.getVariants(p, context)
                is PsiForStatement -> forComponent.getVariants(p, context)
                is PsiForeachStatement -> foreachComponent.getVariants(p, context)
                is PsiSwitchStatement -> switchComponent.getVariants(p, context)
                is PsiSynchronizedStatement -> synchronizedComponent.getVariants(p, context)
                is PsiTryStatement -> tryComponent.getVariants(p, context)
                is PsiNewExpression -> newComponent.getVariants(p, context)
                is PsiLambdaExpression -> lambdaComponent.getVariants(p, context)
                is PsiPostfixExpression -> postfixComponent.getVariants(p, context)
                is PsiPrefixExpression -> prefixComponent.getVariants(p, context)
                is PsiAssignmentExpression -> assignmentComponent.getVariants(p, context)
                is PsiClassInitializer -> classInitializerComponent.getVariants(p, context)
                is PsiTypeCastExpression -> typeCastComponent.getVariants(p, context)
                is PsiConditionalExpression -> conditionalComponent.getVariants(p, context)
                is PsiInstanceOfExpression -> instanceOfComponent.getVariants(p, context)
                is PsiArrayAccessExpression -> arrayAccessComponent.getVariants(p, context)
                is PsiSwitchLabelStatement -> switchLabelComponent.getVariants(p, context)
                is PsiAnnotationMethod -> annotationMethodComponent.getVariants(p, context)
                is PsiMethod -> methodComponent.getVariants(p, context)
                is PsiQualifiedExpression -> qualifiedComponent.getVariants(p, context)
                is PsiPolyadicExpression -> polyadicComponent.getVariants(p, context)
                is PsiReferenceExpression -> referenceComponent.getVariants(p, context)
                is PsiMethodCallExpression -> methodCallComponent.getVariants(p, context)
                is PsiArrayInitializerExpression -> arrayInitializerComponent.getVariants(p, context)
                is PsiDeclarationStatement -> declarationComponent.getVariants(p, context)
                is PsiParenthesizedExpression -> parenthesizedComponent.getVariants(p, context)
                is PsiBreakStatement -> breakComponent.getVariants(p, context)
                is PsiContinueStatement -> continueComponent.getVariants(p, context)
                is PsiReturnStatement -> returnComponent.getVariants(p, context)
                is PsiAssertStatement -> assertComponent.getVariants(p, context)
                is PsiThrowStatement -> throwComponent.getVariants(p, context)
                is PsiLabeledStatement -> labeledComponent.getVariants(p, context)
                is PsiResourceList -> resourceListComponent.getVariants(p, context)
                is PsiReferenceList -> referenceListComponent.getVariants(p, context)
                is PsiParameterList -> parameterListComponent.getVariants(p, context)
                is PsiTypeParameterList -> typeParameterListComponent.getVariants(p, context)
                is PsiExpressionList -> expressionListComponent.getVariants(p, context)
                is PsiAnnotationParameterList -> annotationParameterListComponent.getVariants(p, context)
                is PsiReferenceParameterList -> referenceParameterListComponent.getVariants(p, context)
                is PsiTypeParameter -> typeParameterComponent.getVariants(p, context)
                is PsiEnumConstantInitializer -> enumConstantInitializerComponent.getVariants(p, context)
                is PsiTypeElement -> typeElementComponent.getVariants(p, context)
                is PsiAnonymousClass -> anonymousClassComponent.getVariants(p, context)
                is PsiClass -> classComponent.getVariants(p, context)
                is PsiParameter -> parameterComponent.getVariants(p, context)
                is PsiEnumConstant -> enumConstantComponent.getVariants(p, context)
                is PsiResourceVariable -> resourceVariableComponent.getVariants(p, context)
                is PsiVariable -> variableComponent.getVariants(p, context)
                is PsiExpressionStatement -> expressionStatementComponent.getVariants(p, context)
                is PsiBlockStatement -> codeBlockComponent.getVariants(p, context)
                is PsiNameValuePair -> nameValuePairComponent.getVariants(p, context)
                is PsiAnnotation -> annotationComponent.getVariants(p, context)
                is PsiArrayInitializerMemberValue -> arrayInitializerMemberValueComponent.getVariants(p, context)
                is PsiModifierList -> modifierListComponent.getVariants(p, context)
                is PsiClassObjectAccessExpression -> classObjectAccessComponent.getVariants(p, context)
                is PsiPackageStatement -> packageComponent.getVariants(p, context)
                is PsiJavaCodeReferenceElement -> javaCodeReferenceComponent.getVariants(p, context)
                is PsiImportStatementBase -> importStatementBaseComponent.getVariants(p, context)
                is PsiImportList -> importListComponent.getVariants(p, context)
                
                is PsiJavaFile                    ->                    javaFileComponent.getVariants(p, context)

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

    public fun fillTemplateLists(templateFile: PsiJavaFile) {
        areTemplatesFilled = true
        walker(templateFile, { p: PsiElement ->
            when (p) {
                is PsiIfStatement -> ifComponent.getAndSaveTemplate(p)
                is PsiWhileStatement -> whileComponent.getAndSaveTemplate(p)
                is PsiDoWhileStatement -> doWhileComponent.getAndSaveTemplate(p)
                is PsiForStatement -> forComponent.getAndSaveTemplate(p)
                is PsiForeachStatement -> foreachComponent.getAndSaveTemplate(p)
                is PsiSwitchStatement -> switchComponent.getAndSaveTemplate(p)
                is PsiSynchronizedStatement -> synchronizedComponent.getAndSaveTemplate(p)
                is PsiTryStatement -> tryComponent.getAndSaveTemplate(p)
                is PsiNewExpression -> newComponent.getAndSaveTemplate(p)
                is PsiLambdaExpression -> lambdaComponent.getAndSaveTemplate(p)
                is PsiPostfixExpression -> postfixComponent.getAndSaveTemplate(p)
                is PsiPrefixExpression -> prefixComponent.getAndSaveTemplate(p)
                is PsiAssignmentExpression -> assignmentComponent.getAndSaveTemplate(p)
                is PsiClassInitializer -> classInitializerComponent.getAndSaveTemplate(p)
                is PsiTypeCastExpression -> typeCastComponent.getAndSaveTemplate(p)
                is PsiConditionalExpression -> conditionalComponent.getAndSaveTemplate(p)
                is PsiInstanceOfExpression -> instanceOfComponent.getAndSaveTemplate(p)
                is PsiArrayAccessExpression -> arrayAccessComponent.getAndSaveTemplate(p)
                is PsiSwitchLabelStatement -> switchLabelComponent.getAndSaveTemplate(p)
                is PsiAnnotationMethod -> annotationMethodComponent.getAndSaveTemplate(p)
                is PsiMethod -> methodComponent.getAndSaveTemplate(p)
                is PsiQualifiedExpression -> qualifiedComponent.getAndSaveTemplate(p)
                is PsiPolyadicExpression -> polyadicComponent.getAndSaveTemplate(p)
                is PsiReferenceExpression -> referenceComponent.getAndSaveTemplate(p)
                is PsiMethodCallExpression -> methodCallComponent.getAndSaveTemplate(p)
                is PsiArrayInitializerExpression -> arrayInitializerComponent.getAndSaveTemplate(p)
                is PsiDeclarationStatement -> declarationComponent.getAndSaveTemplate(p)
                is PsiParenthesizedExpression -> parenthesizedComponent.getAndSaveTemplate(p)
                is PsiBreakStatement -> breakComponent.getAndSaveTemplate(p)
                is PsiContinueStatement -> continueComponent.getAndSaveTemplate(p)
                is PsiReturnStatement -> returnComponent.getAndSaveTemplate(p)
                is PsiAssertStatement -> assertComponent.getAndSaveTemplate(p)
                is PsiThrowStatement -> throwComponent.getAndSaveTemplate(p)
                is PsiLabeledStatement -> labeledComponent.getAndSaveTemplate(p)
                is PsiResourceList -> resourceListComponent.getAndSaveTemplate(p)
                is PsiReferenceList -> referenceListComponent.getAndSaveTemplate(p)
                is PsiParameterList -> parameterListComponent.getAndSaveTemplate(p)
                is PsiTypeParameterList -> typeParameterListComponent.getAndSaveTemplate(p)
                is PsiExpressionList -> expressionListComponent.getAndSaveTemplate(p)
                is PsiAnnotationParameterList -> annotationParameterListComponent.getAndSaveTemplate(p)
                is PsiReferenceParameterList -> referenceParameterListComponent.getAndSaveTemplate(p)
                is PsiTypeParameter -> typeParameterComponent.getAndSaveTemplate(p)
                is PsiEnumConstantInitializer -> enumConstantInitializerComponent.getAndSaveTemplate(p)
                is PsiTypeElement -> typeElementComponent.getAndSaveTemplate(p)
                is PsiAnonymousClass -> anonymousClassComponent.getAndSaveTemplate(p)
                is PsiClass -> classComponent.getAndSaveTemplate(p)
                is PsiParameter -> parameterComponent.getAndSaveTemplate(p)
                is PsiEnumConstant -> enumConstantComponent.getAndSaveTemplate(p)
                is PsiResourceVariable -> resourceVariableComponent.getAndSaveTemplate(p)
                is PsiVariable -> variableComponent.getAndSaveTemplate(p)
                is PsiExpressionStatement -> expressionStatementComponent.getAndSaveTemplate(p)
                is PsiBlockStatement -> codeBlockComponent.getAndSaveTemplate(p)
                is PsiNameValuePair -> nameValuePairComponent.getAndSaveTemplate(p)
                is PsiAnnotation -> annotationComponent.getAndSaveTemplate(p)
                is PsiArrayInitializerMemberValue -> arrayInitializerMemberValueComponent.getAndSaveTemplate(p)
                is PsiModifierList -> modifierListComponent.getAndSaveTemplate(p)
                is PsiClassObjectAccessExpression -> classObjectAccessComponent.getAndSaveTemplate(p)
                is PsiPackageStatement -> packageComponent.getAndSaveTemplate(p)
                is PsiJavaCodeReferenceElement -> javaCodeReferenceComponent.getAndSaveTemplate(p)
                is PsiImportStatementBase -> importStatementBaseComponent.getAndSaveTemplate(p)
                is PsiImportList -> importListComponent.getAndSaveTemplate(p)
                
                else -> 5 + 5
            }
        })
    }

    private fun createElementFromText(p: PsiElement, text: String): PsiElement? {
        val factory = JavaPsiFacade.getElementFactory(getProject())
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

        if (p is PsiJavaFile) {
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

