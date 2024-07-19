package replace

import tools.BracketType
import tools.substringBetween
import tools.substringBetweenBraces

class VerifyConverter {
    private val mainConverter = MainConverter()
    fun convert(textToConvert: String): String {
        val updatedText = textToConvert.parseNoInteractions().parseNoMoreInteractions()
        val updatedText2 = parseVerify(updatedText)
        return updatedText2
    }

    private fun parseVerify(textToConvert: String): String {
        var updatedText = textToConvert
        var doneConverting = false
        while (!doneConverting) {
            val extracted = updatedText.extractVerifyData()
            if (extracted != null) {
                val toReplace = "verify { ${extracted.extractedVerify.trim()} }"
                ImportsConverter.addImports("io.mockk.verify")
                val currentText = updatedText.replaceRange(extracted.rangeOfOriginalCode, toReplace)
                if (currentText == updatedText) {
                    doneConverting = true
                }
                updatedText = currentText
            } else {
                doneConverting = true
            }
        }
        return updatedText
    }
    var c=0
    private fun String.extractVerifyData(): VerifyData? {
        c++
        val startIndex = indexOf(PREDICATE)
        if (startIndex == -1) return null
        val bracket = BracketType.Parentheses
        val extracted =
            substringBetweenBraces(startAfterIndex = startIndex, bracketType = bracket) ?: return null
        val extractedAll = extracted.split(",")
        val extractedObjectName = extractedAll.first()
        val params = extractedAll.drop(0)
        val endBlockIndex = indexOf(").") + 2
        val indexOfFirstBracketAfterVerify = indexOf("(", endBlockIndex)
        val indexOfFirstSpaceAfterVerify = indexOf("\n", endBlockIndex + 2) // This is to escape from .\n
        val extractedStatement = if (indexOfFirstSpaceAfterVerify < indexOfFirstBracketAfterVerify) {
            // Use bracket
            val bracesContent = "(${substringBetweenBraces(startAfterIndex = endBlockIndex).orEmpty()})"
            val braceIndex = indexOf(bracesContent)
            substring(startIndex = endBlockIndex, endIndex = braceIndex + bracesContent.length)
        } else {
            substring(IntRange(start = endBlockIndex, indexOfFirstSpaceAfterVerify))
            // Use space
        }
        val lastIndex = indexOf(startIndex = startIndex, string = extractedStatement) + extractedStatement.length - 1
        val wholeBlock = "${extractedObjectName}.$extractedStatement"
        val range = IntRange(startIndex, startIndex + lastIndex)
        return VerifyData(
            rangeOfOriginalCode = range,
            extractedVerify = wholeBlock,
            remainingVerifyParams = params
        )
    }

    private fun String.parseNoMoreInteractions(): String {
        return mainConverter.convert(this, listOf("verifyNoMoreInteractions(")) { block ->
            ImportsConverter.addImports("io.mockk.confirmVerified")
            "confirmVerified($block)"
        }
    }

    private fun String.parseNoInteractions(): String {
        return mainConverter.convert(this, listOf("verifyNoInteractions(")) { block ->
            ImportsConverter.addImports("io.mockk.Called")
            val eachStatement = block.split(",")
            eachStatement.map { eachBlock ->
                "verify { ${eachBlock.trim()} wasNot Called }"
            }.joinToString("\n") { it }
        }
    }

    private data class VerifyData(
        val rangeOfOriginalCode: IntRange,
        val extractedVerify: String,
        val remainingVerifyParams: List<String>,
    )

    private companion object {
        const val PREDICATE = "verify("
    }
}