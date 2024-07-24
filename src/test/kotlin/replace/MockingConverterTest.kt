package replace

import migration.MockitoToMockkConverter
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testfiles.TestData1

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MockingConverterTest {
    private val mockingConverter1 = MockitoToMockkConverter()

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}")
    @MethodSource("args")
    fun `convert, given lines with both mock and return`(
        input: String,
        expected: String
    ) {
        val actual = mockingConverter1.convert(input)
        assertEquals(expected.trim(), actual.trim())
    }

    @Language("kotlin")
    private val testDataMockito1 = """
           "private val someConfig: SomeConfig =
           mock {
               on { dailySummary } doReturn false
           },
    """.trimIndent()

    @Language("kotlin")
    private val testDataMockk1 = """
           "private val someConfig: SomeConfig =
           mockk {
               every { dailySummary } returns false
           },
    """.trimIndent()

    private fun args() = listOf(
        Arguments.of(
            "whenever(someClass.someFunction())",
            "every { someClass.someFunction() }"
        ),
        Arguments.of(
            "whenever(someClass.someFunction()).doReturn(data)",
            "every { someClass.someFunction() }.returns(data)"
        ),
        Arguments.of(
            "whenever(someClass.someFunction())",
            "every { someClass.someFunction() }"
        ),
        Arguments.of(
            "whenever(someClass.someFunction(eq(123), any()))",
            "every { someClass.someFunction(123, any()) }"
        ),
        Arguments.of(
            "whenever(someClass.someFunction(eq(123), eq(date), any()))",
            "every { someClass.someFunction(123, date, any()) }"
        ),

        Arguments.of(
            "whenever(someClass.someFunction(eq(123), eq(date)))",
            "every { someClass.someFunction(123, date) }"
        ),
        Arguments.of(
            "whenever(someClass.someFunction(eq(123), eq(date))).doAnswer(mock)",
            "every { someClass.someFunction(123, date) }.answers(mock)"
        ),
        Arguments.of(
            "whenever(someClass.someFunction(eq(123), eq(date))) doAnswer mock",
            "every { someClass.someFunction(123, date) } answers mock"
        ),
        Arguments.of(
            "whenever(someClass.someFunction(eq(123), eq(date))).doThrow(throwable)",
            "every { someClass.someFunction(123, date) }.throws(throwable)"
        ),

        Arguments.of(
            "whenever(someClass.someFunction(eq(123), eq(date))) doThrow throwable",
            "every { someClass.someFunction(123, date) } throws throwable"
        ),
        Arguments.of(
            "whenever(someClass.someFunction(eq(123), eq(date))) doThrow (throwable)",
            "every { someClass.someFunction(123, date) } throws (throwable)"
        ),
        Arguments.of(
            "wheneverBlocking(someClass.someFunction(eq(123), eq(date)))",
            "coEvery { someClass.someFunction(123, date) }"
        ),
        Arguments.of(
            "wheneverBlocking { someClass.someFunction(eq(123), eq(date)) }",
            "coEvery { someClass.someFunction(123, date) }"
        ),
        Arguments.of(
            "on { someClass.someFunction(eq(123), eq(date)) }",
            "every { someClass.someFunction(123, date) }"
        ),
        Arguments.of(
            "on { someClass.someFunction(eq(123), eq(date)) }\nlocation {}",
            "every { someClass.someFunction(123, date) }\nlocation {}"
        ),
        Arguments.of(
            "on { someClass.someFunction(eq(123), eq(date)) }.doReturn(1,2,3)",
            "every { someClass.someFunction(123, date) }.returnsMany(1,2,3)"
        ),
        Arguments.of(
            "on { \nsomeClass.someFunction(eq(123), eq(date)) \n\t\t}",
            "every { someClass.someFunction(123, date) }"
        ),

        Arguments.of(
            "on { \nsomeClass.someFunction(eq(123), eq(date)) \n\t\t}",
            "every { someClass.someFunction(123, date) }"
        ),
        Arguments.of(
            testDataMockito1,
            testDataMockk1
        ),
        Arguments.of(
            TestData1.testFile1Mockito,
            TestData1.testDataExpected1
        ),
    )

}