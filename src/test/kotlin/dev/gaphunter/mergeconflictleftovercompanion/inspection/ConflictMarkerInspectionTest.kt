package dev.gaphunter.mergeconflictleftovercompanion.inspection

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
}
