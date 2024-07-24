package configuration

object GlobalConfig {
    var relaxed: Boolean = true
    var removeEq: Boolean = true

    val relaxesStatement get() = if (relaxed) "relaxed = true, " else ""
}