<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# XSD Companion Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/xsd-companion/commits/0.1.0
