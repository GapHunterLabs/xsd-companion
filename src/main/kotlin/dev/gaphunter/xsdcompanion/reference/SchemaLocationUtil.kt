package dev.gaphunter.xsdcompanion.reference

import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import dev.gaphunter.xsdcompanion.detection.XsdWsdlDetector

/** Shared "is this a `schemaLocation` value inside a recognized XSD/WSDL file" check. */
object SchemaLocationUtil {

    fun asSchemaLocationValue(element: XmlAttributeValue): XmlAttributeValue? {
        val attribute = element.parent as? XmlAttribute ?: return null
        if (attribute.localName != "schemaLocation") return null

        val tag = attribute.parent ?: return null
        if (!XsdWsdlDetector.isSchemaLocationTag(tag.localName)) return null

        val file = element.containingFile as? XmlFile ?: return null
        if (!XsdWsdlDetector.isXsdOrWsdl(file)) return null

        return element
    }
}
