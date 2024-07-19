package tools

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testfiles.TestData1

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

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}, which={2}, bracketType={3}, startingFrom={4}")
    @MethodSource("argsEndOfFunctionVariable")
    fun `findEndOfFunctionOrVariable test`(
        input: String,
        output: Pair<IntRange, String>,
    ) {
        val actual = input.findEndOfFunctionOrVariable()
        assertEquals(
            actual.second.ignoreSpaces(),
            output.second.ignoreSpaces()
        )
        assertEquals(
            actual.first,
            output.first
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
        Arguments.of(
            TestData1.testFile1,
            "stateHandler.initialState(any(),eq(uid))",
            0,
            BracketType.Parentheses,
            2297,
        ),
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
             testFunction()
         getSomeData()
     """.trimIndent()

    private fun argsEndOfFunctionVariable() = listOf(
        Arguments.of("verify(someClass).testFunction()", IntRange(17, 31) to "testFunction()"),
        Arguments.of("verify(someClass).testFunction(someParam)", IntRange(17, 40) to "testFunction(someParam)"),
        Arguments.of("verify(someClass).testVariable", IntRange(17, 29) to "testVariable"),
        Arguments.of(multiLineFVInput, IntRange(17, 45) to "testFunction()"),
    )
}