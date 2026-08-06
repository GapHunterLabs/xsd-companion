package dev.gaphunter.xsdcompanion.structure

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import dev.gaphunter.xsdcompanion.detection.XsdWsdlDetector

private val COMPONENT_TAGS = setOf(
    "element", "complexType", "simpleType", "attributeGroup", "group",
)

/**
 * Walks real XML PSI (never a custom parser) to turn one [XmlFile] into a
 * [SchemaFileNode]: its own top-level declared components, plus the
 * schemaLocation targets of every `<xs:include>`/`<xs:import>`/
 * `<xs:redefine>` child so [buildSchemaGraph] can keep walking. Resolves
 * `schemaLocation` the exact same way
 * [dev.gaphunter.xsdcompanion.reference.SchemaLocationReference] does
 * (relative to the referencing file's own directory, local files only,
 * never the network) -- both must agree on what "resolves" means, or
 * go-to-definition and the structure tree would disagree about which
 * files are actually reachable.
 *
 * [SchemaFileNode.filePath] is really a [com.intellij.openapi.vfs.VirtualFile.getUrl]
 * (protocol-qualified, e.g. `file:///...` or `temp:///...`), not a bare
 * filesystem path -- [loadFile] round-trips it back to a [XmlFile] via
 * [VirtualFileManager.findFileByUrl], which is filesystem-agnostic. A
 * bare path only round-trips through [com.intellij.openapi.vfs.LocalFileSystem],
 * which never sees the in-memory temp filesystem test fixtures
 * (`myFixture.addFileToProject`) use -- using the real URL is what makes
 * the graph walk work identically in tests and in a real IDE.
 */
object SchemaStructureBuilder {

    fun buildNode(file: XmlFile): SchemaFileNode? {
        val root = file.rootTag ?: return null
        val url = file.virtualFile?.url ?: return null

        val components = root.subTags
            .filter { it.localName in COMPONENT_TAGS }
            .mapNotNull { tag ->
                val name = tag.getAttributeValue("name") ?: return@mapNotNull null
                SchemaComponent(kind = tag.localName, name = name, filePath = url)
            }

        val locationTags = root.subTags.filter { XsdWsdlDetector.isSchemaLocationTag(it.localName) }
        val includes = mutableListOf<String>()
        val unresolved = mutableListOf<String>()
        for (tag in locationTags) {
            val location = tag.getAttributeValue("schemaLocation") ?: continue
            if (location.startsWith("http://") || location.startsWith("https://")) continue
            val resolved = resolveRelative(file, location)
            if (resolved != null) includes.add(resolved) else unresolved.add(location)
        }

        return SchemaFileNode(filePath = url, components = components, includes = includes, unresolved = unresolved)
    }

    /** Resolves a [file]-relative [location] to a real file's URL, local-only. */
    private fun resolveRelative(file: XmlFile, location: String): String? {
        val baseDir = file.virtualFile?.parent ?: return null
        val target = VfsUtilCore.findRelativeFile(location, baseDir) ?: return null
        return target.url
    }

    /** Loads the [XmlFile] at [url] within [project], if it's still a recognized XSD/WSDL file. */
    fun loadFile(project: Project, url: String): XmlFile? {
        val vFile = VirtualFileManager.getInstance().findFileByUrl(url) ?: return null
        val psiFile = PsiManager.getInstance(project).findFile(vFile) as? XmlFile ?: return null
        if (!XsdWsdlDetector.isXsdOrWsdl(psiFile)) return null
        return psiFile
    }
}
