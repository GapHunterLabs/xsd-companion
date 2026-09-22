package dev.gaphunter.xsdcompanion.structure

import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * A WSDL keeps its schema one level down, inside `<wsdl:types>`, and
 * splits across files through `<wsdl:import location="...">` rather than
 * `schemaLocation`. Both are the shape this plugin says it handles ("XSD
 * and WSDL schemas split across multiple files"), so both are pinned
 * here.
 */
class WsdlStructureTest : BasePlatformTestCase() {

    private val wsdlHeader =
        """<wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema">"""

    fun testCollectsComponentsDeclaredInsideWsdlTypes() {
        myFixture.configureByText(
            "service.wsdl",
            """
            $wsdlHeader
              <wsdl:types>
                <xs:schema targetNamespace="urn:acme">
                  <xs:element name="GetOrderRequest" type="xs:string"/>
                  <xs:complexType name="OrderType"/>
                </xs:schema>
              </wsdl:types>
              <wsdl:message name="GetOrderIn"/>
            </wsdl:definitions>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        assertEquals(listOf("GetOrderRequest", "OrderType"), node!!.components.map { it.name })
    }

    fun testFollowsAnImportDeclaredInsideWsdlTypes() {
        val types = myFixture.addFileToProject(
            "common.xsd",
            """<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"/>""",
        )
        myFixture.configureByText(
            "service.wsdl",
            """
            $wsdlHeader
              <wsdl:types>
                <xs:schema targetNamespace="urn:acme">
                  <xs:import namespace="urn:common" schemaLocation="common.xsd"/>
                </xs:schema>
              </wsdl:types>
            </wsdl:definitions>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        assertEquals(listOf(types.virtualFile.url), node!!.includes)
    }

    fun testFollowsWsdlImportWhichUsesLocationNotSchemaLocation() {
        val other = myFixture.addFileToProject(
            "types.wsdl",
            """<wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"/>""",
        )
        myFixture.configureByText(
            "service.wsdl",
            """
            $wsdlHeader
              <wsdl:import namespace="urn:acme" location="types.wsdl"/>
            </wsdl:definitions>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        assertEquals(listOf(other.virtualFile.url), node!!.includes)
    }

    fun testReportsAnUnresolvableWsdlImportLocation() {
        myFixture.configureByText(
            "service.wsdl",
            """
            $wsdlHeader
              <wsdl:import namespace="urn:acme" location="missing.wsdl"/>
            </wsdl:definitions>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        assertEquals(listOf("missing.wsdl"), node!!.unresolved)
    }

    fun testASchemaSplitAcrossAWsdlAndTwoXsdFilesIsWalkedWhole() {
        myFixture.addFileToProject(
            "line-item.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:complexType name="LineItemType"/>
            </xs:schema>
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "common.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="line-item.xsd"/>
              <xs:simpleType name="CurrencyCode"/>
            </xs:schema>
            """.trimIndent(),
        )
        myFixture.configureByText(
            "service.wsdl",
            """
            $wsdlHeader
              <wsdl:types>
                <xs:schema targetNamespace="urn:acme">
                  <xs:import namespace="urn:common" schemaLocation="common.xsd"/>
                  <xs:element name="GetOrderRequest" type="xs:string"/>
                </xs:schema>
              </wsdl:types>
            </wsdl:definitions>
            """.trimIndent(),
        )
        val root = myFixture.file as XmlFile
        val rootUrl = root.virtualFile.url
        val graph = buildSchemaGraph(rootUrl) { url ->
            val file = if (url == rootUrl) root else SchemaStructureBuilder.loadFile(project, url)
            file?.let { SchemaStructureBuilder.buildNode(it) }
        }
        val components = graph.flatMap { it.components }.map { it.name }.sorted()
        assertEquals(listOf("CurrencyCode", "GetOrderRequest", "LineItemType"), components)
    }
}
