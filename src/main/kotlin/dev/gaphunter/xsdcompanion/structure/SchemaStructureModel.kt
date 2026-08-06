package dev.gaphunter.xsdcompanion.structure

/**
 * Pure, PSI-decoupled model of a schema's declared top-level components
 * (element/complexType/simpleType/attributeGroup/group), resolved across
 * every file reachable via `<xs:include>`/`<xs:import>`/`<xs:redefine>`.
 * Fully unit-testable with no platform bootstrap, same "pure model, thin
 * PSI-walking annotator" split already used in gitlab-ci-companion and
 * jenkinsfile-companion.
 */
data class SchemaComponent(
    val kind: String,
    val name: String,
    val filePath: String,
)

data class SchemaFileNode(
    val filePath: String,
    val components: List<SchemaComponent>,
    val includes: List<String>,
    val unresolved: List<String>,
)

/**
 * Walks the include/import/redefine graph starting from [rootPath],
 * using [resolve] to turn a (referencing file, schemaLocation) pair into
 * the referenced file's own path plus its declared components and further
 * references -- the actual PSI walking lives in the caller
 * ([dev.gaphunter.xsdcompanion.structure.SchemaStructureBuilder]), this
 * function only owns graph traversal and cycle safety.
 *
 * A schema legitimately importing itself back (a real, valid XSD pattern
 * for split namespaces) or two files including each other must never
 * cause infinite recursion -- [visited] guards every path exactly once.
 */
fun buildSchemaGraph(
    rootPath: String,
    resolve: (path: String) -> SchemaFileNode?,
): List<SchemaFileNode> {
    val visited = mutableSetOf<String>()
    val result = mutableListOf<SchemaFileNode>()
    val queue = ArrayDeque(listOf(rootPath))

    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()
        if (!visited.add(path)) continue
        val node = resolve(path) ?: continue
        result.add(node)
        for (include in node.includes) {
            if (include !in visited) queue.addLast(include)
        }
    }
    return result
}
