package tools

fun String.substringBetween(after: String, before: String): String =
    substringAfter(after).substringBeforeLast(before)

enum class BracketType(internal val left: Char, internal val right: Char) {
    Parentheses('(', ')'),
    Square('[', ']'),
    Braces('{', '}'),
    Angle('<', '>');

    companion object {
        operator fun get(left: Char): BracketType {
            return BracketType.values().first { it.left == left }
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
): Pair<IntRange, String> {
    TODO()
}