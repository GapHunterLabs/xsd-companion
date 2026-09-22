# XSD Companion

IntelliJ-family plugin. Go-to-definition and a real structure tree for
XSD/WSDL schemas split across multiple files.

## Why it exists

Multi-file schemas are where XSD/WSDL tooling tends to give up. The
long-standing paid option in this niche, XSD / WSDL Visualizer (36,897
downloads, freemium), collected reviews saying so:

- *"Pretty useless tool, not recognizing correlations between multiple
  xsd files in same folder..."* (June 2025)
- *"I need to visualize complex wsdl (xsd was splitted to more files) and
  this was not able to parse it."*
- *"Not optimized at all on big WSDL files as it is REALLY slow."*

Checked again on 2026-09-22: 9 of its 13 reviews are 3 stars or fewer,
and its 2026.1 release (March 2026) says "Tree View now supports
multi-file schemas" and "Single-file schemas are now free" -- so those
complaints are being worked on, and the comparison is dated on purpose
rather than left to imply a current state.

What this plugin does, regardless of that: the whole include/import
graph, `.xsd` and `.wsdl` alike, free, with no network access.

## Why built this way

- **Real content-based detection, not extension guessing.** A file is
  recognized as XSD/WSDL by its actual root-element namespace
  (`http://www.w3.org/2001/XMLSchema`, or WSDL 1.1's
  `http://schemas.xmlsoap.org/wsdl/` and WSDL 2.0's
  `http://www.w3.org/ns/wsdl`), so a split schema stored as
  plain `.xml` is still recognized, and a `.xsd`-named file with
  unrelated content never is.
- **Built on the bundled XML plugin's real PSI** (`XmlTag`/`XmlFile`/
  `XmlAttribute`), not a custom XML parser — the same "don't reimplement
  what the platform already ships" call made in every other plugin in
  this catalog. `com.intellij.modules.xml` is a core platform module,
  not an extra dependency to pull in.
- **Real go-to-definition for `schemaLocation`.** Ctrl+Click (or
  Ctrl+B) on any `schemaLocation` value in `<xs:include>`/`<xs:import>`/
  `<xs:redefine>`/`<xs:override>` (XSD 1.1's replacement for
  `redefine`) navigates to the real target file, resolved purely
  against local `VirtualFile`s (same pattern already proven in
  json-schema-companion/openapi-companion/asyncapi-companion). An
  unresolvable `schemaLocation` is flagged with a warning — the direct
  fix for "was not able to parse it."
- **The "XSD Structure" tool window resolves the whole include/import
  graph**, not just the file currently open — the direct fix for "not
  recognizing correlations between multiple xsd files." It follows the
  active editor automatically and lists every declared
  element/complexType/simpleType/attributeGroup/group across every file
  reached, with cycle-safe traversal (a schema importing itself back, or
  two files including each other, is a real and valid pattern — never
  an infinite loop here).
- **No network access, ever.** An `http(s)://` `schemaLocation` — a
  common namespace-hint form on `<xs:import>` — is never fetched, only
  ever shown as unresolved, same non-network stance as this catalog's
  other `$ref`/reference-resolution plugins.

## What a WSDL needs that an XSD does not

A WSDL is not simply "an XSD with extra elements", and both differences
matter for a multi-file schema:

- **The schema is nested.** It lives under `<wsdl:types>`, not at the
  root, so anything that reads only the root element's own children
  finds no declarations in a WSDL at all. A WSDL may also carry more
  than one `<xs:schema>`, one per namespace.
- **It splits with a different attribute.** `<wsdl:import>` carries
  `location`, not `schemaLocation`. The schema imports nested inside
  `<wsdl:types>` keep using `schemaLocation`, so a real WSDL project
  mixes both in one file.

Both are followed here, and `location` on an unrelated element (a
`<soap:address>` service endpoint, say) is deliberately not treated as a
file reference.

## Usage

Open any `.xsd`/`.wsdl` file (or a plain `.xml` file with a real XML
Schema/WSDL root). Ctrl+Click any `schemaLocation` value, or a
`<wsdl:import>`'s `location`, to jump to the referenced file. Open the
"XSD Structure" tool window (right side) to see the full resolved
structure across every included/imported file.

## Enterprise / Team Licensing

Need enterprise features, custom validation rules, or team licensing?
Contact us at **gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
