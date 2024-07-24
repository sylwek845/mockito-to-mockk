package testfiles

import org.intellij.lang.annotations.Language

object TestData1 {
    @Language("kotlin")
    val testFile1Mockito = """
    import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString

// Define a sample class to be tested
class SampleClass(private val dependency1: Dependency1, private val dependency2: Dependency2) {
    fun doSomething() {
        val result = dependency1.someMethod("input")
        dependency2.anotherMethod(result)
    }
}

// Define dependencies to be mocked
interface Dependency1 {
    fun someMethod(input: String): Int
}

interface Dependency2 {
    fun anotherMethod(input: Int)
}

class SampleClassTest {

    // First function: Set up mocks
    @Test
    fun setupMocks() {
        // Create mocks
        val mockDependency1 = mock(Dependency1::class.java)
        val mockDependency2 = mock(Dependency2::class.java)

        // Define behavior for mocks
        `when`(mockDependency1.someMethod(anyString())).thenReturn(42)

        // Create an instance of the class under test
        val sampleClass = SampleClass(mockDependency1, mockDependency2)

        // Call the method to be tested
        sampleClass.doSomething()

        // Verify interactions
        verify(mockDependency1).someMethod("input")
        verify(mockDependency2).anotherMethod(42)
    }

    // Second function: Verify interactions
    @Test
    fun verifyInteractions() {
        // Create mocks
        val mockDependency1 = mock(Dependency1::class.java)
        val mockDependency2 = mock(Dependency2::class.java)

        // Define behavior for mocks
        `when`(mockDependency1.someMethod(anyString())).thenReturn(42)

        // Create an instance of the class under test
        val sampleClass = SampleClass(mockDependency1, mockDependency2)

        // Call the method to be tested
        sampleClass.doSomething()

        // Verify interactions
        Mockito.verify(mockDependency1).someMethod("input")
        verify(mockDependency2).anotherMethod(42)

        // Additional verification using ArgumentCaptor
        val captor = ArgumentCaptor.forClass(Int::class.java)
        verify(mockDependency2).anotherMethod(capture(captor))
        assert(captor.value == 42)
    }
}
    """.trimIndent()

    @Language("kotlin")
    val testDataExpected1 = """import io.mockk.slot
import io.mockk.verify
import io.mockk.every
import io.mockk.mockk
    import org.junit.jupiter.api.Test

// Define a sample class to be tested
class SampleClass(private val dependency1: Dependency1, private val dependency2: Dependency2) {
    fun doSomething() {
        val result = dependency1.someMethod("input")
        dependency2.anotherMethod(result)
    }
}

// Define dependencies to be mocked
interface Dependency1 {
    fun someMethod(input: String): Int
}

interface Dependency2 {
    fun anotherMethod(input: Int)
}

class SampleClassTest {

    // First function: Set up mocks
    @Test
    fun setupMocks() {
        // Create mocks
        val mockDependency1 = mockk(relaxed = true, Dependency1::class.java)
        val mockDependency2 = mockk(relaxed = true, Dependency2::class.java)

        // Define behavior for mocks
        every { mockDependency1.someMethod(anyString()) }.returns(42)

        // Create an instance of the class under test
        val sampleClass = SampleClass(mockDependency1, mockDependency2)

        // Call the method to be tested
        sampleClass.doSomething()

        // Verify interactions
        verify { mockDependency1.someMethod("input") }
        verify { mockDependency2.anotherMethod(42) }
    }

    // Second function: Verify interactions
    @Test
    fun verifyInteractions() {
        // Create mocks
        val mockDependency1 = mockk(relaxed = true, Dependency1::class.java)
        val mockDependency2 = mockk(relaxed = true, Dependency2::class.java)

        // Define behavior for mocks
        every { mockDependency1.someMethod(anyString()) }.returns(42)

        // Create an instance of the class under test
        val sampleClass = SampleClass(mockDependency1, mockDependency2)

        // Call the method to be tested
        sampleClass.doSomething()

        // Verify interactions
        verify { mockDependency1.someMethod("input") }
        verify { mockDependency2.anotherMethod(42) }

        // Additional verification using ArgumentCaptor
        val captor = slot<Int>()
        verify { mockDependency2.anotherMethod(capture(captor)) }
        assert(captor.value == 42)
    }
}
    """.trimIndent()
}