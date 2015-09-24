package org.jetbrains.PrinterGenerator.generators

import java.io.File

public class ComponentSubtreePrep (
        public val name : String
        , val specificCode : String?
)
{
    override public fun toString(): String{

        val prepCodeTemplate = File("resources/generators/ComponentSubtreePrep.txt").readText()

        val prepParameterList = listOf(
                Pair("@NAME_CC@"        , name.capitalize() ),
                Pair("@NAME@"           , name              ),
                Pair("@NAME_CAP@"       , name.toUpperCase())
        )

        return specificCode ?: prepCodeTemplate.replaceAllInsertPlace(prepParameterList)
    }
}