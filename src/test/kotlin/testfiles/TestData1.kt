package testfiles

import org.intellij.lang.annotations.Language

object TestData1 {
    @Language("kotlin")
    val testFile1Mockito = """package com.deliveroo.driverapp.feature.confirmidentityv2

import com.deliveroo.common.ui.UiKitDefaultRow
import com.deliveroo.common.ui.compose.tags.UIKitTagColor
import com.deliveroo.driverapp.assertValue
import com.deliveroo.driverapp.assertions.isEqualTo
import com.deliveroo.driverapp.assertions.isFalse
import com.deliveroo.driverapp.assertions.isTrue
import com.deliveroo.driverapp.components.IconState
import com.deliveroo.driverapp.configuration.FeatureConfigurations
import com.deliveroo.driverapp.coroutine.test
import com.deliveroo.driverapp.feature.analytics.AnalyticsVerifyIdentityProvider
import com.deliveroo.driverapp.feature.confirmidentityv2.viewmodels.ConfirmIdentityViewModel
import com.deliveroo.driverapp.model.ConfirmIdentityConfiguration
import com.deliveroo.driverapp.model.Strings
import com.deliveroo.driverapp.navigation.CommonDirections
import com.deliveroo.driverapp.rules.CoroutinesTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmIdentityViewModelTest {

    @RegisterExtension
    var coroutinesTestRule = CoroutinesTestRule()

    private val analyticsVerifyIdentityProvider: AnalyticsVerifyIdentityProvider = mock()

    private val confirmIdentityConfiguration: ConfirmIdentityConfiguration = mock {
        on { checkType } doReturn ConfirmIdentityConfiguration.CheckType.IDENTITY_V2
        on { isManualReviewEnabled } doReturn false
        on { isOnfidoReviewEnabled } doReturn false
    }
    private val featureConfigurations: FeatureConfigurations = mock {
        on { this.confirmIdentityConfiguration } doReturn confirmIdentityConfiguration
    }

    private val strings: Strings = mock {
        on { get(R.string.identity_check_process_verification_title) } doReturn "Verify your identity"
        on { get(R.string.identity_check_process_verification_selfie_description1) } doReturn MANUAL_REVIEW_TITLE
        on { get(R.string.identity_check_process_verification_selfie_description2) } doReturn SUBTITLE_ONE
        on { get(R.string.identity_check_process_verification_selfie_description3) } doReturn SUBTITLE_TWO
        on { get(R.string.identity_check_process_verification_selfie_automated_option_title) } doReturn AUTOMATED_STATE_TITLE_DISABLED
        on { get(R.string.identity_check_process_verification_selfie_automated_option_subtitle) } doReturn AUTOMATED_STATE_SUBTITLE_DISABLED
        on { get(R.string.identity_check_process_verification_selfie_manual_option_title) } doReturn MANUAL_STATE_TITLE_ENABLED
        on { get(R.string.identity_check_process_verification_selfie_manual_option_subtitle) } doReturn MANUAL_STATE_SUBTITLE_ENABLED
        on { get(R.string.identity_check_process_verification_selfie_recommended) } doReturn RECOMMENDED
    }

    private val viewModel by lazy {
        ConfirmIdentityViewModel(
            featureConfigurations,
            strings,
            analyticsVerifyIdentityProvider,
        )
    }

    @Test
    fun `Check ui state when manual review disabled `() = runTest {
        val uiStateTest = viewModel.uiState.test()

        uiStateTest.assertValue { uiState ->
            requireNotNull(uiState) { "Ui State is null" }
            with(uiState.topBarState) {
                navigation?.onClick?.invoke()
                title isEqualTo DISABLED_TITLE
                navigation?.icon isEqualTo R.drawable.uikit_ic_cross
            }
            uiState.icon isEqualTo IconState(id = R.drawable.uikit_illustration_badge_camera)
            uiState.title isEqualTo MANUAL_REVIEW_TITLE
            uiState.subtitle isEqualTo SUBTITLE_ONE + "\n" + SUBTITLE_TWO
            uiState.tag.isVisible.isFalse()
            uiState.manualReview.isVisible.isFalse()
            with(uiState.automatedCheckState) {
                title isEqualTo AUTOMATED_STATE_TITLE_DISABLED
                subtitle isEqualTo AUTOMATED_STATE_SUBTITLE_DISABLED
                rightOption isEqualTo UiKitDefaultRow.RightOption.ICON
                rightIcon isEqualTo R.drawable.uikit_ic_chevron_right
            }
            true
        }
        uiStateTest
            .assertNoMoreInteractions()
    }

    @Test
    fun `Check ui state when manual review enabled `() = runTest {
        whenever(confirmIdentityConfiguration.isManualReviewEnabled) doReturn true
        val uiStateTest = viewModel.uiState.test()
        uiStateTest.assertValue { uiState ->
            requireNotNull(uiState) { "Ui State is null" }
            with(uiState.topBarState) {
                navigation?.onClick?.invoke()
                title isEqualTo DISABLED_TITLE
                navigation?.icon isEqualTo R.drawable.uikit_ic_cross
            }
            uiState.icon isEqualTo IconState(id = R.drawable.uikit_illustration_badge_camera)
            uiState.title isEqualTo MANUAL_REVIEW_TITLE
            uiState.subtitle isEqualTo SUBTITLE_ONE + "\n" + SUBTITLE_TWO
            with(uiState.tag) {
                isVisible.isTrue()
                icon isEqualTo R.drawable.uikit_ic_clock
                color isEqualTo UIKitTagColor.Green
                isInverted.isFalse()
            }
            with(uiState.manualReview) {
                title isEqualTo MANUAL_STATE_TITLE_ENABLED
                subtitle isEqualTo MANUAL_STATE_SUBTITLE_ENABLED
                isVisible.isTrue()
                rightOption isEqualTo UiKitDefaultRow.RightOption.ICON
                rightIcon isEqualTo R.drawable.uikit_ic_chevron_right
                onClickAction?.invoke(this)
            }
            with(uiState.automatedCheckState) {
                title isEqualTo AUTOMATED_STATE_TITLE_DISABLED
                subtitle isEqualTo AUTOMATED_STATE_SUBTITLE_DISABLED
                rightOption isEqualTo UiKitDefaultRow.RightOption.ICON
                rightIcon isEqualTo R.drawable.uikit_ic_chevron_right
            }
            true
        }
        uiStateTest
            .assertNoMoreInteractions()
    }

    @Test
    fun `should emit correct navigation events when ui elements pressed`() = runTest {
        whenever(confirmIdentityConfiguration.isManualReviewEnabled) doReturn true
        val navigationTest = viewModel.navigation.test()
        val uiState = viewModel.uiState.value
        uiState.topBarState.navigation?.onClick?.invoke()
        uiState.automatedCheckState.onClickAction?.invoke(uiState.automatedCheckState)
        uiState.manualReview.onClickAction?.invoke(uiState.manualReview)

        navigationTest
            .assertValue(CommonDirections.close)
            .assertValue(ConfirmIdentityNavigator.startAutomatedCheck)
            .assertValue(ConfirmIdentityNavigator.startManualReview)
            .assertNoMoreInteractions()
    }

    companion object {
        private const val MANUAL_REVIEW_TITLE = "To verify your identity, you’ll need to take a selfie."
        private const val SUBTITLE_ONE =
            "We’ll occasionally ask you to do this to make sure no one else is trying to access your rider account."
        private const val SUBTITLE_TWO =
            "Your selfie is automatically compared to the selfie and ID document you provided during your rider application."
        private const val DISABLED_TITLE = "Verify your identity"
        private const val MANUAL_STATE_TITLE_ENABLED = "Manual option"
        private const val MANUAL_STATE_SUBTITLE_ENABLED = "Deliveroo will review a photo of you and your ID document"
        private const val AUTOMATED_STATE_TITLE_DISABLED = "Automated identity check"
        private const val AUTOMATED_STATE_SUBTITLE_DISABLED = "With our identity provider Onfido"
        private const val RECOMMENDED = "Recommended"
    }
}

    """.trimIndent()

    @Language("kotlin")
    val testDataExpected1 = """
       import io.mockk.verify
import io.mockk.clearMocks
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
        val mockDependency1 = mockk(relaxed = true,Dependency1::class.java)
        val mockDependency2 = mockk(relaxed = true,Dependency2::class.java)

        // Define behavior for mocks
        every { mockDependency1.someMethod(anyString()) } returns 42

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
        val mockDependency1 = mockk(relaxed = true,Dependency1::class.java)
        val mockDependency2 = mockk(relaxed = true,Dependency2::class.java)

        // Define behavior for mocks
        every { mockDependency1.someMethod(anyString()) } returns 42

        // Create an instance of the class under test
        val sampleClass = SampleClass(mockDependency1, mockDependency2)

        // Call the method to be tested
        sampleClass.doSomething()

        // Verify interactions
        verify { mockDependency1.someMethod("input") }
        verify { mockDependency2.anotherMethod(42) }

        // Additional verification using ArgumentCaptor
        val captor = ArgumentCaptor.forClass(Int::class.java)
        verify { mockDependency2.anotherMethod(capture(captor)) }
        assert(captor.value == 42)
    }
}
    """.trimIndent()
}