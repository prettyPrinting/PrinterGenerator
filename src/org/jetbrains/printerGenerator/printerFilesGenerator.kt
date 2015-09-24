package org.jetbrains.printerGenerator

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.ArrayList
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement

data class ComponentData(val name : String?, val psiClass : String?, val fromText : String?)

public class PrinterFilesParser() {
    var specificImport     : String? = null
    var fileClassName      : String? = null
    var filePsiClass       : String? = null
    var defaultFromText    : String? = null

    private fun clean() {
        specificImport     = null
        fileClassName      = null
        filePsiClass       = null
        defaultFromText    = null
    }

    private fun getPrinter(eventReader: XMLEventReader): Printer? {

        val components : ArrayList<ComponentData> = ArrayList()

        while (eventReader.hasNext()) {
            val event = eventReader.nextEvent()

            if (event.isStartElement()) {
                val startElement = event.asStartElement()
                val localPart = startElement.getName().getLocalPart()

                if (localPart == "printer") {
                    val attributes: Iterator<Any?> = startElement.getAttributes()
                    while (attributes.hasNext()) {
                        val attribute = attributes.next()
                        if (attribute !is Attribute) { break }
                        val value = attribute.getValue()
                        when (attribute.getName().toString()) {
                            "specificImport"    -> specificImport = value
                            "defaultFromText"   -> defaultFromText = value
                        }
                    }
                }

                if (localPart == "file") {
                    val attributes: Iterator<Any?> = startElement.getAttributes()
                    while (attributes.hasNext()) {
                        val attribute = attributes.next()
                        if (attribute !is Attribute) { break }
                        val value = attribute.getValue()
                        when (attribute.getName().toString()) {
                            "fileClassName"   -> fileClassName = value
                            "filePsiClass"    -> filePsiClass = value
                        }
                    }
                }

                if (localPart == "component") {
                    var compName: String? = null
                    var psiClass: String? = null
                    var fromText: String? = null
                    val attributes: Iterator<Any?> = startElement.getAttributes()
                    while (attributes.hasNext()) {
                        val attribute = attributes.next()
                        if (attribute !is Attribute) { break }
                        val value = attribute.getValue()
                        when (attribute.getName().toString()) {
                            "name"           -> compName = value
                            "psiClass"       -> psiClass = value
                            "fromText"       -> fromText = value
                        }
                    }
                    val component = ComponentData(compName, psiClass, fromText)
                    components.add(component)
                }
            }

            if (event.isEndElement()) {
                val endElement: EndElement = event.asEndElement();
                if (endElement.getName().getLocalPart() != "printer") { continue }
                return Printer(
                          specificImport
                        , fileClassName
                        , filePsiClass
                        , defaultFromText
                        , components
                )
            }
        }
        return null
    }

    public fun readXml(inputXml : String) : Printer? {
        try {
            val inputFactory = XMLInputFactory.newInstance()
            val mInput = FileInputStream(inputXml)
            val eventReader = inputFactory.createXMLEventReader(mInput)

            clean()

            return getPrinter(eventReader)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: XMLStreamException) {
            e.printStackTrace()
        }
        return null
    }
}
