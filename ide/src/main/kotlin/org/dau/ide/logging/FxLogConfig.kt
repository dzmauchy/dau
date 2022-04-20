package org.dau.ide.logging

import java.util.logging.Logger

class FxLogConfig {
  init {
    val logger = Logger.getLogger("")
    logger.addHandler(FxConsoleHandler)
    logger.addHandler(FxInitLogHandler)
  }
}
