package tools

fun removeEqFromText(text: String): String {
    if (!text.contains(EQ)) return text
    val extractedParams = text.substringBetween("(", ")")
    if (!extractedParams.contains(",")) {
        return extractedParams
    }
    return extractedParams.split(",").joinToString(", ") {
        if (it.contains(EQ)) {
            it.substringBetween(EQ, ")")
        } else it.trim()
    }.let {
        text.replace(extractedParams, it)
    }
}

private const val EQ = "eq("