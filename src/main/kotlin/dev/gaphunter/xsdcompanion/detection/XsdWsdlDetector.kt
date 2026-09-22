package dev.gaphunter.xsdcompanion.detection

import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag

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
        // WSDL 2.0. Rare next to 1.1, but its root element is the only
        // thing that tells the two apart.
        "http://www.w3.org/ns/wsdl",
    )

    fun isXsdOrWsdl(file: XmlFile): Boolean {
        val root = file.rootTag ?: return false
        val ns = root.namespace
        return ns in XSD_NAMESPACES || ns in WSDL_NAMESPACES
    }

    fun isXsd(file: XmlFile): Boolean = file.rootTag?.namespace in XSD_NAMESPACES

    fun isWsdl(file: XmlFile): Boolean = file.rootTag?.namespace in WSDL_NAMESPACES

    fun isSchemaLocationTag(localName: String): Boolean =
        localName == "include" || localName == "import" || localName == "redefine" || localName == "override"

    /**
     * A WSDL splits across files with `<wsdl:import location="...">`,
     * which carries `location`, not `schemaLocation` -- the schema
     * elements nested under `<wsdl:types>` keep using `schemaLocation`.
     */
    fun locationAttributeFor(tag: XmlTag): String? = when {
        tag.namespace in WSDL_NAMESPACES && tag.localName == "import" -> "location"
        isSchemaLocationTag(tag.localName) -> "schemaLocation"
        else -> null
    }
}
