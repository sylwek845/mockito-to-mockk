package tools

import logs.LogKeeper

fun String.substringBetween(after: String, before: String): String =
    substringAfter(after).substringBeforeLast(before)

enum class BracketType(internal val left: Char, internal val right: Char) {
    Parentheses('(', ')'),
    Square('[', ']'),
    Braces('{', '}'),
    Angle('<', '>');

    companion object {
        operator fun get(left: Char): BracketType {
            return entries.first { it.left == left }
        }
    }
}

fun String.substringBetweenBraces(
    bracketCount: Int = 0,
    startAfterIndex: Int = 0,
    bracketType: BracketType = BracketType.Parentheses,
): String? {
    val slicedString =
        if (startAfterIndex == 0) this else substring(startAfterIndex)

    var leftCount = 0
    var rightCount = 0
    var leftIndex = 0
    var rightIndex = 0

    slicedString.toCharArray().forEachIndexed { index, c ->
        when (c) {
            bracketType.left -> {
                if (leftCount == bracketCount) {
                    leftIndex = index
                }
                leftCount++
            }

            bracketType.right -> {
                rightCount++
                if (leftCount == rightCount + bracketCount) {
                    rightIndex = index
                    return slicedString.substring(leftIndex + 1, rightIndex)
                }
            }
        }
    }
    if (bracketCount + 1 > leftCount || leftCount != rightCount) return null
    return slicedString.substring(leftIndex + 1, rightIndex)
}

fun String.findEndOfFunctionOrVariable(
    startAfterIndex: Int = 0,
): Pair<Int, String>? {
    val slicedString = drop(startAfterIndex)
    slicedString.forEachIndexed { index, c ->
        when {
            (slicedString.length - 1 == index) -> {
                val code = substring(startIndex = startAfterIndex, endIndex = startAfterIndex + index + 1)
                return startAfterIndex + index to code
            }

            (!c.isLetterOrDigit() && !c.isParentheses()) && c.emptyChar() -> {
                val code = substring(startIndex = startAfterIndex, endIndex = startAfterIndex + index + 1)
                return startAfterIndex + index to code
            }

            !c.isLetterOrDigit() && c.isParentheses() -> {
                val bracketStartingIndex = index + startAfterIndex
                val extractedCode = substringBetweenBraces(startAfterIndex = bracketStartingIndex) ?: run {
                    LogKeeper.logCritical("Did not find end of ( near index $bracketStartingIndex")
                    return null
                }
                val fullCodeLen = extractedCode.length + 2 + index + startAfterIndex
                val code = substring(startIndex = startAfterIndex, endIndex = fullCodeLen).trim()
                return fullCodeLen to code
            }
        }
    }
    return null
}

fun String.variableNameFinder(
    startIndex: Int = length - 1,
): String? {
    val trimmedString = substring(startIndex = 0, endIndex = startIndex)
    val name = variableRegex.findAll(trimmedString).last()
    return name.groups[1]?.value
}

private val variableRegex = Regex("(?:var|val)\\s+(\\w+)")

private fun Char.emptyChar(): Boolean {
    return this == ' '
}

private fun Char.isParentheses(): Boolean {
    return this == '('
}