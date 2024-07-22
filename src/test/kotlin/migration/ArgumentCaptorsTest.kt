package migration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArgumentCaptorsTest {
    private val argumentCaptors = ArgumentCaptors()

    @ParameterizedTest(name = "#{index}: input={0}, expected={1}")
    @MethodSource("args")
    fun `convert, given lines with both mock and return`(
        input: String,
        expected: String
    ) {
        val actual = argumentCaptors.convert(input)
        assertEquals(expected.trim(), actual.trim())
    }

    private fun args() = listOf(
        Arguments.of(input1, expected1),
        Arguments.of(input2, expected2),
        Arguments.of(input3, expected3),
    )

    private val input1 = """
        val test = argumentCaptor<Test>()
        
        someClass.test1()
        someClass.test2(test.capture())
        
        isEqualTo(test.firstValue, someData())
        isEqualTo(test.secondValue, someData())
        isEqualTo(test.thirdValue, someData())
    """.trimIndent()

    private val expected1 = """
        val test = slot<Test>()
        
        someClass.test1()
        someClass.test2(capture(test))
        
        isEqualTo(test.captured, someData())
        isEqualTo(test.captured, someData())
        isEqualTo(test.captured, someData())
    """.trimIndent()

    private val input2 = """
        val test: Test = argumentCaptor()
        
        someClass.test1()
        someClass.test2(test.capture())
        
        isEqualTo(test.lastValue, someData())
    """.trimIndent()

    private val expected2 = """
        val test: Test = slot()
        
        someClass.test1()
        someClass.test2(capture(test))
        
        isEqualTo(test.captured, someData())
    """.trimIndent()

    private val input3 = """
        val test: Test = argumentCaptor()
        
        someClass.test1()
        someClass.test2(test.capture())
        
        isEqualTo(test.allValues[3], someData())
    """.trimIndent()

    private val expected3 = """
        val test: Test = slot()
        
        someClass.test1()
        someClass.test2(capture(test))
        
        isEqualTo(test.captured, someData())
    """.trimIndent()
}