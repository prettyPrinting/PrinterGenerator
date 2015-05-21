package org.jetbrains.PrinterGenerator.generators

import java.io.File

/**
 * Created by Aleksei on 3/19/2015.
 */
public class ComponentNewElement (
        val psiComponentClass: String
        , val elementFactory : String
        , val createFromText : String?
        , val specificCode   : String?
)
{
    override public fun toString(): String{

        val newElemCodeTemplate =
                if (specificCode == null)   { File("resources/generators/ComponentNewElement.txt").readText() }
                else                        { File("resources/generators/ComponentNewElementSpecCode.txt").readText() }

        val newElemParametersList = listOf(
                Pair("@COMP_CLASS@"     , psiComponentClass                     ),
                Pair("@ELEMENT_FACTORY@", elementFactory                        ),
                Pair("@FROM_TEXT@"      , createFromText?.capitalize() ?: ""    ),
                Pair("@SPECIFIC_CODE@"  , specificCode ?: ""                    )
        )

        return newElemCodeTemplate.replaceAllInsertPlace(newElemParametersList)
    }
}