package tools

import replace.ImportsConverter

internal object StartingPoints {
    val mockingPredicate = listOf(
        "whenever(",
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
        "mock(" to ReplaceOnlyData("mockk(relaxed = true,") {},
        "mock<" to ReplaceOnlyData("mockk<") {},
        "= mock {" to ReplaceOnlyData("= mockk {") {},
        "mock {" to ReplaceOnlyData("mockk {") {},
        "spy {" to ReplaceOnlyData("spyK {") {},
        "spy(" to ReplaceOnlyData("= spyK(") {},
        "@Mock" to ReplaceOnlyData("@Mockk") {},
        "@Spy" to ReplaceOnlyData("@SpyK") {},
        "Mockito.reset(" to ReplaceOnlyData("clearMocks(")
        { ImportsConverter.addImports("io.mockk.clearMocks") },
    )
}

internal data class ReplaceOnlyData(
    val replaceWith: String,
    val performAdditionalAction: () -> Unit,
)