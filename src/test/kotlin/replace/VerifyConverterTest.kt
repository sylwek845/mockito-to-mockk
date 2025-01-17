package replace

import configuration.GlobalConfig
import migration.VerifyConverter
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VerifyConverterTest {
    private val verifyConverter1 = VerifyConverter()

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}")
    @MethodSource("args")
    fun `convert, given line with verify`(
        input: String,
        expected: String
    ) {
        GlobalConfig.removeEq = true
        val actual = verifyConverter1.convert(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `should not remove EQ when flag is off`() {
        GlobalConfig.removeEq = false
        val actual = verifyConverter1.convert(inputWithEQ)
        assertEquals(outputWithEQ, actual)
    }

    @Language("kotlin")
    private val example1Mockito = """
        verify(mock)
        .testNewLine()
    """.trimIndent()

    @Language("kotlin")
    private val example2Mockito = """
        inOrder.verify(something).test()
        verify(mock).testNewLine()
        verify(mock1).testNewLine2(321421,mock())
        verify(mock2).testNewLine5<String>(321421,mock<Long>())
        verify(mock3).testNewLine4<String>(321421,any<Long>())
        verify(mock4).testNewLine5
        someOtherFunction.verify(1)
    """.trimIndent()

    @Language("kotlin")
    private val example1MockK = """
        verify { mock.testNewLine() }
    """.trimIndent()

    @Language("kotlin")
    private val example2MockK = """
        inOrder.verify(something).test()
        verify { mock.testNewLine() }
        verify { mock1.testNewLine2(321421,mock()) }
        verify { mock2.testNewLine5<String>(321421,mock<Long>()) }
        verify { mock3.testNewLine4<String>(321421,any<Long>()) }
        verify { mock4.testNewLine5 }
        someOtherFunction.verify(1)
    """.trimIndent()

    private fun args() = listOf(
        Arguments.of("verify(mock).someMethod()", "verify { mock.someMethod() }"),
        Arguments.of("verify(mock, times(2)).someMethod()", "verify(exactly = 2) { mock.someMethod() }"),
        Arguments.of("verify(mock, times(5)).someMethod(58)", "verify(exactly = 5) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, never()).someMethod(58)", "verify(exactly = 0) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, atLeastOnce()).someMethod(58)", "verify(atLeast = 1) { mock.someMethod(58) }"),
        Arguments.of("verify(mock, atLeast(2)).someMethod(58)", "verify(atLeast = 2) { mock.someMethod(58) }"),
        Arguments.of(
            "doSuspendableAnswer(mock, atMost(3)).someMethod(58)",
            "coVerify(atMost = 3) { mock.someMethod(58) }"
        ),
        Arguments.of("verify(eq(mock), atMost(3)).someMethod(eq(58))", "verify(atMost = 3) { mock.someMethod(58) }"),
        Arguments.of("verifyNoMoreInteractions(mock)", "confirmVerified(mock)"),
        Arguments.of("verifyNoMoreInteractions(mock, mock2)", "confirmVerified(mock, mock2)"),
        Arguments.of("verifyNoInteractions(mock)", "verify { mock wasNot Called }"),
        Arguments.of(
            "verifyNoInteractions(mock, mock2, mock3)",
            "verify { mock wasNot Called }\nverify { mock2 wasNot Called }\nverify { mock3 wasNot Called }"
        ),
        Arguments.of(example1Mockito, example1MockK),
        Arguments.of(example2Mockito, example2MockK),
    )

    private val inputWithEQ = "verify(mock).function(eq(data))"

    private val outputWithEQ = "verify { mock.function(eq(data)) }"
}
