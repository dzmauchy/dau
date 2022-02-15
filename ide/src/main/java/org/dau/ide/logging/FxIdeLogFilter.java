package org.dau.ide.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public final class FxIdeLogFilter implements Filter {

  public static final FxIdeLogFilter FILTER = new FxIdeLogFilter();

  private FxIdeLogFilter() {
  }

  @Override
  public boolean isLoggable(LogRecord record) {
    var logger = record.getLoggerName();
    if (logger == null) {
      return false;
    }
    var message = record.getMessage();
    if (message == null) {
      return false;
    }
    switch (logger) {
      case "javafx" -> {
        if (message.startsWith("Unsupported JavaFX configuration: classes were loaded")) {
          return false;
        }
      }
    }
    return true;
  }
}
