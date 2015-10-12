package org.jetbrains.printerGenerator

import org.jetbrains.printerGenerator.componentGenerator.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.ArrayList
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

public class StaXParser(elementFactory: String) {

    val factory            : String  = elementFactory
    var name               : String? = null
    var psiComponentClass  : String? = null
    var createFromText     : String? = null
    var isList             : String  = "false"
    var predecessors       : String? = null
    var specificImport     : String? = null
    var getNewElement      : String? = null
    var updateSubtrees     : String? = null
    var prepareSubtrees    : String? = null
    var getTags            : String? = null
    var isTemplateSuitable : String? = null
    var getTemplate        : String? = null
    var specificCode       : String? = null
    var isFile             : String = "false"

    private fun clean() {
        name               = null
        psiComponentClass  = null
        createFromText     = null
        isList             = "false"
        predecessors       = null
        specificImport     = null
        getNewElement      = null
        updateSubtrees     = null
        prepareSubtrees    = null
        getTags            = null
        isTemplateSuitable = null
        getTemplate        = null
        specificCode       = null
        isFile             = "false"
    }

    private fun getSpecificCode(startElement: StartElement) : String? {
        var specificCode : String? = null
        val attributes: Iterator<Any?> = startElement.getAttributes()
        while (attributes.hasNext()) {
            val attribute = attributes.next()
            if (attribute !is Attribute) { break }
            val value = attribute.getValue()
            when (attribute.getName().toString()) {
                "specificCode" -> specificCode = value
            }
        }
        return specificCode
    }

    private fun getSubtreeComponent(eventReader: XMLEventReader, startEvent: XMLEvent, psiComponentClass: String?): ComponentSubtree? {
        var name            : String? = null
        var psiGetMethod    : String? = null
        var isEverywhereSuit: String = "false"
        var hasSeveralElem  : String = "false"
        var isCodeBlock     : String? = null
        var isRequired      : String? = null
        var addCode         : String? = null
        var prepCode        : String? = null
        var getCode         : String? = null

        val startElement = startEvent.asStartElement()
        val attributes = startElement.getAttributes()
        while (attributes.hasNext()) {
            val attribute = attributes.next()
            if (attribute !is Attribute) { break }
            val value = attribute.getValue()
            when (attribute.getName().toString()) {
                "name"              -> name             = value
                "psiGetMethod"      -> psiGetMethod     = value
                "isEverywhereSuit"  -> isEverywhereSuit = value
                "hasSeveralElements"-> hasSeveralElem   = value
                "isCodeBlock"       -> isCodeBlock      = value
                "isRequired"        -> isRequired       = value
            }
        }

        while(eventReader.hasNext()) {
            val event = eventReader.nextEvent()

            if (event.isStartElement()) {

                val startElement = event.asStartElement()
                val localPart = startElement.getName().getLocalPart()

                if (localPart == "addCode") {
                    addCode = ComponentSubtreeAdd(name!!, psiGetMethod!!, isEverywhereSuit.toBoolean()
                            , getSpecificCode(startElement)).toString()
                }
                if (localPart == "prepCode") {
                    prepCode = ComponentSubtreePrep(name!!, getSpecificCode(startElement)).toString()
                }
                if (localPart == "getCode") {
                    var foldFunction : String? = null
                    var specificCode : String? = null
                    val attributes: Iterator<Any?> = startElement.getAttributes()
                    while (attributes.hasNext()) {
                        val attribute = attributes.next()
                        if (attribute !is Attribute) { break }
                        val value = attribute.getValue()
                        when (attribute.getName().toString()) {
                            "specificCode" -> specificCode = value
                            "foldFunction" -> foldFunction = value
                        }
                    }
                    getCode = ComponentSubtreeGet(name!!, psiGetMethod!!, hasSeveralElem.toBoolean()
                            , foldFunction, specificCode).toString()
                }
            }

            if (event.isEndElement()) {
                val endElement: EndElement = event.asEndElement();
                if (endElement.getName().getLocalPart() == "subtree") { break }
            }
        }

        return ComponentSubtree(
                name!!
                , psiComponentClass!!
                , hasSeveralElem.toBoolean()
                , psiGetMethod!!
                , isCodeBlock!!.toBoolean()
                , isRequired!!.toBoolean()
                , addCode ?: ComponentSubtreeAdd(name!!, psiGetMethod!!, isEverywhereSuit.toBoolean(), null).toString()
                , prepCode ?: ComponentSubtreePrep(name!!, null).toString()
                , getCode ?: ComponentSubtreeGet(name!!, psiGetMethod!!, false, null, null).toString()
        )
    }

    private fun getComponent(eventReader: XMLEventReader): Component? {

        val subtrees : ArrayList<ComponentSubtree> = ArrayList()

        while (eventReader.hasNext()) {
            val event = eventReader.nextEvent()

            if (event.isStartElement()) {
                val startElement = event.asStartElement()
                val localPart = startElement.getName().getLocalPart()

                if (localPart == "component") {
                    val attributes: Iterator<Any?> = startElement.getAttributes()
                    while (attributes.hasNext()) {
                        val attribute = attributes.next()
                        if (attribute !is Attribute) { break }
                        val value = attribute.getValue()
                        when (attribute.getName().toString()) {
                            "name"              -> name = value
                            "psiComponentClass" -> psiComponentClass = value
                            "createFromText"    -> createFromText = value
                            "isList"            -> isList = value
                            "predecessors"      -> predecessors = value
                            "specificImport"    -> specificImport = value
                            "isFile"            -> isFile = value
                        }
                    }
                }

                if (localPart == "getNewElement") {
                     getNewElement = ComponentNewElement(psiComponentClass!!, factory, createFromText, getSpecificCode(startElement)).toString()
                }

                if (localPart == "updateSubtrees") {
                    updateSubtrees = ComponentUpdateSubtrees(psiComponentClass!!, subtrees, getSpecificCode(startElement)).toString()
                }

                if (localPart == "prepareSubtrees") {
                    prepareSubtrees = ComponentPrepareSubtrees(psiComponentClass!!, subtrees, getSpecificCode(startElement)).toString()
                }

                if (localPart == "getTags") {
                     getTags = ComponentGetTags(psiComponentClass!!, subtrees, getSpecificCode(startElement)).toString()
                }

                if (localPart == "isTemplSuit") {
                    isTemplateSuitable = ComponentIsTemplSuit(
                            psiComponentClass!!, subtrees, getSpecificCode(startElement), isList).toString()
                }

                if (localPart == "getTemplate") {
                    getTemplate = ComponentGetTemplate(psiComponentClass!!, subtrees, getSpecificCode(startElement)).toString()
                }

                if (localPart == "specCode") {
                    specificCode = getSpecificCode(startElement)
                }

                if (localPart == "subtree") {
                    val subtree = getSubtreeComponent(eventReader, event, psiComponentClass)
                    if (subtree != null) { subtrees.add(subtree) }
                }
            }

            if (event.isEndElement()) {
                val endElement: EndElement = event.asEndElement();
                if (endElement.getName().getLocalPart() != "component") { continue }
                return Component(
                        name!!
                        , psiComponentClass!!
                        , isList
                        , predecessors
                        , specificImport
                        , subtrees
                        , getNewElement ?: ComponentNewElement(psiComponentClass!!, factory, createFromText!!, null).toString()
                        , updateSubtrees ?: ComponentUpdateSubtrees(psiComponentClass!!, subtrees, null).toString()
                        , prepareSubtrees ?: ComponentPrepareSubtrees(psiComponentClass!!, subtrees, null).toString()
                        , getTags ?: ComponentGetTags(psiComponentClass!!, subtrees, null).toString()
                        , isTemplateSuitable ?: ComponentIsTemplSuit(psiComponentClass!!, subtrees, null, isList).toString()
                        , getTemplate ?: ComponentGetTemplate(psiComponentClass!!, subtrees, null).toString()
                        , specificCode
                        , isFile
                )
            }
        }
        return null
    }

    public fun readXml(inputXml : String) : Component? {
        try {
            //val mImport = File(importList).readText()
            //File("resources/generators/ImportList.txt").writeText(mImport)

            val inputFactory = XMLInputFactory.newInstance()
            val mInput = FileInputStream(inputXml)
            val eventReader = inputFactory.createXMLEventReader(mInput)

            clean()

            return getComponent(eventReader)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: XMLStreamException) {
            e.printStackTrace()
        }
        return null
    }
}