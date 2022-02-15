package org.dau.ide.logging;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public final class FxLogHandler extends Handler {

  private final ObservableList<LogRecord> records = FXCollections.observableArrayList();
  private final SimpleIntegerProperty limit = new SimpleIntegerProperty(10_000);

  public FxLogHandler() {
    limit.addListener((observable, oldValue, newValue) -> {
      if (newValue.intValue() < oldValue.intValue()) {
        records.remove(0, oldValue.intValue() - newValue.intValue());
      }
    });
  }

  @Override
  public void publish(LogRecord record) {
    Platform.runLater(() -> {
      var size = records.size();
      var lim = limit.get();
      if (size >= lim) {
        records.remove(0, size - lim + 1);
      }
      records.add(record);
    });
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
  }

  public void copyFrom(FxInitLogHandler handler) {
    handler.drainTo(records);
  }

  public SimpleIntegerProperty limitProperty() {
    return limit;
  }
}
