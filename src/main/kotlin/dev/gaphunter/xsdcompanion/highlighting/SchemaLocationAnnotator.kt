package dev.gaphunter.xsdcompanion.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import dev.gaphunter.xsdcompanion.reference.SchemaLocationReference
import dev.gaphunter.xsdcompanion.reference.SchemaLocationUtil

/**
 * Flags a `schemaLocation` value that fails to resolve to a real local
 * file -- real, visible feedback for exactly the "can't resolve schemas
 * split across multiple files" complaint this plugin exists to fix, on
 * top of the go-to-definition navigation
 * [dev.gaphunter.xsdcompanion.reference.SchemaLocationReferenceContributor]
 * already provides for valid ones. `http(s)://` values (a common hint-only
 * form on `<xs:import>`) are never flagged -- this plugin never resolves
 * or fetches over the network, so a URI-form schemaLocation is expected to
 * stay unresolved rather than being treated as broken.
 */
class SchemaLocationAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val value = element as? XmlAttributeValue ?: return
        SchemaLocationUtil.asSchemaLocationValue(value) ?: return
        if (value.value.startsWith("http://") || value.value.startsWith("https://")) return
        if (value.value.isBlank()) return

        if (SchemaLocationReference(value).resolve() == null) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Cannot resolve schemaLocation '${value.value}'")
                .range(value.textRange)
                .create()
        }
    }
}
