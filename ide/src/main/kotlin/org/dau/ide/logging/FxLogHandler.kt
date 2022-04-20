package org.dau.ide.logging

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import java.util.logging.Handler
import java.util.logging.LogRecord

class FxLogHandler : Handler() {

  private val records = FXCollections.observableArrayList<LogRecord>()
  private val limit = SimpleIntegerProperty(10000)

  init {
    limit.addListener { _, oldValue, newValue ->
      if (newValue.toInt() < oldValue.toInt()) {
        records.remove(0, oldValue.toInt() - newValue.toInt())
      }
    }
  }

  override fun publish(record: LogRecord) {
    Platform.runLater {
      val size = records.size
      val lim = limit.get()
      if (size >= lim) {
        records.remove(0, size - lim + 1)
      }
      records.add(record)
    }
  }

  override fun flush() {}
  override fun close() {}

  fun copyFrom(handler: FxInitLogHandler) {
    handler.drainTo(records)
  }

  fun limitProperty(): SimpleIntegerProperty {
    return limit
  }
}
