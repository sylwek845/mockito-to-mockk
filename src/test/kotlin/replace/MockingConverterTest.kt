package replace

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

    private fun args() = listOf(
        Arguments.of(
            "whenever(someClass.someFunction())",
            "every { someClass.someFunction() }"
        ),
        Arguments.of(
            "whenever(someClass.someFunction())",
            "every { someClass.someFunction() }"
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
            "on { someClass.someFunction(eq(123), eq(date) }",
            "every { someClass.someFunction(123, date) }"
        ),
        Arguments.of(
            "on { \nsomeClass.someFunction(eq(123), eq(date) \n\t\t}",
            "every { someClass.someFunction(123, date) }"
        ),

        Arguments.of(
            "on { \nsomeClass.someFunction(eq(123), eq(date) \n\t\t}",
            "every { someClass.someFunction(123, date) }"
        ),
        Arguments.of(
            TestData1.testFile1Mockito,
            TestData1.testDataExpected1
        ),
    )

}