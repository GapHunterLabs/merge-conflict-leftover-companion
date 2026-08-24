# Merge Conflict Leftover Companion

Flags real Git merge-conflict markers (`<<<<<<<`, `=======`,
`>>>>>>>`) left behind in any file — a real, distinct problem from an
*active* unresolved merge (which the IDE's own Commit window already
flags in red): a merge that was already resolved and committed, but
with the markers themselves accidentally left in the file.

## Why it exists

This is a real, recurring mistake — a quick web search for "detect git
conflict markers" turns up multiple independent blog posts and CI
scripts written specifically to catch this after the fact, confirming
it's a known problem, not an imagined one. Nothing in the IDE flags it
proactively today.

## Why built this way

- **100% plain-text scan, any file type.** No PSI-per-language
  dependency, no network calls — registered without a `language`
  filter, same pattern as `env-var-missing-companion`.
- **Favors recall over a fragile exclusion heuristic.** Any file whose
  text happens to match the exact 7-character marker patterns is
  flagged, including documentation that cites the literal syntax as an
  example — a real, accepted trade-off rather than risk hiding a
  genuine leftover marker behind an imperfect filter.

## Usage

Open any file. A leftover conflict marker shows as an inline warning.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
