package org.jetbrains.printerGenerator.componentGenerator

import org.jetbrains.prettyPrinter.core.util.string.replaceAllInsertPlace
import java.io.File

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