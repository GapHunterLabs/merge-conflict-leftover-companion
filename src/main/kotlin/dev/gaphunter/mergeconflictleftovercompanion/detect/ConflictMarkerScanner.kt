package dev.gaphunter.mergeconflictleftovercompanion.detect

/** One conflict marker line found: [startOffset]/[endOffset] cover the whole line (no trailing newline), [label] is what the message should call it. */
data class ConflictMarkerHit(val startOffset: Int, val endOffset: Int, val label: String)

/**
 * Finds real Git merge-conflict markers (`<<<<<<<`, `=======`,
 * `>>>>>>>`) left behind in a file's text -- the real, distinct problem
 * from an *active* unresolved merge (which the IDE's own Commit window
 * already flags in red): a merge that was already resolved and
 * committed, but with the markers themselves accidentally left in the
 * file (a real, recurring mistake -- confirmed by multiple independent
 * "how to catch this" blog posts/CI scripts, not an imagined problem).
 *
 * **v0.1 scope, stated honestly:** any file containing text that
 * happens to match these exact 7-character marker patterns at the
 * start of a line is flagged, including documentation/tutorials that
 * cite the literal marker syntax as an example -- a real, accepted
 * false-positive rate for genuine "about git conflicts" content, same
 * trade-off this catalog already accepts elsewhere (favoring recall
 * over a fragile exclusion heuristic that could hide a real leftover
 * marker).
 */
object ConflictMarkerScanner {

    private val START_MARKER = Regex("""^<{7}(?!<).*$""", RegexOption.MULTILINE)
    private val MIDDLE_MARKER = Regex("""^={7}$""", RegexOption.MULTILINE)
    private val END_MARKER = Regex("""^>{7}(?!>).*$""", RegexOption.MULTILINE)

    fun scan(text: String): List<ConflictMarkerHit> {
        val hits = mutableListOf<ConflictMarkerHit>()
        for (match in START_MARKER.findAll(text)) hits += ConflictMarkerHit(match.range.first, match.range.last + 1, "<<<<<<< (conflict start)")
        for (match in MIDDLE_MARKER.findAll(text)) hits += ConflictMarkerHit(match.range.first, match.range.last + 1, "======= (conflict separator)")
        for (match in END_MARKER.findAll(text)) hits += ConflictMarkerHit(match.range.first, match.range.last + 1, ">>>>>>> (conflict end)")
        return hits.sortedBy { it.startOffset }
    }
}
