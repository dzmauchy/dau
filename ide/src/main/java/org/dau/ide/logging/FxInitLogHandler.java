package org.dau.ide.logging;

import javafx.collections.ObservableList;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class FxInitLogHandler extends Handler {

  public static final FxInitLogHandler INSTANCE = new FxInitLogHandler();

  private final ConcurrentLinkedQueue<LogRecord> records = new ConcurrentLinkedQueue<>();

  private FxInitLogHandler() {
    setFilter(FxIdeLogFilter.FILTER);
    setLevel(Level.ALL);
  }

  @Override
  public void publish(LogRecord record) {
    if (isLoggable(record)) {
      records.offer(record);
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
    records.clear();
  }

  public void drainTo(ObservableList<LogRecord> records) {
    records.addAll(0, this.records);
  }
}
