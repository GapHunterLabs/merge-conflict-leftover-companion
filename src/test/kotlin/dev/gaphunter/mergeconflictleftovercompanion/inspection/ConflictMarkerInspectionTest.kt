package dev.gaphunter.mergeconflictleftovercompanion.inspection

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ConflictMarkerInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(ConflictMarkerInspection::class.java)
    }

    fun `test a file with a leftover conflict marker produces a warning`() {
        myFixture.configureByText(
            "notes.txt",
            "line1\n<<<<<<< HEAD\nline3\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Leftover Git merge-conflict marker") == true })
    }

    fun `test a clean file produces no warning`() {
        myFixture.configureByText("notes.txt", "line1\nline2\nline3\n")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Leftover Git merge-conflict marker") == true })
    }

    fun `test the inspection applies to real source files too, not just plain text`() {
        myFixture.configureByText(
            "Main.java",
            "class Main {\n<<<<<<< HEAD\n    void run() {}\n=======\n    void execute() {}\n>>>>>>> feature\n}\n",
        )
        val highlights = myFixture.doHighlighting()
        val count = highlights.count { it.description?.contains("Leftover Git merge-conflict marker") == true }
        assertEquals(3, count)
    }

    // Regression (2026-09-30): the warning was anchored to the first leaf at
    // the marker and dropped when the marker line was longer than that
    // leaf -- in Java "|||||||" lexes as "||" tokens, in YAML "|||||||" and
    // ">>>>>>>" start with a one-character block-scalar token.
    fun `test every marker of a diff3 conflict is reported, highlighting the whole line`() {
        val text = "class Main {\n<<<<<<< HEAD\n    int t = 30;\n||||||| merged common ancestors\n    int t = 20;\n=======\n    int t = 45;\n>>>>>>> release\n}\n"
        myFixture.configureByText("Main.java", text)
        val ours = myFixture.doHighlighting().filter { it.description?.contains("Leftover Git merge-conflict marker") == true }
        assertEquals(4, ours.size)
        val highlighted = ours.map { text.substring(it.startOffset, it.endOffset) }.sorted()
        assertEquals(
            listOf("<<<<<<< HEAD", "=======", ">>>>>>> release", "||||||| merged common ancestors").sorted(),
            highlighted,
        )
    }

    fun `test every marker of a diff3 conflict in YAML is reported`() {
        val text = "checkout:\n  currency: USD\n<<<<<<< HEAD\n  timeout_seconds: 30\n||||||| merged common ancestors\n  timeout_seconds: 20\n=======\n  timeout_seconds: 45\n>>>>>>> release/2.4\n  retries: 3\n"
        myFixture.configureByText("settings.yaml", text)
        val ours = myFixture.doHighlighting().filter { it.description?.contains("Leftover Git merge-conflict marker") == true }
        assertEquals(
            listOf("<<<<<<< HEAD", "||||||| merged common ancestors", "=======", ">>>>>>> release/2.4"),
            ours.sortedBy { it.startOffset }.map { text.substring(it.startOffset, it.endOffset) },
        )
    }

    fun `test a 7-letter title underline in a text file is not flagged`() {
        myFixture.configureByText("notes.txt", "License\n=======\n\nApache-2.0.\n")
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Leftover Git merge-conflict marker") == true })
    }

    fun `test only the base-language root reports`() {
        assertTrue(ConflictMarkerInspection.isBaseRoot(PlainTextLanguage.INSTANCE, PlainTextLanguage.INSTANCE))
        assertFalse(ConflictMarkerInspection.isBaseRoot(PlainTextLanguage.INSTANCE, Language.ANY))
    }

    fun `test the marker range is the whole line and rejects ranges outside the text`() {
        assertEquals(TextRange(4, 11), ConflictMarkerInspection.markerRange(4, 11, 20))
        assertNull(ConflictMarkerInspection.markerRange(4, 21, 20))
        assertNull(ConflictMarkerInspection.markerRange(-1, 3, 20))
    }
}
