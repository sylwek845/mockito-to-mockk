package tools

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UtilsKtTest {

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}, which={2}, bracketType={3}, startingFrom={4}")
    @MethodSource("argsBracketTest")
    fun `convert, input with brackets, expect extracted string`(
        input: String,
        expected: String?,
        which: Int,
        bracketType: BracketType,
        startingFrom: Int = 0,
    ) {
        assertEquals(
            expected.ignoreSpaces(),
            input.substringBetweenBraces(
                bracketCount = which,
                bracketType = bracketType,
                startAfterIndex = startingFrom,
            ).ignoreSpaces()
        )
    }

    @ParameterizedTest(name = "#{index}: input={0}, output={1}")
    @MethodSource("argsEndOfFunctionVariable")
    fun `findEndOfFunctionOrVariable test`(
        input: String,
        output: Pair<IntRange, String>,
    ) {
        val actual = input.findEndOfFunctionOrVariable()
        assertEquals(
            output.second.ignoreSpaces(),
            actual?.second.ignoreSpaces(),
        )
        assertEquals(
            output.first.last,
            actual?.first,
        )
    }

    @ParameterizedTest(name = "#{index}: input={0}, output={1}")
    @MethodSource("argsVariableNameFinder")
    fun `findVariableName test`(
        input: Pair<String, Int>,
        output: String,
    ) {
        val actual = input.first.variableNameFinder(startIndex = input.second)
        assertEquals(
            output,
            actual,
        )
    }


    private fun argsBracketTest() = listOf(
        Arguments.of("something(mock)", "mock", 0, BracketType.Parentheses, 0),
        Arguments.of("something(mock)", null, 1, BracketType.Parentheses, 0),
        Arguments.of("something(mock(mock1))", "mock(mock1)", 0, BracketType.Parentheses, 0),
        Arguments.of("something(mock(mock1))", "mock1", 1, BracketType.Parentheses, 0),
        Arguments.of("something(mock(mock1)).doReturn(this)", "mock(mock1)", 0, BracketType.Parentheses, 0),
        Arguments.of("something(mock(mock1(mock2)))", "mock2", 2, BracketType.Parentheses, 0),
        Arguments.of("something{mock{mock1{mock2}}}", "mock2", 2, BracketType.Braces, 0),
        Arguments.of("ew().something{mock{mock1{mock2}}}", "mock2", 2, BracketType.Braces, 0),
        Arguments.of(multiLineInput, multiLineExptected, 0, BracketType.Parentheses, 0),
    )

    private val multiLineInput = """
           whenever(
            stateHandler.getNewPasscodeDialog(
                any(),
                any()
            )
        ) doReturn something
    """.trimIndent()

    private val multiLineExptected = """ stateHandler.getNewPasscodeDialog(
                any(),
                any()
            )""".trimIndent()

    private fun String?.ignoreSpaces(): String? = this?.replace(" ", "")?.replace("\n", "")

    private val multiLineFVInput = """
         verify(someClass).
         testFunction(someParam,someParam)
         getSomeData()
     """.trimIndent()

    @Language("kotlin")
    private val example1Mockito = """
        verify(mock)
        .testNewLine()

    someCode()
    """.trimIndent()

    private fun argsEndOfFunctionVariable() = listOf(
        Arguments.of(
            "verify(someClass).testFunction() \n\n verify(someClass).testFunction()",
            IntRange(18, 32) to "testFunction()"
        ),
        Arguments.of(
            "verify(someClass).testFunction(someParam)  \n" +
                    "\n" +
                    " verify(someClass).testFunction()", IntRange(18, 41) to "testFunction(someParam)"
        ),
        Arguments.of("verify(someClass).testVariable", IntRange(18, 29) to "testVariable"),
        Arguments.of("verify(someClass).testVariable\n\n", IntRange(18, 31) to "testVariable"),
        Arguments.of(multiLineFVInput, IntRange(18, 52) to "testFunction(someParam,someParam)"),
        Arguments.of(example1Mockito, IntRange(example1Mockito.indexOf(".") + 1, 35) to "testNewLine()"),
    )

    private fun argsVariableNameFinder() = listOf(
        Arguments.of(
            "val testName = argumentCaptor()" to 31, "testName",
        ),
        Arguments.of(
            "val testName1\n = argumentCaptor()" to 33, "testName1",
        ),
        Arguments.of(
            "var testName2 = argumentCaptor()" to 32, "testName2",
        ),
        Arguments.of(
            "var testName3 = argumentCaptor()\nvar testName4 = someFunction()\nval testName5" to 51, "testName4",
        )
    )
}