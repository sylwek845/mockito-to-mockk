package migration

import logs.LogKeeper
import tools.BracketType
import tools.findEndOfFunctionOrVariable
import tools.substringBetweenBraces

class VerifyConverter {
    private val mainConverter = MainConverter()
    fun convert(textToConvert: String): String {
        val updatedText = textToConvert.parseNoInteractions().parseNoMoreInteractions()
        val updatedText2 = parseVerify(updatedText)
        val fixedVerifies = updatedText2.replace(FAKE_VERIFY, PREDICATE).replace(EMPTY_VERIFY_PARAMS, NO_VERIFY_PARAMS)
        return fixedVerifies
    }

    private fun parseVerify(textToConvert: String): String {
        var updatedText = textToConvert
        var doneConverting = false
        while (!doneConverting) {
            val extracted = updatedText.extractVerifyData()
            if (extracted != null) {
                val verifyParams = extracted.remainingVerifyParams.prepareVerifyParamsForMockk()
                val toReplace = "$FAKE_VERIFY$verifyParams) { ${extracted.extractedVerify.trim()} }"
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

    private fun String.extractVerifyData(): VerifyData? {
        val startIndex = indexOf(PREDICATE)
        if (startIndex == -1) return null
        val bracket = BracketType.Parentheses
        val extracted =
            substringBetweenBraces(startAfterIndex = startIndex, bracketType = bracket) ?: return null
        val extractedAll = extracted.split(",")
        val extractedObjectName = extractedAll.first()
        val params = extractedAll.drop(1)
        val endBlockIndex = indexOf(startIndex = startIndex, char = '.') + 1
        val extractedStatement = findEndOfFunctionOrVariable(endBlockIndex) ?: return null
        val lastIndex = extractedStatement.first
        val wholeBlock = "${extractedObjectName}.${extractedStatement.second}"
        val range = IntRange(startIndex, lastIndex - 1)
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

    private fun List<String>.prepareVerifyParamsForMockk(): String {
        if (isEmpty()) return ""
        return mapNotNull { item ->
            when {
                item.contains(TIMES_PREFIX) -> {
                    val times = item.extractNumberOfVerifications(TIMES_PREFIX)
                    "exactly = $times"
                }

                item.contains(NEVER) -> {
                    "exactly = 0"
                }

                item.contains(AT_LEAST_ONCE) -> {
                    "atLeast = 1"
                }

                item.contains(AT_LEAST_PREFIX) -> {
                    val times = item.extractNumberOfVerifications(AT_LEAST_PREFIX)
                    "atLeast = $times"
                }

                item.contains(AT_MOST_PREFIX) -> {
                    val times = item.extractNumberOfVerifications(AT_MOST_PREFIX)
                    "atMost = $times"
                }

                else -> {
                    LogKeeper.logCritical("Failed to convert verify param $item please review missing param")
                    null
                }
            }
        }.joinToString(separator = ",") { it }
    }

    private fun String.extractNumberOfVerifications(prefix: String): String =
        substringAfter(prefix).substringBefore(")")

    private data class VerifyData(
        val rangeOfOriginalCode: IntRange,
        val extractedVerify: String,
        val remainingVerifyParams: List<String>,
    )

    private companion object {
        const val PREDICATE = "verify("
        const val FAKE_VERIFY =
            "ver@ify(" // This is required to avoid infinite loop, since the parser is looking for verify(
        const val EMPTY_VERIFY_PARAMS = "verify()"
        const val NO_VERIFY_PARAMS = "verify"
        const val TIMES_PREFIX = "times("
        const val AT_MOST_PREFIX = "atMost("
        const val AT_LEAST_PREFIX = "atLeast("
        const val NEVER = "never()"
        const val AT_LEAST_ONCE = "atLeastOnce()"
    }
}