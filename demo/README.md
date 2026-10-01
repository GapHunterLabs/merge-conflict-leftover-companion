# Demo project

`pricing.py` — a realistic file with a full leftover conflict block
(as if someone resolved the merge in a diff tool but forgot to remove
the markers before committing).

## Try it

1. `./gradlew runIde` from `merge-conflict-leftover-companion`, open
   this `demo/` folder as the project.
2. Open `pricing.py` — 3 inline warnings appear, one per marker line.

The GIFs in `docs/media/` also show the `diff3` style (the `|||||||`
common-ancestor marker) in a JavaScript file.
