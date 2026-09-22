package dev.gaphunter.xsdcompanion.structure

import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

/**
 * The demo project as a tester actually opens it: files read from
 * `demo/` on disk, so the walkthrough in `demo/README.md` cannot drift
 * away from what the code does.
 */
class DemoProjectTest : BasePlatformTestCase() {

    private fun addDemoFile(name: String) =
        myFixture.addFileToProject(name, File("demo/$name").readText())

    private fun graphOf(root: XmlFile): List<SchemaFileNode> {
        val rootUrl = root.virtualFile.url
        return buildSchemaGraph(rootUrl) { url ->
            val file = if (url == rootUrl) root else SchemaStructureBuilder.loadFile(project, url)
            file?.let { SchemaStructureBuilder.buildNode(it) }
        }
    }

    fun testTheWsdlDemoWalksIntoItsImportedWsdlAndSharedXsd() {
        addDemoFile("common-types.xsd")
        addDemoFile("line-item.xsd")
        addDemoFile("order-types.wsdl")
        val service = addDemoFile("order-service.wsdl") as XmlFile

        val graph = graphOf(service)
        val files = graph.map { it.filePath.substringAfterLast('/') }.sorted()
        assertEquals(
            listOf("common-types.xsd", "order-service.wsdl", "order-types.wsdl"),
            files,
        )

        val components = graph.flatMap { it.components }.map { it.name }
        assertTrue("expected the elements declared in the WSDL itself", components.containsAll(listOf("GetOrderRequest", "GetOrderResponse")))
        assertTrue("expected the type from the imported WSDL", components.contains("OrderSummaryType"))
        assertTrue("expected a type from the shared XSD", components.contains("AddressType"))
    }

    fun testTheWsdlDemoReportsOnlyTheDeliberatelyMissingImport() {
        addDemoFile("common-types.xsd")
        addDemoFile("line-item.xsd")
        addDemoFile("order-types.wsdl")
        val service = addDemoFile("order-service.wsdl") as XmlFile

        val unresolved = graphOf(service).flatMap { it.unresolved }
        // The <soap:address location="http://..."> endpoint must not show
        // up here: it is not a file reference.
        assertEquals(listOf("not-here.xsd"), unresolved)
    }

    fun testTheXsdDemoStillWalksItsTwoIncludes() {
        addDemoFile("common-types.xsd")
        addDemoFile("line-item.xsd")
        val order = addDemoFile("order.xsd") as XmlFile

        val files = graphOf(order).map { it.filePath.substringAfterLast('/') }.sorted()
        assertEquals(listOf("common-types.xsd", "line-item.xsd", "order.xsd"), files)
    }
}
