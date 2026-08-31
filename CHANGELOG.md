<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Merge Conflict Leftover Companion Changelog

## [Unreleased]

## [0.2.0]

### Added

- Detects the `|||||||` common-ancestor marker Git adds when
  `merge.conflictStyle` is `diff3` or `zdiff3` (the latter has been
  Git's recommended default since 2.35) -- previously only the 3-way
  style's 3 markers were covered, silently missing this 4th one.

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real findings, a one-time
  notification asks whether to rate the plugin on Marketplace, with a
  permanent "Don't ask again" option. Standard mechanism used
  catalog-wide since 2026-08-24, rolled out
  to this plugin now.

## [0.1.0]

### Added

- Flags real Git merge-conflict markers (`<<<<<<<`, `=======`,
  `>>>>>>>`) left behind in any file, inline.
- 100% plain-text scan, any file type, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/merge-conflict-leftover-companion/compare/0.2.0...HEAD
[0.2.0]: https://github.com/GapHunterLabs/merge-conflict-leftover-companion/compare/0.1.1...0.2.0
[0.1.1]: https://github.com/GapHunterLabs/merge-conflict-leftover-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/merge-conflict-leftover-companion/commits/0.1.0
