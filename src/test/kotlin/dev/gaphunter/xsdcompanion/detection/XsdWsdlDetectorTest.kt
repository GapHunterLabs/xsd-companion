package dev.gaphunter.xsdcompanion.detection

import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class XsdWsdlDetectorTest : BasePlatformTestCase() {

    fun testRecognizesARealXsdSchemaRoot() {
        myFixture.configureByText(
            "types.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:element name="Order" type="xs:string"/>
            </xs:schema>
            """.trimIndent(),
        )
        assertTrue(XsdWsdlDetector.isXsdOrWsdl(myFixture.file as XmlFile))
    }

    fun testRecognizesARealWsdlDefinitionsRoot() {
        myFixture.configureByText(
            "service.wsdl",
            """
            <wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
            </wsdl:definitions>
            """.trimIndent(),
        )
        assertTrue(XsdWsdlDetector.isXsdOrWsdl(myFixture.file as XmlFile))
    }

    fun testRecognizesAnXsdSchemaEvenWithAPlainXmlExtension() {
        // Content-based detection, not extension guessing -- the whole
        // point is that a split schema doesn't have to end in .xsd.
        myFixture.configureByText(
            "types.xml",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:element name="Order" type="xs:string"/>
            </xs:schema>
            """.trimIndent(),
        )
        assertTrue(XsdWsdlDetector.isXsdOrWsdl(myFixture.file as XmlFile))
    }

    fun testDoesNotRecognizeAnUnrelatedXmlFileNamedXsd() {
        myFixture.configureByText(
            "notASchema.xsd",
            """
            <config>
              <setting name="foo" value="bar"/>
            </config>
            """.trimIndent(),
        )
        assertFalse(XsdWsdlDetector.isXsdOrWsdl(myFixture.file as XmlFile))
    }

    fun testRecognizesSchemaLocationTagNames() {
        assertTrue(XsdWsdlDetector.isSchemaLocationTag("include"))
        assertTrue(XsdWsdlDetector.isSchemaLocationTag("import"))
        assertTrue(XsdWsdlDetector.isSchemaLocationTag("redefine"))
        assertTrue(XsdWsdlDetector.isSchemaLocationTag("override"))
        assertFalse(XsdWsdlDetector.isSchemaLocationTag("element"))
    }
}
