package migration

import logs.LogKeeper
import tools.*

internal class ArgumentCaptors {
    fun convert(classText: String): String {
        var updatedText = classText
        StartingPoints.argumentCaptorsPredicate.forEach { (searchPredicate, requiresTypeExtraction) ->
            var doneConverting = false
            while (!doneConverting) {
                val (type, captorIndex) = if (requiresTypeExtraction) {
                    updatedText.extractTypeCodeAndIndex() ?: ("" to -1)
                } else {
                    null to updatedText.indexOf(searchPredicate)
                }
                if (captorIndex != -1) {
                    if (type != null) {
                        updatedText = updatedText.replace("ArgumentCaptor.forClass($type::class.java)", "slot<$type>()")
                    }
                    convertRemaining(updatedText, captorIndex)?.let {
                        updatedText = it
                    }
                } else {
                    doneConverting = true
                }
            }
        }

        return updatedText.migrateCaptorsWithoutVariable()
    }

    private fun String.migrateCaptorsWithoutVariable(): String {
        var updatedText = this
        while (updatedText.contains(argumentCaptorRegex)) {
            val range = argumentCaptorRegex.find(updatedText)?.range
            if (range == null) {
                LogKeeper.logWarning("Failed to convert ${argumentCaptorRegex.pattern}")
                return this
            }
            val endIndex = updatedText.indexOfMatchingBraces(
                startAfterIndex = range.endInclusive - 1,
                bracketType = BracketType.Braces
            )
            if (endIndex == null) {
                LogKeeper.logWarning("Failed to find end of function. Skipping")
                return this
            }
            val oldRangeToReplace = IntRange(range.start, endIndex + range.endInclusive - 1)
            val extractedCode = updatedText.substring(oldRangeToReplace)
            if (extractedCode.isEmpty()) {
                LogKeeper.logWarning("Failed to extract code after index:${range.last}")
                return this
            }
            var updatedExtractedCode: String = extractedCode
            blockStringToReplace.forEach { (old, new) ->
                updatedExtractedCode = updatedExtractedCode.replace(old, new)
            }
            updatedExtractedCode = updatedExtractedCode.replace("argumentCaptor<", "slot<")
            updatedText = updatedText.replaceRange(oldRangeToReplace, updatedExtractedCode)
        }
        return updatedText
    }

    private fun convertRemaining(updatedText: String, captorIndex: Int): String? {
        /*
                     * 1.Find index of captor
                     * 2.Get variable name
                     * 3.Replace the call to captor declaration
                     * 4.Replace .capture
                     * 5.Replace getting value from captor (log potential warnings)
                     */
        var updatedText1 = updatedText
        ImportsConverter.addImports("io.mockk.slot")
        val variableName = updatedText1.variableNameFinder(captorIndex) ?: run {
            LogKeeper.logWarning("Could not find variable name. Skipping this migration, please check the code.")
            return null
        }
        // Replace only first argumentCaptor so the search can next one.
        stringsToReplace.forEach { (old, new) ->
            if (updatedText1.contains(old)) {
                ImportsConverter.addImports("io.mockk.CapturingSlot")
            }
            updatedText1 = updatedText1.replaceFirst(old, new)
        }
        val captureCode = "${variableName}.capture()"
        updatedText1 = updatedText1.replaceFirst(captureCode, "capture($variableName)")
        updatedText1 = updatedText1.replaceCapturedValueCall(variableName)
        return updatedText1
    }

    private fun String.extractTypeCodeAndIndex(): Pair<String, Int>? {
        val indexOf = indexOf("ArgumentCaptor.forClass(")
        if (indexOf == -1) return null
        val type = substringBetweenBraces(startAfterIndex = indexOf)?.split(":")?.firstOrNull() ?: return null
        return type to indexOf(type, startIndex = indexOf)
    }

    /*
        This will replace all occurrences with the same now across the whole file, but does it matter?
        Should this be per function?
     */
    private fun String.replaceCapturedValueCall(variableName: String): String {
        var updatedText = this
        updatedText = replaceToCapture("firstValue", variableName, updatedText)
        updatedText = replaceToCapture("secondValue", variableName, updatedText)
        updatedText = replaceToCapture("thirdValue", variableName, updatedText)
        updatedText = replaceToCapture("lastValue", variableName, updatedText)
        updatedText = updatedText.extractAllValuesStatement(variableName)
        return updatedText
    }

    private fun replaceToCapture(suffix: String, variableName: String, updatedText: String): String {
        LogKeeper.logWarning("$variableName.$suffix replaced with $variableName.captured")
        return updatedText.replace("$variableName.$suffix", "${variableName}.captured")
    }

    private fun String.extractAllValuesStatement(variableName: String): String {
        var updatedText = this
        val searchText = "$variableName.allValues"
        while (updatedText.contains(searchText)) {
            val index = updatedText.indexOf(searchText)
            val insideBracketValue = substringBetweenBraces(startAfterIndex = index, bracketType = BracketType.Square)
            val valueToReplace = "$variableName.allValues[$insideBracketValue]"
            LogKeeper.logWarning("$variableName replace with captured, please review the code!!")
            updatedText = updatedText.replace(valueToReplace, "${variableName}.captured")
        }
        return updatedText
    }

    private val stringsToReplace = listOf(
        "argumentCaptor<" to "slot<",
        "argumentCaptor(" to "slot(",
        "KArgumentCaptor" to "CapturingSlot",
    )

    private val blockStringToReplace = listOf(
        "firstValue" to "captured",
        "secondValue" to "captured",
        "thirdValue" to "captured",
        "lastValue" to "captured",
        "capture()" to "capture(this)",
        "this.capture()" to "capture(this)",
    )

    private val argumentCaptorRegex = Regex("argumentCaptor<.*>().* \\{")
}