# Demo data for screenshots

`pricing.py` — a realistic file with a full leftover conflict block
(as if someone resolved the merge in a diff tool but forgot to remove
the markers before committing).

## How to get the screenshot

1. `./gradlew runIde` from `merge-conflict-leftover-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `pricing.py` — 3 inline warnings should appear
   (one per marker line).
3. Screenshot with all 3 warnings visible, save into
   `merge-conflict-leftover-companion/docs/screenshots/`. Close the
   sandbox.
