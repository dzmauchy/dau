package org.dau.ide

import javafx.application.Application
import org.dau.ide.logging.FxLogConfig
import org.dau.ide.logging.FxLogManager

object IdeLauncher {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("java.util.logging.manager", FxLogManager::class.java.name)
    System.setProperty("java.util.logging.config.class", FxLogConfig::class.java.name)
    Application.launch(Ide::class.java, *args)
  }
}