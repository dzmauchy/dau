package org.dau.ide.logging

import java.util.logging.Filter
import java.util.logging.LogRecord

object FxIdeLogFilter : Filter {
  override fun isLoggable(record: LogRecord): Boolean {
    val logger = record.loggerName ?: return false
    val message = record.message ?: return false
    when (logger) {
      "javafx" -> if (message.startsWith("Unsupported JavaFX configuration: classes were loaded")) {
        return false
      }
    }
    return true
  }
}
