package replace

import tools.substringBetweenBraces

class VerifyConverter1 {
    fun convert(textToParse: String): String {
        if (textToParse.contains(NO_MORE_PREFIX)) return textToParse.replace("verifyNoMoreInteractions", "confirmVerified")
        if (textToParse.contains(NO_INTERACTION)) return textToParse.substringBetweenBraces()?.split(",")?.joinToString(separator = "\n") {
            "verify { ${it.trim()} wasNot Called }"
        } ?: textToParse
        return extractVerifyCode(textToParse).extractVerificationMode()
    }

//    private fun convertUntilEOF(textToParse: String):String{
//        var updatedText = textToParse
//        var doneConverting = false
//        while (!doneConverting) {
//
//        }
//    }

    private fun extractVerifyCode(line: String): String =
        line.replaceFirst(")", "")
            .substringAfter(VERIFY_PREFIX)

    private fun String.extractVerificationMode(): String {
        if (!this.contains(",")) return this.addBasicMockKVerifyBlock()
        return when {
            contains(NEVER) -> "verify(exactly = 0) { ${this.replace(", never()", "")} }"
            contains(AT_LEAST_ONCE) -> "verify(atLeast = 1) { ${this.replace(", atLeastOnce()", "")} }"
            contains(NO_INTERACTION) -> {
                this.substringBetweenBraces()?.split(",")?.joinToString {
                    "verify { $it wasNot Called }"
                } ?: this
            }

            contains(TIMES_PREFIX) -> {
                val times = extractNumberOfVerifications(TIMES_PREFIX)
                addExactlyAndRemoveOldVerification("exactly", times)
            }

            contains(AT_MOST_PREFIX) -> {
                val times = extractNumberOfVerifications(AT_MOST_PREFIX)
                addExactlyAndRemoveOldVerification("atMost", times)
            }

            contains(AT_LEAST_PREFIX) -> {
                val times = extractNumberOfVerifications(AT_LEAST_PREFIX)
                addExactlyAndRemoveOldVerification("atLeast", times)
            }

            else -> this.addBasicMockKVerifyBlock()
        }
    }

    private fun String.addExactlyAndRemoveOldVerification(prefix: String, times: String): String =
        "verify($prefix = $times) { ${this.removeOldVerification()} }"

    private fun String.extractNumberOfVerifications(prefix: String): String =
        substringAfter(prefix).substringBefore(")")

    private fun String.removeOldVerification(): String {
        val indexOfFirstChar = indexOfFirst { it == ',' }
        val indexOfDot = indexOfFirst { it == '.' }
        return removeRange(indexOfFirstChar, indexOfDot)
    }

    private fun String.addBasicMockKVerifyBlock(): String =
        "verify { $this }"

    private companion object {
        const val VERIFY_PREFIX = "verify("
        const val TIMES_PREFIX = "times("
        const val AT_MOST_PREFIX = "atMost("
        const val AT_LEAST_PREFIX = "atLeast("
        const val NO_MORE_PREFIX = "verifyNoMoreInteractions("
        const val NO_INTERACTION = "verifyNoInteractions("
        const val NEVER = "never()"
        const val AT_LEAST_ONCE = "atLeastOnce()"
    }
}