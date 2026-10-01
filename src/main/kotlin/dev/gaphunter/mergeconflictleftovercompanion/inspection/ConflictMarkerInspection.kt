package dev.gaphunter.mergeconflictleftovercompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import dev.gaphunter.mergeconflictleftovercompanion.detect.ConflictMarkerScanner
import dev.gaphunter.mergeconflictleftovercompanion.review.ReviewPrompt

/**
 * Flags real Git merge-conflict markers left behind in any file's text.
 * Plain-text scan, no PSI-per-language dependency -- registered without
 * a `language` filter in `plugin.xml`, same "applies to any file type"
 * pattern as `env-var-missing-companion`'s `MissingEnvVarInspection`.
 */
class ConflictMarkerInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        // Registered for every language: a file with more than one PSI root
        // (Markdown with the bundled plugin) ran this once per root and
        // reported every marker twice. Only the base-language root reports.
        if (!isBaseRoot(file.language, file.viewProvider.baseLanguage)) return null
        val text = file.text
        if (text.length > MAX_FILE_LENGTH) return null

        val hits = ConflictMarkerScanner.scan(text)
        if (hits.isEmpty()) return null

        val virtualFile = file.virtualFile
        val problems = hits.mapNotNull { hit ->
            val range = markerRange(hit.startOffset, hit.endOffset, text.length) ?: return@mapNotNull null

            val problem = manager.createProblemDescriptor(
                file,
                range,
                "Leftover Git merge-conflict marker: ${hit.label}",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )

            if (virtualFile != null) {
                val lineNumber = file.viewProvider.document?.getLineNumber(hit.startOffset) ?: -1
                ReviewPrompt.recordHit(file.project, "${virtualFile.path}:$lineNumber")
            }

            problem
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    companion object {
        /** Files larger than this are skipped -- avoids pathological cost on generated/minified files. */
        const val MAX_FILE_LENGTH = 2_000_000

        fun isBaseRoot(language: Language, baseLanguage: Language): Boolean = language == baseLanguage

        /**
         * The whole marker line, in file offsets, anchored to the file itself.
         * It used to be anchored to the first leaf at the marker and dropped
         * when the line was longer than that leaf: in YAML the "|||||||" and
         * ">>>>>>>" lines start with a one-character block-scalar token, so
         * those markers were silently not reported (found 2026-09-30). Null if
         * the range falls outside the text.
         */
        fun markerRange(start: Int, end: Int, textLength: Int): TextRange? {
            if (start < 0 || end <= start || end > textLength) return null
            return TextRange(start, end)
        }
    }
}
