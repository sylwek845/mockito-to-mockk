package replace

import logs.LogKeeper
import tools.StartingPoints

class MockitoToMockkConverter {
    private val mainConverter = MainConverter()
    private val verifyConverter = VerifyConverter()
    fun convert(clazz: String): String {
        LogKeeper.clear()
        return runCatching {
            val converted1 = mainConverter.convert(clazz, StartingPoints.mockingPredicate) { extractedBlock ->
                ImportsConverter.addImports("io.mockk.every")
                "every { $extractedBlock }"
            }
            val converted2 = mainConverter.convert(converted1, StartingPoints.returnPredicate) { extractedBlock ->
                ".returns($extractedBlock)"
            }
            val converted3 = converted2.replaceHardcoded()

            val verified = verifyConverter.convert(converted3)

            val converted4 = ImportsConverter.convert(verified)
            converted4
        }.onFailure {
            LogKeeper.logError(it.message.orEmpty())
        }.getOrDefault(clazz).also {
            LogKeeper.print()
        }
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