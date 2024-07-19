package replace

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportsConverterTest {
    private val importsConverter = ImportsConverter

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}")
    @MethodSource("args")
    fun `convert, given lines with import`(
        input: String,
        expected: String?
    ) {
        val actual = importsConverter.convert(input)
        Assertions.assertEquals(expected, actual)
    }

    private val multiLineInput = """
        import org.mockito.kotlin.doReturn
        import org.mockito.kotlin.whenever
        import org.mockito.kotlin.*
        import org.mockito.*
        import org.mockito.Mockito.verify
        import org.mockito.Mockito.verifyNoMoreInteractions
        import org.mockito.kotlin.argumentCaptor
        import org.mockito.kotlin.eq
        import org.mockito.kotlin.mock
    """.trimIndent()

    private val multiLineExpected = """
        import io.mockk.every
        
    """.trimIndent()
    private fun args() = listOf(
        Arguments.of("import org.mockito.kotlin.doReturn", ""),
        Arguments.of("import org.mockito.kotlin.whenever", "import io.mockk.every"),
        Arguments.of(
            "whenever(someClass.someFunction()) doReturn listOf(appFeedback)",
            "whenever(someClass.someFunction()) doReturn listOf(appFeedback)"
        ),
    )
}