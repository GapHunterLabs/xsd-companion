<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# XSD Companion Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.3...HEAD
[0.1.3]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.2...0.1.3
[0.1.2]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/GapHunterLabs/xsd-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/xsd-companion/commits/0.1.0
