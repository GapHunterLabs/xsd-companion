package dev.gaphunter.xsdcompanion.structure

import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SchemaStructureBuilderTest : BasePlatformTestCase() {

    fun testCollectsTopLevelComponentsFromTheRootFile() {
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:element name="Order" type="xs:string"/>
              <xs:complexType name="OrderType"/>
            </xs:schema>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        val names = node!!.components.map { it.name }
        assertEquals(listOf("Order", "OrderType"), names)
    }

    fun testCollectsIncludeTargetsAsResolvedPaths() {
        val typesFile = myFixture.addFileToProject(
            "types.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"/>
            """.trimIndent(),
        )
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="types.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        assertEquals(1, node!!.includes.size)
        assertTrue(node.includes.first().endsWith("types.xsd"))
        assertEquals(typesFile.virtualFile.url, node.includes.first())
    }

    fun testReportsAnUnresolvableIncludeInsteadOfThrowing() {
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="doesNotExist.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        assertEquals(listOf("doesNotExist.xsd"), node!!.unresolved)
        assertTrue(node.includes.isEmpty())
    }

    fun testHttpImportIsNeitherIncludedNorReportedAsUnresolved() {
        // http(s) schemaLocations are a hint, not a real fetch target --
        // this plugin never tries to resolve them locally OR reports them
        // as broken, same non-network stance as SchemaLocationReference.
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:import namespace="urn:example" schemaLocation="https://example.com/schema.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        val node = SchemaStructureBuilder.buildNode(myFixture.file as XmlFile)
        assertNotNull(node)
        assertTrue(node!!.includes.isEmpty())
        assertTrue(node.unresolved.isEmpty())
    }

    fun testFullGraphWalkAcrossThreeFilesCollectsAllComponents() {
        myFixture.addFileToProject(
            "b.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:simpleType name="IdType"/>
            </xs:schema>
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "a.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="b.xsd"/>
              <xs:complexType name="AddressType"/>
            </xs:schema>
            """.trimIndent(),
        )
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="a.xsd"/>
              <xs:element name="Order" type="xs:string"/>
            </xs:schema>
            """.trimIndent(),
        )

        val rootFile = myFixture.file as XmlFile
        val rootUrl = rootFile.virtualFile.url
        val cache = mutableMapOf<String, SchemaFileNode>()
        val graph = buildSchemaGraph(rootUrl) { url ->
            cache[url]?.let { return@buildSchemaGraph it }
            val file = if (url == rootUrl) rootFile else SchemaStructureBuilder.loadFile(project, url)
            val node = file?.let { SchemaStructureBuilder.buildNode(it) }
            if (node != null) cache[url] = node
            node
        }

        assertEquals(3, graph.size)
        val allNames = graph.flatMap { it.components }.map { it.name }.toSet()
        assertEquals(setOf("Order", "AddressType", "IdType"), allNames)
    }
}
