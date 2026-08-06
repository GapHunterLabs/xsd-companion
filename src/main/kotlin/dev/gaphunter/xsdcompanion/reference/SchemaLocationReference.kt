package dev.gaphunter.xsdcompanion.reference

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.xml.XmlAttributeValue

/**
 * Resolves a `schemaLocation="..."` value on `<xs:include>`/`<xs:import>`/
 * `<xs:redefine>` to the real target file, purely against local files --
 * same [VfsUtilCore.findRelativeFile] pattern already proven in
 * json-schema-companion/openapi-companion/asyncapi-companion's own `$ref`
 * resolution. No HTTP client anywhere in this plugin: an `http(s)://`
 * schemaLocation (common on `<xs:import namespace="...">` where
 * schemaLocation is only a hint, not a real fetch target) simply fails to
 * resolve rather than attempting a network fetch.
 */
class SchemaLocationReference(element: XmlAttributeValue) :
    PsiReferenceBase<XmlAttributeValue>(element, ElementManipulators.getValueTextRange(element)) {

    override fun resolve(): PsiElement? {
        val path = element.value
        if (path.isBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return null

        val currentVirtualFile = element.containingFile?.originalFile?.virtualFile ?: return null
        val baseDir = currentVirtualFile.parent ?: return null
        val targetVirtualFile = VfsUtilCore.findRelativeFile(path, baseDir) ?: return null
        val targetFile = PsiManager.getInstance(element.project).findFile(targetVirtualFile) ?: return null
        return targetFile
    }

    override fun getVariants(): Array<Any> = emptyArray()
}
