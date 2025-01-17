package migration

import logs.LogKeeper
import tools.*

class VerifyConverter {
    private val mainConverter = MainConverter()
    fun convert(textToConvert: String): String {
        val updatedText = textToConvert.parseNoInteractions().parseNoMoreInteractions()
        val updatedText2 = parseVerify(updatedText)
        val fixedVerifies = updatedText2
            .replace(FAKE_VERIFY, PREDICATE)
            .replace(FAKE_CO_VERIFY, CO_VERIFY)
            .replace(EMPTY_VERIFY_PARAMS, NO_VERIFY_PARAMS)
        return fixedVerifies
    }

    private fun parseVerify(textToConvert: String): String {
        var updatedText = textToConvert
        predicates.forEach { predicate ->
            var doneConverting = false
            while (!doneConverting) {
                when (val extracted = updatedText.extractVerifyData(predicate)) {
                    VerifyType.Ignore -> {
                        val prefix = getPrefixToReplace(predicate)
                        updatedText = updatedText.replaceFirst(PREDICATE, prefix)
                    }

                    VerifyType.NotFound -> {
                        doneConverting = true
                    }

                    is VerifyType.VerifyData -> {
                        val prefix = getPrefixToReplace(predicate)
                        val verifyParams = extracted.remainingVerifyParams.prepareVerifyParamsForMockk()
                        val toReplace = "$prefix$verifyParams) { ${extracted.extractedVerify.trim()} }"
                        val currentText = updatedText.replaceRange(extracted.rangeOfOriginalCode, toReplace)
                        if (currentText == updatedText) {
                            doneConverting = true
                        }
                        updatedText = currentText
                    }
                }
            }
        }
        return updatedText
    }

    private fun getPrefixToReplace(predicate: String): String {
        val isSuspended = predicate.isSuspended
        val prefix = if (isSuspended) {
            ImportsConverter.addImports("io.mockk.coVerify")
            FAKE_CO_VERIFY
        } else {
            ImportsConverter.addImports("io.mockk.verify")
            FAKE_VERIFY
        }
        return prefix
    }

    private fun String.extractVerifyData(predicate: String): VerifyType {
        val startIndex = indexOf(predicate)
        if (startIndex == -1) return VerifyType.NotFound
        val previousChar = getOrNull(startIndex - 1)
        if (previousChar == '.') { // Ignore .verify, this most likely does not belong to mockito
            return VerifyType.Ignore
        }
        val bracket = BracketType.Parentheses
        val extracted =
            substringBetweenBraces(startAfterIndex = startIndex, bracketType = bracket) ?: return VerifyType.Ignore
        val extractedAll = extracted.split(",")
        val extractedObjectName = removeEqFromText(extractedAll.first())
        val params = extractedAll.drop(1)
        val endBlockIndex = indexOf(startIndex = startIndex, char = '.') + 1
        val extractedStatement = findEndOfFunctionOrVariable(endBlockIndex) ?: return VerifyType.Ignore
        val lastIndex = extractedStatement.first
        val extractedBlock = if (extractedStatement.second.last() == ')') {
            extractedStatement.second
        } else {
            "${extractedStatement.second}\n"
        }
        val wholeBlock = "${extractedObjectName}.${removeEqFromText(extractedBlock)}"
        val range = IntRange(startIndex, lastIndex - 1)
        return VerifyType.VerifyData(
            rangeOfOriginalCode = range,
            extractedVerify = wholeBlock,
            remainingVerifyParams = params
        )
    }

    private fun String.parseNoMoreInteractions(): String {
        return mainConverter.convert(this, listOf("verifyNoMoreInteractions(")) { block, _ ->
            ImportsConverter.addImports("io.mockk.confirmVerified")
            "confirmVerified($block)"
        }
    }

    private fun String.parseNoInteractions(): String {
        return mainConverter.convert(this, listOf("verifyNoInteractions(")) { block, _ ->
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
                    LogKeeper.logWarning("Failed to convert verify param $item please review missing param")
                    null
                }
            }
        }.joinToString(separator = ",") { it }
    }

    private fun String.extractNumberOfVerifications(prefix: String): String =
        substringAfter(prefix).substringBefore(")")

    sealed interface VerifyType {

        data class VerifyData(
            val rangeOfOriginalCode: IntRange,
            val extractedVerify: String,
            val remainingVerifyParams: List<String>,
        ) : VerifyType

        data object Ignore : VerifyType
        data object NotFound : VerifyType
    }

    private val predicates = listOf(
        PREDICATE,
        "doSuspendableAnswer"
    )

    private companion object {
        const val PREDICATE = "verify("
        const val CO_VERIFY = "coVerify("
        const val FAKE_VERIFY =
            "ver@ify(" // This is required to avoid infinite loop, since the parser is looking for verify(
        const val FAKE_CO_VERIFY =
            "coVer@ify(" // This is required to avoid infinite loop, since the parser is looking for verify(
        const val EMPTY_VERIFY_PARAMS = "verify()"
        const val NO_VERIFY_PARAMS = "verify"
        const val TIMES_PREFIX = "times("
        const val AT_MOST_PREFIX = "atMost("
        const val AT_LEAST_PREFIX = "atLeast("
        const val NEVER = "never()"
        const val AT_LEAST_ONCE = "atLeastOnce()"
    }
}