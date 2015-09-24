package org.jetbrains.printerGenerator

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.HashMap
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.events.Attribute

class LanguageInfo private constructor(inputXml: String) {

    var language: String = ""
    var langPackage: String = ""
    var factory: String = ""
    var factoryPackage: String = ""
    var psiPackage: String = ""

    companion object {
        private var mInstance: LanguageInfo? = null
        public fun getInstance(inputXml: String): LanguageInfo? {
            if (mInstance == null) {
                mInstance = LanguageInfo(inputXml)
            }
            return mInstance
        }

        public fun getInstance(): LanguageInfo? {
            if (mInstance != null) {
                return mInstance
            } else {
                return null
            }
        }
    }

    init {
        readXml(inputXml)
    }

    private fun getLangInfo(eventReader: XMLEventReader) {
        while(eventReader.hasNext()) {
            val event = eventReader.nextEvent()
            if (event.isStartElement()) {
                val startElement = event.asStartElement()
                val localPart = startElement.getName().getLocalPart()

                if (localPart == "language") {
                    val attrs: Iterator<Any?> = startElement.getAttributes()
                    while (attrs.hasNext()) {
                        val attr = attrs.next()
                        if (attr !is Attribute) continue;
                        val value = attr.getValue()
                        when (attr.getName().toString()) {
                            "language" -> language = value
                            "langPackage" -> langPackage = value
                            "factory" -> factory = value
                            "factoryPackage" -> factoryPackage = value
                            "psiPackage" -> psiPackage = value
                        }
                    }
                }
            }
        }
    }

    private fun clean() {
        language = ""
        langPackage = ""
        factory = ""
        factoryPackage = ""
        psiPackage = ""
    }

    private fun readXml(inputXml: String): Map<String, String>? {
        try {
            val inputFactory = XMLInputFactory.newInstance()
            val mInput = FileInputStream(inputXml)
            val eventReader = inputFactory.createXMLEventReader(mInput)
            getLangInfo(eventReader)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: XMLStreamException) {
            e.printStackTrace()
        }
        return null
    }
}
