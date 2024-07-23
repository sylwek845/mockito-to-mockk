package migration

import tools.BracketType
import tools.isSuspended
import tools.removeEqFromText
import tools.substringBetweenBraces

class MainConverter {

    fun convert(classText: String, predicate: List<String>, block: (String, Boolean) -> String): String {
        var updatedText = classText
        predicate.forEach { searchPredicate ->
            var doneConverting = false
            while (!doneConverting) {
                val extracted = updatedText.extractCode(searchPredicate)
                if (extracted != null) {
                    val isCoroutine = searchPredicate.isSuspended
                    val toReplace = block.invoke(extracted.extractedCode.trim(), isCoroutine)
                    val currentText = updatedText.replaceRange(extracted.rangeOfOriginalCode, toReplace)
                    if (currentText == updatedText) {
                        doneConverting = true
                    }
                    updatedText = currentText
                } else {
                    doneConverting = true
                }
            }
        }
        return updatedText
    }

    private fun String.extractCode(predicate: String): ExtractedCodeData? {
        val startIndex = indexOf(predicate)
        val previousChar = getOrElse(startIndex - 1) { ' ' }
        if (startIndex == -1 || previousChar.isLetterOrDigit()) return null
        val bracket = BracketType[predicate.last()]
        val extractedCode = substringBetweenBraces(startAfterIndex = startIndex, bracketType = bracket) ?: return null
        val originalCodeLen = predicate.length + extractedCode.length
        val range = IntRange(startIndex, startIndex + originalCodeLen)
        return ExtractedCodeData(range, removeEqFromText(extractedCode))
    }
}

data class ExtractedCodeData(
    val rangeOfOriginalCode: IntRange,
    val extractedCode: String,
)