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
            val mockingConverted =
                mainConverter.convert(clazz, StartingPoints.mockingPredicate) { extractedBlock, isSuspended ->
                    ImportsConverter.addImports("io.mockk.every")
                    if (isSuspended) {
                        "coEvery { $extractedBlock }"
                    } else {
                        "every { $extractedBlock }"
                    }
                }
            val returns =
                mainConverter.convert(mockingConverted, StartingPoints.returnPredicate) { extractedBlock, _ ->
                    val prefix = if (extractedBlock.contains(",")) {
                        ".returnsMany"
                    } else {
                        ".returns"
                    }
                    "$prefix($extractedBlock)" // We can;t use infix since some mockito code has \n in between which causes the infix to fail.
                }
            val hardcoded = returns.replaceHardcoded()

            val verified = verifyConverter.convert(hardcoded)

            val slot = argumentCaptors.convert(verified)

            val imports = ImportsConverter.convert(slot)
            imports
        }.onFailure {
            LogKeeper.logError(it.message.orEmpty())
        }.getOrDefault(clazz).also {
            LogKeeper.logInfo("Success!")
            LogKeeper.print()
        }
    }

    private fun String.replaceHardcoded(): String {
        var updatedClass = this
        StartingPoints.justToReplace.forEach { (old, data) ->
            val addImport = updatedClass.contains(old)
            updatedClass = updatedClass.replace(old, data.replaceWith)
            if (addImport) {
                data.performAdditionalAction.invoke()
            }
        }
        return updatedClass
    }
}