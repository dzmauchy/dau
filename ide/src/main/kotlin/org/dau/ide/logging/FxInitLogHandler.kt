package org.dau.ide.logging

import javafx.collections.ObservableList

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

object FxInitLogHandler : Handler() {

  private val records = ConcurrentLinkedQueue<LogRecord>()

  init {
    filter = FxIdeLogFilter
    level = Level.ALL
  }

  override fun publish(record: LogRecord) {
    if (isLoggable(record)) {
      records.offer(record)
    }
  }

  override fun flush() {}
  override fun close() = records.clear()
  fun drainTo(records: ObservableList<LogRecord>) = records.addAll(0, this.records)
}
