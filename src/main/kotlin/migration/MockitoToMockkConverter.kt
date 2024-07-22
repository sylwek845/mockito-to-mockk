package migration

import logs.LogKeeper
import tools.StartingPoints

class MockitoToMockkConverter {
    private val mainConverter = MainConverter()
    private val verifyConverter = VerifyConverter()
    private val argumentCaptors = ArgumentCaptors()
    fun convert(clazz: String): String {
        LogKeeper.clear()
        ImportsConverter.clear()
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

            val slot = argumentCaptors.convert(verified)

            val converted4 = ImportsConverter.convert(slot)
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