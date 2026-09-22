package dev.gaphunter.xsdcompanion.reference

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Ctrl+Click inside a WSDL: on the `schemaLocation` of a schema import
 * nested in `<wsdl:types>`, and on the `location` of a `<wsdl:import>`,
 * which is the attribute WSDL uses to split itself across files.
 */
class WsdlLocationNavigationTest : BasePlatformTestCase() {

    private fun attributeValue(file: PsiFile, name: String): XmlAttributeValue =
        PsiTreeUtil.findChildrenOfType(file, XmlAttribute::class.java)
            .first { it.localName == name }
            .valueElement!!

    private fun resolveOf(value: XmlAttributeValue) =
        value.references.filterIsInstance<SchemaLocationReference>().firstOrNull()?.resolve()

    fun testResolvesASchemaImportNestedInWsdlTypes() {
        val common = myFixture.addFileToProject(
            "common.xsd",
            """<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"/>""",
        )
        myFixture.configureByText(
            "service.wsdl",
            """
            <wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <wsdl:types>
                <xs:schema targetNamespace="urn:acme">
                  <xs:import namespace="urn:common" schemaLocation="common.xsd"/>
                </xs:schema>
              </wsdl:types>
            </wsdl:definitions>
            """.trimIndent(),
        )
        assertEquals(common, resolveOf(attributeValue(myFixture.file, "schemaLocation")))
    }

    fun testResolvesAWsdlImportLocation() {
        val types = myFixture.addFileToProject(
            "types.wsdl",
            """<wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"/>""",
        )
        myFixture.configureByText(
            "service.wsdl",
            """
            <wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
              <wsdl:import namespace="urn:acme" location="types.wsdl"/>
            </wsdl:definitions>
            """.trimIndent(),
        )
        assertEquals(types, resolveOf(attributeValue(myFixture.file, "location")))
    }

    fun testDoesNotTreatAnUnrelatedLocationAttributeAsAReference() {
        myFixture.configureByText(
            "service.wsdl",
            """
            <wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
                              xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/">
              <wsdl:service name="OrderService">
                <wsdl:port name="OrderPort" binding="tns:OrderBinding">
                  <soap:address location="http://acme.example.com/orders"/>
                </wsdl:port>
              </wsdl:service>
            </wsdl:definitions>
            """.trimIndent(),
        )
        // `location` on <soap:address> is a service endpoint, not a file.
        assertNull(resolveOf(attributeValue(myFixture.file, "location")))
        assertTrue(attributeValue(myFixture.file, "location").references.none { it is SchemaLocationReference })
    }

    fun testAnUnresolvableWsdlImportIsFlaggedByAttributeName() {
        myFixture.configureByText(
            "service.wsdl",
            """
            <wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
              <wsdl:import namespace="urn:acme" location="missing.wsdl"/>
            </wsdl:definitions>
            """.trimIndent(),
        )
        val warnings = myFixture.doHighlighting().filter { it.description != null }.map { it.description!! }
        assertTrue(
            "expected the warning to name `location`, got $warnings",
            warnings.any { it.contains("Cannot resolve location") && it.contains("missing.wsdl") },
        )
    }
}
