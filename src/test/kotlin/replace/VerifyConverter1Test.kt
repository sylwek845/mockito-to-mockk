package replace

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VerifyConverter1Test {
    private val verifyConverter1 = VerifyConverter()

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}")
    @MethodSource("args")
    fun `convert, given line with verify`(
        input: String,
        expected: String
    ) {
        val actual = verifyConverter1.convert(input)
        assertEquals(expected, actual)
    }

    @Language("kotlin")
    private val example1Mockito = """
        verify(mock)
    .testNewLine()
    """.trimIndent()

    @Language("kotlin")
    private val example1MockK = """
        verify { mock
    .testNewLine() }
    """.trimIndent()

    private fun args() = listOf(
        Arguments.of("verify(mock).someMethod()", "verify { mock.someMethod() }"),
        Arguments.of("verify(mock, times(2)).someMethod()", "verify(exactly = 2) { mock.someMethod() }"),
        Arguments.of("verify(mock, times(5)).someMethod(58)", "verify(exactly = 5) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, never()).someMethod(58)", "verify(exactly = 0) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, atLeastOnce()).someMethod(58)", "verify(atLeast = 1) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, atLeast(2)).someMethod(58)", "verify(atLeast = 2) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, atMost(3)).someMethod(58)", "verify(atMost = 3) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, atMost(3)).someMethod(58)", "verify(atMost = 3) { mock.someMethod(58) }"),
        Arguments.of("verifyNoMoreInteractions(mock)", "confirmVerified(mock)"),
        Arguments.of("verifyNoMoreInteractions(mock, mock2)", "confirmVerified(mock, mock2)"),
        Arguments.of("verifyNoInteractions(mock)", "verify { mock wasNot Called }"),
        Arguments.of(
            "verifyNoInteractions(mock, mock2, mock3)",
            "verify { mock wasNot Called }\nverify { mock2 wasNot Called }\nverify { mock3 wasNot Called }"
        ),
        Arguments.of(example1Mockito, example1MockK)
//        Arguments.of()
    )
}
