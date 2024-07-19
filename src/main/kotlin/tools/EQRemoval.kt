package tools

fun removeEqFromText(text: String): String {
    if (!text.contains(EQ)) return text
    val extractedParams = text.substringBetween("(", ")")
    return extractedParams.split(",").joinToString(", ") {
        if (it.contains(EQ)) {
            it.substringBetween(EQ, ")")
        } else it.trim()
    }.let {
        text.replace(extractedParams, it)
    }
}

private const val EQ = "eq("