package dev.gaphunter.xsdcompanion.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Real PSI, through the full extension pipeline (annotator registered on
 * language="XML", gated by the real
 * [dev.gaphunter.xsdcompanion.reference.SchemaLocationUtil] check) --
 * `myFixture.doHighlighting()`, same discipline as jenkinsfile-companion's
 * own annotator test.
 */
class SchemaLocationAnnotatorTest : BasePlatformTestCase() {

    private fun warnings(): List<String> =
        myFixture.doHighlighting()
            .filter { it.description != null }
            .map { it.description!! }

    fun testNoWarningForAResolvableInclude() {
        myFixture.addFileToProject(
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
        assertTrue(
            "expected no unresolved-schemaLocation warning, got: ${warnings()}",
            warnings().none { it.contains("Cannot resolve schemaLocation") },
        )
    }

    fun testFlagsAnUnresolvableInclude() {
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="doesNotExist.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        assertTrue(warnings().any { it.contains("Cannot resolve schemaLocation") && it.contains("doesNotExist.xsd") })
    }

    fun testNeverFlagsAnHttpSchemaLocationAsBroken() {
        myFixture.configureByText(
            "root.xsd",
            """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:import namespace="urn:example" schemaLocation="https://example.com/schema.xsd"/>
            </xs:schema>
            """.trimIndent(),
        )
        assertTrue(
            "an http(s) schemaLocation must never be flagged as broken -- this plugin never fetches it",
            warnings().none { it.contains("Cannot resolve schemaLocation") },
        )
    }

    fun testDoesNotAnnotateOutsideARecognizedXsdFile() {
        myFixture.configureByText(
            "notASchema.xml",
            """
            <config>
              <include schemaLocation="doesNotExist.xml"/>
            </config>
            """.trimIndent(),
        )
        assertTrue(warnings().none { it.contains("Cannot resolve schemaLocation") })
    }
}
