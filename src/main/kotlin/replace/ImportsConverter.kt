package replace

object ImportsConverter {
    private val importsToAdd = mutableSetOf("import io.mockk.mockk")
    fun addImports(import: String) {
        importsToAdd.add("import $import")
    }
    fun clear() {
        importsToAdd.clear()
    }

    fun convert(textToParse: String): String {

        if (!textToParse.contains("import")) return textToParse

        val lines = textToParse.split("\n").asSequence()
            .filterNot { it.contains("import org.mockito.") }
            .toMutableList()

        val firstImportIndex = lines.indexOfFirst { it.contains("import") }


        importsToAdd.forEach { importToAdd ->
            lines.add(firstImportIndex, importToAdd)
        }

        return lines.joinToString(separator = "\n") { it }
    }
}