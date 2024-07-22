package migration

import logs.LogKeeper
import tools.BracketType
import tools.StartingPoints
import tools.substringBetweenBraces
import tools.variableNameFinder

internal class ArgumentCaptors {
    fun convert(classText: String): String {
        var updatedText = classText
        StartingPoints.argumentCaptorsPredicate.forEach { searchPredicate ->
            var doneConverting = false
            while (!doneConverting) {
                val captorIndex = updatedText.indexOf(searchPredicate)
                if (captorIndex != -1) {
                    /*
                     * 1.Find index of captor
                     * 2.Get variable name
                     * 3.Replace the call to captor declaration
                     * 4.Replace .capture
                     * 5.Replace getting value from captor (log potential warnings)
                     */
                    ImportsConverter.addImports("io.mockk.slot")
                    val variableName = updatedText.variableNameFinder(captorIndex) ?: run {
                        LogKeeper.logCritical("Could not find variable name. Skipping this migration, please check the code.")
                        return@forEach
                    }
                    // Replace only first argumentCaptor so the search can next one.
                    stringsToReplace.forEach { (old, new) ->
                        updatedText = updatedText.replaceFirst(old, new)
                    }
                    val captureCode = "${variableName}.capture()"
                    updatedText = updatedText.replaceFirst(captureCode, "capture($variableName)")
                    updatedText = updatedText.replaceCapturedValueCall(variableName)
                } else {
                    doneConverting = true
                }
            }
        }
        return updatedText
    }

    /*
        This will replace all occurrences with the same now across the whole file, but does it matter?
        Should this be per function?
     */
    private fun String.replaceCapturedValueCall(variableName: String): String {
        var updatedText = this
//        while (getContainsList(variableName).anyMatch(updatedText::contains)) {
        updatedText = replaceToCapture("firstValue", variableName, updatedText)
        updatedText = replaceToCapture("secondValue", variableName, updatedText)
        updatedText = replaceToCapture("thirdValue", variableName, updatedText)
        updatedText = replaceToCapture("lastValue", variableName, updatedText)
        updatedText = updatedText.extractAllValuesStatement(variableName)
//        }
        return updatedText
    }

    private fun replaceToCapture(suffix: String, variableName: String, updatedText: String): String {
        LogKeeper.logWarning("$variableName.$suffix replaced with .captured")
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
    )
}