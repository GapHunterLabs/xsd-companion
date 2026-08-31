package dev.gaphunter.xsdcompanion.detection

import com.intellij.psi.xml.XmlFile

/**
 * Detects XSD/WSDL files by real root-element namespace, never by file
 * extension alone -- a `.xml` file with an `xs:schema`/`wsdl:definitions`
 * root is just as valid as one named `.xsd`/`.wsdl`, and a `.xsd`-named
 * file with unrelated content should never be treated as a schema.
 */
object XsdWsdlDetector {

    private val XSD_NAMESPACES = setOf(
        "http://www.w3.org/2001/XMLSchema",
        "http://www.w3.org/1999/XMLSchema",
    )
    private val WSDL_NAMESPACES = setOf(
        "http://schemas.xmlsoap.org/wsdl/",
    )

    fun isXsdOrWsdl(file: XmlFile): Boolean {
        val root = file.rootTag ?: return false
        val ns = root.namespace
        return ns in XSD_NAMESPACES || ns in WSDL_NAMESPACES
    }

    fun isSchemaLocationTag(localName: String): Boolean =
        localName == "include" || localName == "import" || localName == "redefine" || localName == "override"
}
