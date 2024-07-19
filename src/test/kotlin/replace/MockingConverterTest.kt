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
        assertEquals(expected.trim(), actual?.trim())
    }

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}")
    @MethodSource("argsReturn")
    fun `convert returns, given lines with both mock and return`(
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
    )

        private fun argsReturn() = listOf(
//        Arguments.of(
//            "every { someClass.someFunction() } doReturn listOf(appFeedback)",
//            "every { someClass.someFunction() } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "every { someClass.someFunction() }.doReturn(listOf(appFeedback))",
//            "every { someClass.someFunction() } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "every { someClass.someFunction() }.thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction() } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "every { someClass.someFunction(123, any()) }.thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, any()) } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "every { someClass.someFunction(123, date, any()) }.thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, date, any()) } returns listOf(appFeedback)"
//        ),
//
//        Arguments.of(
//            "every { someClass.someFunction(123, date) }.thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, date) } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "every { someClass.someFunction(123, date) }.thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, date) } returns listOf(appFeedback)"
//        ),
        Arguments.of(
            TestData1.testFile1,
            "every { someClass.someFunction(123, date) } returns listOf(appFeedback)"
        ),
//        "" to "",
//        "" to "",
    )

//    private fun args() = listOf(
//        Arguments.of(
//            "whenever(someClass.someFunction()) doReturn listOf(appFeedback)",
//            "every { someClass.someFunction() } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "whenever(someClass.someFunction()).doReturn(listOf(appFeedback))",
//            "every { someClass.someFunction() } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "whenever(someClass.someFunction()).thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction() } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "whenever(someClass.someFunction(eq(123), any())).thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, any()) } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "whenever(someClass.someFunction(eq(123), eq(date), any())).thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, date, any()) } returns listOf(appFeedback)"
//        ),
//
//        Arguments.of(
//            "whenever(someClass.someFunction(eq(123), eq(date)).thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, date) } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "on { someClass.someFunction(eq(123), eq(date) }.thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, date) } returns listOf(appFeedback)"
//        ),
//        Arguments.of(
//            "on { \nsomeClass.someFunction(eq(123), eq(date) \n\t\t}.thenReturn(listOf(appFeedback))",
//            "every { someClass.someFunction(123, date) } returns listOf(appFeedback)"
//        ),
////        "" to "",
////        "" to "",
//    )
}