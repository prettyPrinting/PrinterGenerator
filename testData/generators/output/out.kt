package org.jetbrains.likePrinter.components

import org.jetbrains.likePrinter.printer.Printer
import org.jetbrains.likePrinter.templateBase.template.SmartInsertPlace
import org.jetbrains.likePrinter.templateBase.template.PsiTemplateGen
import org.jetbrains.likePrinter.util.psiElement.getTextRange
import java.util.HashMap
import org.jetbrains.likePrinter.util.psiElement.toSmartInsertPlace
import org.jetbrains.likePrinter.printer.CommentConnectionUtils.VariantConstructionContext

import org.jetbrains.likePrinter.util.psiElement.getCorrectTextOffset
import java.util.HashSet
import org.jetbrains.format.FormatSet
import org.jetbrains.likePrinter.util.string.getFillConstant
import org.jetbrains.likePrinter.util.box.Box
import org.jetbrains.likePrinter.templateBase.template.PsiElementComponent
import com.intellij.psi.PsiElementFactory
import org.jetbrains.likePrinter.util.psiElement.toBox
import com.intellij.util.IncorrectOperationException
import org.jetbrains.likePrinter.util.psiElement.*
import com.intellij.psi.PsiTypeParameter


public class TypeParameterComponent(
        printer: Printer
): PsiElementComponent<PsiTypeParameter, SmartInsertPlace, PsiTemplateGen<PsiTypeParameter, SmartInsertPlace>>(printer)
   
{

    final val NAME_IDENTIFIER_TAG: String
        get() = "name_identifier"
    final val EXTENDS_LIST_TAG: String
        get() = "extends_list"
    final val ANNOTATION_TAG: String
        get() = "annotation"
    
    private fun addName_identifierToInsertPlaceMap(
            p: PsiTypeParameter
            , insertPlaceMap: MutableMap<String, SmartInsertPlace>
            , delta: Int
    ): Boolean {
        val name_identifier = p.getNameIdentifier()
        val name_identifierTextRange = name_identifier?.getTextRange()
        if (name_identifierTextRange == null) { return false }
        
        val text = p.getContainingFile()?.getText()
        if (text == null) { return false }
        val fillConstant = text.getFillConstant(name_identifierTextRange)
        
        insertPlaceMap.put(
               NAME_IDENTIFIER_TAG
               , SmartInsertPlace(name_identifierTextRange.shiftRight(delta), fillConstant, name_identifier!!.toBox())
            )
        return true
    }
    
    private fun prepareName_identifierVariants(
            p: PsiTypeParameter
            , variants: MutableMap<String, FormatSet>
            , context: VariantConstructionContext
    ) {
        val name_identifierVariants = getName_identifierVariants(p, context)
        if (name_identifierVariants.isEmpty()) { return }
        variants.put(NAME_IDENTIFIER_TAG, name_identifierVariants)
    }
    
    private fun getName_identifierVariants(
            p: PsiTypeParameter
            , context: VariantConstructionContext
    ): FormatSet {
        val name_identifier = p.getNameIdentifier()
        if (name_identifier == null) { return printer.getEmptySet() }
        return printer.getVariants(name_identifier, context)
    }
    
    private fun addExtends_listToInsertPlaceMap(
            p: PsiTypeParameter
            , insertPlaceMap: MutableMap<String, SmartInsertPlace>
            , delta: Int
    ): Boolean {
        val extends_list = p.getExtendsList()
        val extends_listTextRange = extends_list?.getTextRange()
        if (extends_listTextRange == null) { return false }
        
        val text = p.getContainingFile()?.getText()
        if (text == null) { return false }
        val fillConstant = text.getFillConstant(extends_listTextRange)
        
        insertPlaceMap.put(
               EXTENDS_LIST_TAG
               , SmartInsertPlace(extends_listTextRange.shiftRight(delta), fillConstant, Box.getEverywhereSuitable())
            )
        return true
    }
    
    private fun prepareExtends_listVariants(
            p: PsiTypeParameter
            , variants: MutableMap<String, FormatSet>
            , context: VariantConstructionContext
    ) {
        val extends_listVariants = getExtends_listVariants(p, context)
        if (extends_listVariants.isEmpty()) { return }
        variants.put(EXTENDS_LIST_TAG, extends_listVariants)
    }
    
    private fun getExtends_listVariants(
            p: PsiTypeParameter
            , context: VariantConstructionContext
    ): FormatSet {
        val extends_list = p.getExtendsList()
        if (extends_list == null) { return printer.getEmptySet() }
        return printer.getVariants(extends_list, context)
    }
    
    private fun addAnnotationToInsertPlaceMap(
            p: PsiTypeParameter
            , insertPlaceMap: MutableMap<String, SmartInsertPlace>
            , delta: Int
    ): Boolean {
        val annotation = p.getAnnotations()
        val annotationTextRange = annotation?.getTextRange()
        if (annotationTextRange == null) { return false }
        
        val text = p.getContainingFile()?.getText()
        if (text == null) { return false }
        val fillConstant = text.getFillConstant(annotationTextRange)
        
        insertPlaceMap.put(
               ANNOTATION_TAG
               , SmartInsertPlace(annotationTextRange.shiftRight(delta), fillConstant, Box.getEverywhereSuitable())
            )
        return true
    }
    
    private fun prepareAnnotationVariants(
            p: PsiTypeParameter
            , variants: MutableMap<String, FormatSet>
            , context: VariantConstructionContext
    ) {
        val annotationVariants = getAnnotationVariants(p, context)
        if (annotationVariants.isEmpty()) { return }
        variants.put(ANNOTATION_TAG, annotationVariants)
    }
    
    private fun getAnnotationVariants(
            p: PsiTypeParameter
            , context: VariantConstructionContext
    ): FormatSet {
        val annotation = p.getAnnotations()
        if (annotation == null || annotation.isEmpty()) { return printer.getEmptySet() }
        
        val annotationVariants = annotation.map { e -> printer.getVariants(e, context) }
        val variants = annotationVariants.fold(printer.getInitialSet(), {r, e -> r - e})
        return variants
    }
    
    
    override protected fun getNewElement(
            text: String
            , elementFactory: PsiElementFactory
    ): PsiTypeParameter? {
        try {
            val newP = elementFactory.createTypeParameterFromText(text, null)
            return newP as? PsiTypeParameter
        } catch (e: Exception) {
            return null
        }
    }

    override protected fun updateSubtreeVariants(
            p       : PsiTypeParameter
            , tmplt   : PsiTemplateGen<PsiTypeParameter, SmartInsertPlace>
            , variants: Map<String, FormatSet>
            , context: VariantConstructionContext
    ): Map<String, FormatSet> {
        return variants
    }

    override protected fun prepareSubtreeVariants(
            p: PsiTypeParameter
            , context: VariantConstructionContext
    ): Map<String, FormatSet> {
        val variants = HashMap<String, FormatSet>()
    
        prepareName_identifierVariants(p, variants, context)
        prepareExtends_listVariants(p, variants, context)
        prepareAnnotationVariants(p, variants, context)
        
        
    
        return variants
    }

    override protected fun getTags(p: PsiTypeParameter): Set<String> {
        val set = HashSet<String>()
    
        if (p.getNameIdentifier() != null) { set.add(NAME_IDENTIFIER_TAG) }
        if (p.getExtendsList() != null) { set.add(EXTENDS_LIST_TAG) }
        if (p.getAnnotations() != null && !p.getAnnotations().isEmpty()) { set.add(ANNOTATION_TAG) }
        
        
    
        return set
    }

    override protected fun isTemplateSuitable(
            p: PsiTypeParameter
            , tmplt: PsiTemplateGen<PsiTypeParameter, SmartInsertPlace>
    ): Boolean {
        return true
    }

    override public fun getTemplateFromElement(newP: PsiTypeParameter): PsiTemplateGen<PsiTypeParameter, SmartInsertPlace>? {
        val insertPlaceMap = HashMap<String, SmartInsertPlace>()
        val negShift = -newP.getCorrectTextOffset()
    
        val text = newP.getText() ?: ""
    
        addName_identifierToInsertPlaceMap(newP, insertPlaceMap, negShift)
        if (!addExtends_listToInsertPlaceMap(newP, insertPlaceMap, negShift)) { return null }
        addAnnotationToInsertPlaceMap(newP, insertPlaceMap, negShift)
        
        
    
        val contentRelation = getContentRelation(newP.getText() ?: "", insertPlaceMap)
        return PsiTemplateGen(newP, insertPlaceMap, contentRelation.first, contentRelation.second)
    }

    
}