package dev.gaphunter.mergeconflictleftovercompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
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

    companion object {
        /** Files larger than this are skipped -- avoids pathological cost on generated/minified files. */
        const val MAX_FILE_LENGTH = 2_000_000
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val text = file.text
        if (text.length > MAX_FILE_LENGTH) return null

        val hits = ConflictMarkerScanner.scan(text)
        if (hits.isEmpty()) return null

        val virtualFile = file.virtualFile
        val problems = hits.mapNotNull { hit ->
            val anchor = leafElementAt(file, hit.startOffset) ?: return@mapNotNull null
            val anchorStart = anchor.textRange.startOffset
            val relativeRange = TextRange(hit.startOffset - anchorStart, hit.endOffset - anchorStart)
            if (relativeRange.startOffset < 0 || relativeRange.endOffset > anchor.textLength) return@mapNotNull null

            val problem = manager.createProblemDescriptor(
                anchor,
                relativeRange,
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

    /** Leaf-anchored, never a composite node -- same fallback-to-file-root pattern as `MissingEnvVarInspection.leafElementAt`. */
    private fun leafElementAt(file: PsiFile, startOffset: Int): PsiElement? {
        if (startOffset < 0 || startOffset >= file.textLength) return null
        var element = file.findElementAt(startOffset) ?: return file
        while (element.firstChild != null) element = element.firstChild
        return element
    }
}
