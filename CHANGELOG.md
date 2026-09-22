<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# XSD Companion Changelog

## [Unreleased]

## [0.3.0]

### Fixed

- **A WSDL showed nothing in the XSD Structure tool window.** A WSDL
  keeps its schema one level down, inside `<wsdl:types>`, and the tool
  window only ever read the root element's own children -- so for a
  `.wsdl` file it found no declarations and followed no imports, which
  is exactly the multi-file WSDL case this plugin is for. It now finds
  every `<xs:schema>` in the file, including the several a WSDL may
  carry, one per namespace.
- **A WSDL split across WSDL files was not followed.** `<wsdl:import>`
  carries `location`, not `schemaLocation`, so those references were
  invisible to go-to-definition, to the unresolved-reference warning and
  to the structure graph. All three now handle it, while `location` on
  an unrelated element (a `<soap:address>` service endpoint, for
  instance) is still left alone.
- The unresolved-reference warning names the attribute that actually
  failed, instead of always saying `schemaLocation`.
- WSDL 2.0 (`http://www.w3.org/ns/wsdl`) is recognized alongside WSDL
  1.1.

### Added

- Demo files for the WSDL case (`order-service.wsdl` +
  `order-types.wsdl`), a walkthrough in `demo/README.md`, and tests that
  read those demo files from disk so the walkthrough cannot drift from
  what the code does.

## [0.2.0]

### Added

- Recognizes `<xs:override>` (XSD 1.1's replacement for `<xs:redefine>`)
  as a `schemaLocation`-bearing element -- go-to-definition, the
  unresolved-reference warning, and the XSD Structure tool window's
  graph traversal all now cover it, alongside the existing
  include/import/redefine.

## [0.1.3]

### Added

- Review/star CTA: after 10 distinct real unresolved `schemaLocation`
  findings, a one-time notification asks whether to rate the plugin on
  Marketplace, with a permanent "Don't ask again" option. Standard
  mechanism used catalog-wide since 2026-08-24, rolled out to this
  plugin now.

## [0.1.2]

### Fixed

- Tool window no longer shows the generic platform icon in the sidebar —
  the real Gap Hunter Labs mark is now declared via `icon=` on
  `<toolWindow>`.

## [0.1.1]

### Fixed

- "XSD Structure" tool window content (the structure tree) was
  rendering flush against the tool window's own border, with no margin
  — fixed with an 8px empty border on the root panel.

## [0.1.0]

### Added

- Go-to-definition (Ctrl+Click / Ctrl+B) for `schemaLocation` values in
  `<xs:include>`/`<xs:import>`/`<xs:redefine>`, resolved entirely
  against local files. Unresolvable locations are flagged with a
  warning.
- "XSD Structure" tool window: a structure tree resolved across the
  whole include/import/redefine graph, not just the file currently
  open, cycle-safe for schemas that import each other or themselves.
- Real content-based XSD/WSDL detection (root-element namespace), not
  file-extension guessing.
- No telemetry, no license prompts, no network access.

[Unreleased]: https://github.com/GapHunterLabs/xsd-companion/compare/0.3.0...HEAD
[0.3.0]: https://github.com/GapHunterLabs/xsd-companion/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.3...0.2.0
[0.1.3]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.2...0.1.3
[0.1.2]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/xsd-companion/commits/0.1.0
