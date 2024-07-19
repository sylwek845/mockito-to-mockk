package replace

import tools.StartingPoints

class MockitoToMockkConverter {
    private val mainConverter = MainConverter()
    private val verifyConverter = VerifyConverter()
    fun convert(clazz: String): String {
        val converted1 = mainConverter.convert(clazz, StartingPoints.mockingPredicate) { extractedBlock ->
            ImportsConverter.addImports("io.mockk.every")
            "every { $extractedBlock }"
        }
        val converted2 = mainConverter.convert(converted1, StartingPoints.returnPredicate) { extractedBlock ->
            " returns $extractedBlock"
        }
        val converted3 = converted2.replaceHardcoded()

        val verified = verifyConverter.convert(converted3)

        val converted4 = ImportsConverter.convert(verified)

        return converted4
    }

    private fun String.replaceHardcoded(): String {
        var updatedClass = this
        StartingPoints.justToReplace.forEach { (old, data) ->
            updatedClass = updatedClass.replace(old, data.replaceWith)
            data.performAdditionalAction.invoke()
        }
        return updatedClass
    }
}