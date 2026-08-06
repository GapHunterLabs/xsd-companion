package dev.gaphunter.xsdcompanion.reference

import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

/**
 * Wires [SchemaLocationReference] up as a real, navigable go-to-definition
 * (Ctrl+Click / Ctrl+B) on `schemaLocation` values inside `<xs:include>`/
 * `<xs:import>`/`<xs:redefine>` tags of files recognized as XSD/WSDL by
 * [dev.gaphunter.xsdcompanion.detection.XsdWsdlDetector].
 */
class SchemaLocationReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                    val value = element as? XmlAttributeValue ?: return PsiReference.EMPTY_ARRAY
                    SchemaLocationUtil.asSchemaLocationValue(value) ?: return PsiReference.EMPTY_ARRAY
                    return arrayOf(SchemaLocationReference(value))
                }
            },
        )
    }
}
