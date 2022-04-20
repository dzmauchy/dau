package org.dau.ide.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.lang.management.ManagementFactory
import java.text.MessageFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField.*
import java.util.*
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

object FxConsoleHandler : Handler() {

  private val DTF =
    DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2)
      .appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).appendLiteral('.').appendValue(MILLI_OF_SECOND, 3)
      .toFormatter(Locale.US)

  private val THREADS = ManagementFactory.getThreadMXBean()

  init {
    filter = FxIdeLogFilter
    level = Level.INFO
  }

  override fun publish(record: LogRecord) {
    if (!isLoggable(record)) {
      return
    }
    val sw = StringWriter(64)
    val buffer = sw.buffer
    val instant = record.instant.atZone(ZoneId.systemDefault())
    DTF.formatTo(instant, buffer)
    buffer.append(' ')
    buffer.append(record.level.name)
    buffer.append(' ')
    buffer.append(record.loggerName)
    buffer.append(" [")
    val threadInfo = THREADS.getThreadInfo(record.longThreadID)
    if (threadInfo == null) {
      buffer.append(record.longThreadID)
    } else {
      buffer.append(threadInfo.threadName)
    }
    buffer.append("] ")
    val params = record.parameters
    if (params == null || params.size == 0) {
      buffer.append(record.message)
    } else {
      try {
        val mf = MessageFormat(record.message, Locale.US)
        mf.format(params, buffer, null)
      } catch (e: Throwable) {
        e.printStackTrace(System.err)
        buffer.append(record.message)
      }

    }
    buffer.append(System.lineSeparator())
    val thrown = record.thrown
    if (thrown != null) {
      PrintWriter(sw).use { pw -> thrown.printStackTrace(pw) }
    }
    print(sw)
  }

  override fun flush() {}
  override fun close() {}
}
