package org.dau.ide.logging

import java.util.logging.LogManager

class FxLogManager : LogManager() {

  init {
    super.reset()
  }

  override fun reset() {}
}
