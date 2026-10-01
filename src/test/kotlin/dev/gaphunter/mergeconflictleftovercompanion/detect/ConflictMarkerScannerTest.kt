package dev.gaphunter.mergeconflictleftovercompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConflictMarkerScannerTest {

    @Test
    fun `a full conflict block is found as 3 separate hits`() {
        val text = """
            fun main() {
            <<<<<<< HEAD
                println("mine")
            =======
                println("theirs")
            >>>>>>> feature-branch
            }
        """.trimIndent()

        val hits = ConflictMarkerScanner.scan(text)
        assertEquals(3, hits.size)
        assertTrue(hits[0].label.startsWith("<<<<<<<"))
        assertTrue(hits[1].label.startsWith("======="))
        assertTrue(hits[2].label.startsWith(">>>>>>>"))
    }

    @Test
    fun `a diff3-style conflict block is found as 4 separate hits including the base marker`() {
        val text = """
            fun main() {
            <<<<<<< HEAD
                println("mine")
            ||||||| merged common ancestors
                println("original")
            =======
                println("theirs")
            >>>>>>> feature-branch
            }
        """.trimIndent()

        val hits = ConflictMarkerScanner.scan(text)
        assertEquals(4, hits.size)
        assertTrue(hits[0].label.startsWith("<<<<<<<"))
        assertTrue(hits[1].label.startsWith("|||||||"))
        assertTrue(hits[2].label.startsWith("======="))
        assertTrue(hits[3].label.startsWith(">>>>>>>"))
    }

    @Test
    fun `a base marker is only matched at exactly 7 pipes`() {
        assertTrue(ConflictMarkerScanner.scan("|||||| not enough").isEmpty())
        assertEquals(2, ConflictMarkerScanner.scan("<<<<<<< HEAD\n||||||| merged common ancestors").size)
    }

    @Test
    fun `8 or more pipes is not matched as the base marker`() {
        assertEquals(1, ConflictMarkerScanner.scan("<<<<<<< HEAD\n|||||||| too many").size)
    }

    @Test
    fun `clean code with no markers produces no hits`() {
        val text = "fun main() {\n    println(\"hello\")\n}"
        assertTrue(ConflictMarkerScanner.scan(text).isEmpty())
    }

    @Test
    fun `a start marker is only matched at exactly 7 angle brackets`() {
        assertTrue(ConflictMarkerScanner.scan("<<<<<< not enough").isEmpty())
        assertEquals(1, ConflictMarkerScanner.scan("<<<<<<< HEAD").size)
    }

    @Test
    fun `8 or more angle brackets is not matched as the 7-character marker`() {
        assertTrue(ConflictMarkerScanner.scan("<<<<<<<< too many").isEmpty())
    }

    @Test
    fun `the middle separator requires exactly 7 equals signs alone on the line`() {
        assertEquals(2, ConflictMarkerScanner.scan("<<<<<<< HEAD\n=======").size)
        assertEquals(1, ConflictMarkerScanner.scan("<<<<<<< HEAD\n======= trailing text").size)
    }

    // Regression (2026-09-30): a 7-letter title underlined with exactly
    // seven "=" -- Markdown setext and reStructuredText -- was flagged.
    @Test
    fun `a Markdown setext heading underline is not a conflict separator`() {
        assertTrue(ConflictMarkerScanner.scan("pricing-service\n===============\n\nLicense\n=======\n\nApache-2.0.\n").isEmpty())
    }

    @Test
    fun `a reStructuredText section underline is not a conflict separator`() {
        assertTrue(ConflictMarkerScanner.scan("Changes\n=======\n\n2.4\n---\n\n- Longer checkout timeout.\n").isEmpty())
    }

    @Test
    fun `separator and base markers are reported when an angle-bracket marker is in the file`() {
        val text = "a\n|||||||\nb\n=======\nc\n>>>>>>> feature\n"
        assertEquals(
            listOf("||||||| (diff3 common-ancestor section)", "======= (conflict separator)", ">>>>>>> (conflict end)"),
            ConflictMarkerScanner.scan(text).map { it.label },
        )
    }

    @Test
    fun `markers are found regardless of position in a multi-line file`() {
        val text = "line1\nline2\n<<<<<<< HEAD\nline4\n"
        val hits = ConflictMarkerScanner.scan(text)
        assertEquals(1, hits.size)
        assertTrue(text.substring(hits[0].startOffset, hits[0].endOffset).startsWith("<<<<<<<"))
    }
}
