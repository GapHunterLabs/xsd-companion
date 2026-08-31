# XSD Companion

IntelliJ-family plugin. Go-to-definition and a real structure tree for
XSD/WSDL schemas split across multiple files.

## Why it exists

Born from real evidence in JetBrains Marketplace reviews of a paid
XSD/WSDL visualizer plugin (~$10/month, 69% of reviews at 3 stars or
fewer), not assumptions:

- "Pretty useless tool, not recognizing correlations between multiple
  xsd files in same folder... I consider this as a joke."
- "I need to visualize complex wsdl (xsd was splitted to more files) and
  this was not able to parse it."
- "Unusable with big / complex wsdl files."
- "Not optimized at all on big WSDL files as it is REALLY slow."
- "The pricing is way off compared to the benefit."

## Why built this way

- **Real content-based detection, not extension guessing.** A file is
  recognized as XSD/WSDL by its actual root-element namespace
  (`http://www.w3.org/2001/XMLSchema` or
  `http://schemas.xmlsoap.org/wsdl/`), so a split schema stored as
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

## Usage

Open any `.xsd`/`.wsdl` file (or a plain `.xml` file with a real XML
Schema/WSDL root). Ctrl+Click any `schemaLocation` value to jump to the
referenced file. Open the "XSD Structure" tool window (right side) to
see the full resolved structure across every included/imported file.

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
