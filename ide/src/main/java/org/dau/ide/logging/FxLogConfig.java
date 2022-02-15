package org.dau.ide.logging;

import java.util.logging.Logger;

public final class FxLogConfig {

  public FxLogConfig() {
    var logger = Logger.getLogger("");
    logger.addHandler(FxConsoleHandler.INSTANCE);
    logger.addHandler(FxInitLogHandler.INSTANCE);
  }
}
