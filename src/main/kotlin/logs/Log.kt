package logs

object LogKeeper {
    private val logList = mutableListOf<Log>()

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

    fun logCritical(message: String) {
        logList.add(Log(message, LogType.Critical))
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

enum class LogType {
    Warning, Error, Info, Critical
}

data class Log(
    val message: String,
    val logType: LogType
)