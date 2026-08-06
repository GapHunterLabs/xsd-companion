package dev.gaphunter.xsdcompanion.structure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemaStructureModelTest {

    @Test
    fun `walks a simple include chain`() {
        val nodes = mapOf(
            "root.xsd" to SchemaFileNode("root.xsd", emptyList(), listOf("child.xsd"), emptyList()),
            "child.xsd" to SchemaFileNode("child.xsd", emptyList(), emptyList(), emptyList()),
        )

        val result = buildSchemaGraph("root.xsd") { nodes[it] }

        assertEquals(listOf("root.xsd", "child.xsd"), result.map { it.filePath })
    }

    @Test
    fun `does not infinite-loop on a two-file mutual include cycle`() {
        val nodes = mapOf(
            "a.xsd" to SchemaFileNode("a.xsd", emptyList(), listOf("b.xsd"), emptyList()),
            "b.xsd" to SchemaFileNode("b.xsd", emptyList(), listOf("a.xsd"), emptyList()),
        )

        val result = buildSchemaGraph("a.xsd") { nodes[it] }

        assertEquals(setOf("a.xsd", "b.xsd"), result.map { it.filePath }.toSet())
        assertEquals(2, result.size)
    }

    @Test
    fun `does not infinite-loop on a schema that imports itself`() {
        val nodes = mapOf(
            "self.xsd" to SchemaFileNode("self.xsd", emptyList(), listOf("self.xsd"), emptyList()),
        )

        val result = buildSchemaGraph("self.xsd") { nodes[it] }

        assertEquals(listOf("self.xsd"), result.map { it.filePath })
    }

    @Test
    fun `an unresolvable include is simply skipped, not an error`() {
        val nodes = mapOf(
            "root.xsd" to SchemaFileNode("root.xsd", emptyList(), listOf("missing.xsd"), emptyList()),
        )

        val result = buildSchemaGraph("root.xsd") { nodes[it] }

        assertEquals(listOf("root.xsd"), result.map { it.filePath })
    }

    @Test
    fun `collects components across the whole reachable graph`() {
        val nodes = mapOf(
            "root.xsd" to SchemaFileNode(
                "root.xsd",
                listOf(SchemaComponent("element", "Order", "root.xsd")),
                listOf("types.xsd"),
                emptyList(),
            ),
            "types.xsd" to SchemaFileNode(
                "types.xsd",
                listOf(SchemaComponent("complexType", "AddressType", "types.xsd")),
                emptyList(),
                emptyList(),
            ),
        )

        val result = buildSchemaGraph("root.xsd") { nodes[it] }
        val allComponents = result.flatMap { it.components }

        assertEquals(2, allComponents.size)
        assertTrue(allComponents.any { it.name == "Order" })
        assertTrue(allComponents.any { it.name == "AddressType" })
    }

    @Test
    fun `a diamond include shape visits the shared file once`() {
        // root includes both a.xsd and b.xsd, which both include shared.xsd
        val nodes = mapOf(
            "root.xsd" to SchemaFileNode("root.xsd", emptyList(), listOf("a.xsd", "b.xsd"), emptyList()),
            "a.xsd" to SchemaFileNode("a.xsd", emptyList(), listOf("shared.xsd"), emptyList()),
            "b.xsd" to SchemaFileNode("b.xsd", emptyList(), listOf("shared.xsd"), emptyList()),
            "shared.xsd" to SchemaFileNode("shared.xsd", emptyList(), emptyList(), emptyList()),
        )

        val result = buildSchemaGraph("root.xsd") { nodes[it] }

        assertEquals(4, result.size)
        assertEquals(1, result.count { it.filePath == "shared.xsd" })
    }
}
