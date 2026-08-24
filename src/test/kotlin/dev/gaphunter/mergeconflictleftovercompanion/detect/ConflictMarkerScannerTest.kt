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
        assertEquals(1, ConflictMarkerScanner.scan("=======").size)
        assertTrue(ConflictMarkerScanner.scan("======= trailing text").isEmpty())
    }

    @Test
    fun `markers are found regardless of position in a multi-line file`() {
        val text = "line1\nline2\n<<<<<<< HEAD\nline4\n"
        val hits = ConflictMarkerScanner.scan(text)
        assertEquals(1, hits.size)
        assertTrue(text.substring(hits[0].startOffset, hits[0].endOffset).startsWith("<<<<<<<"))
    }
}
