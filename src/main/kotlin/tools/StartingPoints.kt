package tools

import migration.ImportsConverter

internal object StartingPoints {
    val mockingPredicate = listOf(
        "whenever(",
        "wheneverBlocking(",
        "wheneverBlocking {",
        "on {",
        "on \n{",
        "`when`(",
    )
    val returnPredicate = listOf(
        ".doReturn(",
        ".thenReturn(",
    )

    val justToReplace = mapOf(
        "doReturn" to ReplaceOnlyData("returns") {},
        "doAnswer" to ReplaceOnlyData("answers") {},
        "doThrow" to ReplaceOnlyData("throws") {},
        "mock(" to ReplaceOnlyData("mockk(relaxed = true,") {},
        "mock<" to ReplaceOnlyData("mockk<") {},
        "= mock {" to ReplaceOnlyData("= mockk {") {},
        "mock {" to ReplaceOnlyData("mockk {") {},
        "spy {" to ReplaceOnlyData("spyK {") {},
        ".stub {" to ReplaceOnlyData(".let {") {},
        "onBlocking" to ReplaceOnlyData("coEvery") {},
        "spy(" to ReplaceOnlyData("spyk(") {},
        "@Mock" to ReplaceOnlyData("@MockK") { ImportsConverter.addImports("io.mockk.impl.annotations.MockK") },
        "@Spy" to ReplaceOnlyData("@SpyK") { ImportsConverter.addImports("io.mockk.impl.annotations.SpyK") },
        "Mockito." to ReplaceOnlyData("") {},
        "Mockito.reset(" to ReplaceOnlyData("clearMocks(")
        { ImportsConverter.addImports("io.mockk.clearMocks") },
    )

    val argumentCaptorsPredicate = listOf(
        "= argumentCaptor" to false,
        "ArgumentCaptor.forClass(" to true
    )
}

internal data class ReplaceOnlyData(
    val replaceWith: String,
    val performAdditionalAction: () -> Unit,
)