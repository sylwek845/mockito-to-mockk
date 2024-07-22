package logs

import com.intellij.execution.ui.ConsoleViewContentType

object LogKeeper {
    private val logList = mutableListOf<Log>()
    val logs: List<Log> get() = logList

    fun clear() {
        logList.clear()
    }

    fun logInfo(message: String) {
        logList.add(Log(message, LogType.Info))
    }

    fun logError(message: String) {
        logList.add(Log(message, LogType.Error))
    }

    fun logWarning(message: String) {
        logList.add(Log(message, LogType.Warning))
    }

    fun getLogString(): String {
        return logList.reversed().joinToString(separator = "\n") { log ->
            "**${log.logType}** -> $log"
        }
    }

    fun print() {
        println(getLogString())
    }
}

enum class LogType(internal val logType: ConsoleViewContentType) {
    Warning(logType = ConsoleViewContentType.LOG_WARNING_OUTPUT),
    Error(logType = ConsoleViewContentType.ERROR_OUTPUT),
    Info(logType = ConsoleViewContentType.LOG_INFO_OUTPUT),
}

data class Log(
    val message: String,
    val logType: LogType
)