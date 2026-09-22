package dev.gaphunter.xsdcompanion.reference

import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import dev.gaphunter.xsdcompanion.detection.XsdWsdlDetector

/**
 * Shared "does this attribute value point at another schema file" check:
 * `schemaLocation` on `<xs:include>`/`<xs:import>`/`<xs:redefine>`/
 * `<xs:override>`, and `location` on `<wsdl:import>`, inside a file
 * recognized as XSD or WSDL.
 */
object SchemaLocationUtil {

    fun asSchemaLocationValue(element: XmlAttributeValue): XmlAttributeValue? {
        val attribute = element.parent as? XmlAttribute ?: return null
        val tag = attribute.parent ?: return null
        if (attribute.localName != XsdWsdlDetector.locationAttributeFor(tag)) return null

        val file = element.containingFile as? XmlFile ?: return null
        if (!XsdWsdlDetector.isXsdOrWsdl(file)) return null

        return element
    }
}
