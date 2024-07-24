package tools

import configuration.GlobalConfig

fun removeEqFromText(text: String): String {
    if (!GlobalConfig.removeEq) return text
    if (!text.contains(EQ)) return text

    val paramsSplit = text.split(EQ)
    return paramsSplit.mapNotNull {
        val updatedParam = it.replaceFirst(")", "")
        updatedParam.ifEmpty {
            null
        }
    }.joinToString("", postfix = ", ") { it }.removeSuffix(", ")
}

private const val EQ = "eq("