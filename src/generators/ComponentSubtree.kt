package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentSubtree (
        public val name : String
        , val psiComponentClass : String
        , val hasSeveralElem : Boolean
        , val psiSubtreeGet : String
        , val isCodeBlock : Boolean
        , val isRequired : Boolean
        , val addCode : String
        , val prepCode : String
        , val getCode : String
)
{
    override public fun toString(): String{

        val subtreeCodeTemplate = File("resources/generators/ComponentSubtree.txt").readText()

        val parametersList = listOf(
                Pair("@NAME_CC@"        , name.capitalize() ),
                Pair("@COMP_CLASS@"     , psiComponentClass ),
                Pair("@ADD_SUBTREE@"    , addCode           ),
                Pair("@PREP_SUBTREE@"   , prepCode          ),
                Pair("@GET_SUBTREE@"    , getCode           )
        )

        return subtreeCodeTemplate.replaceAllInsertPlace(parametersList)
    }
}