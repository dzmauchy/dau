package org.dau.ide.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class FxConsoleHandler extends Handler {

  public static final FxConsoleHandler INSTANCE = new FxConsoleHandler();

  private static final DateTimeFormatter DTF = new DateTimeFormatterBuilder()
    .appendValue(ChronoField.HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
    .appendLiteral('.')
    .appendValue(ChronoField.MILLI_OF_SECOND, 3)
    .toFormatter(Locale.US);

  private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

  private FxConsoleHandler() {
    setFilter(FxIdeLogFilter.FILTER);
    setLevel(Level.INFO);
  }

  @Override
  public void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    var sw = new StringWriter(64);
    var buffer = sw.getBuffer();
    var instant = record.getInstant().atZone(ZoneId.systemDefault());
    DTF.formatTo(instant, buffer);
    buffer.append(' ');
    buffer.append(record.getLevel().getName());
    buffer.append(' ');
    buffer.append(record.getLoggerName());
    buffer.append(" [");
    var threadInfo = THREADS.getThreadInfo(record.getLongThreadID());
    if (threadInfo == null) {
      buffer.append(record.getLongThreadID());
    } else {
      buffer.append(threadInfo.getThreadName());
    }
    buffer.append("] ");
    var params = record.getParameters();
    if (params == null || params.length == 0) {
      buffer.append(record.getMessage());
    } else {
      try {
        var mf = new MessageFormat(record.getMessage(), Locale.US);
        mf.format(params, buffer, null);
      } catch (Throwable e) {
        e.printStackTrace(System.err);
        buffer.append(record.getMessage());
      }
    }
    buffer.append(System.lineSeparator());
    var thrown = record.getThrown();
    if (thrown != null) {
      try (var pw = new PrintWriter(sw)) {
        thrown.printStackTrace(pw);
      }
    }
    System.out.print(sw);
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
  }
}
