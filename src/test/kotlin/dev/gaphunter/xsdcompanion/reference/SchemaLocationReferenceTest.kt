package dev.gaphunter.xsdcompanion.reference

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Real PSI resolution across 2+ fixture files -- the direct regression
 * test for the competitor's central complaint ("not recognizing
 * correlations between multiple xsd files in same folder", "was not able
 * to parse it" on multi-file WSDL).
 */
class SchemaLocationReferenceTest : BasePlatformTestCase() {

    private fun findSchemaLocationValue(file: PsiFile): XmlAttributeValue =
        PsiTreeUtil.findChildrenOfType(file, XmlAttribute::class.java)
            .first { it.localName == "schemaLocation" }
            .valueElement!!

    private fun ourReference(value: XmlAttributeValue): SchemaLocationReference? =
        value.references.filterIsInstance<SchemaLocationReference>().firstOrNull()

    fun testResolvesIncludeAcrossTwoFiles() {
        myFixture.addFileToProject(
            "types.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:complexType name="AddressType"/>
            </xs:schema>
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
        val value = findSchemaLocationValue(myFixture.file)
        val resolved = ourReference(value)?.resolve()
        assertNotNull("expected schemaLocation to resolve to the real file", resolved)
        assertEquals("types.xsd", resolved!!.containingFile.name)
    }

    fun testResolvesImportAcrossTwoFiles() {
        myFixture.addFileToProject(
            "common.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:simpleType name="IdType"/>
            </xs:schema>
            """.trimIndent(),
        )
        myFixture.configureByText(
            "order.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:import schemaLocation="common.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        val value = findSchemaLocationValue(myFixture.file)
        assertNotNull(ourReference(value)?.resolve())
    }

    fun testResolvesASubdirectoryPath() {
        myFixture.addFileToProject(
            "shared/types.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:complexType name="AddressType"/>
            </xs:schema>
            """.trimIndent(),
        )
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="shared/types.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        val value = findSchemaLocationValue(myFixture.file)
        assertNotNull(ourReference(value)?.resolve())
    }

    fun testDoesNotResolveWhenTargetFileIsMissing() {
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="doesNotExist.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        val value = findSchemaLocationValue(myFixture.file)
        val reference = ourReference(value)
        assertNotNull("expected our reference to still be attached", reference)
        assertNull(reference!!.resolve())
    }

    fun testNeverResolvesAnHttpSchemaLocation() {
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:import namespace="urn:example" schemaLocation="https://example.com/schema.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        val value = findSchemaLocationValue(myFixture.file)
        val reference = ourReference(value)
        assertNotNull(reference)
        assertNull("an http(s) schemaLocation must never resolve via network access", reference!!.resolve())
    }

    fun testDoesNotAttachOurReferenceOutsideARecognizedXsdFile() {
        myFixture.configureByText(
            "notASchema.xml",
            """
            <config>
              <include schemaLocation="other.xml"/>
            </config>
            """.trimIndent(),
        )
        val value = findSchemaLocationValue(myFixture.file)
        assertNull(ourReference(value))
    }
}
