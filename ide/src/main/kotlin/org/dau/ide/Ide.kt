package org.dau.ide

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import org.dau.di.Ctx
import org.dau.ide.logging.FxInitLogHandler
import org.dau.ide.logging.FxLogHandler
import org.dau.ide.main.MainConf
import org.dau.ui.schematic.fx.theme.ThemeApplier
import java.util.function.Supplier

import java.util.logging.Logger

class Ide : Application() {

  private val ctx = Ctx("Root")

  override fun init() {
    //logging
    val rootLogger = Logger.getLogger("")
    val fxHandler = FxLogHandler()
    rootLogger.addHandler(fxHandler)
    rootLogger.removeHandler(FxInitLogHandler.INSTANCE)
    fxHandler.copyFrom(FxInitLogHandler.INSTANCE)

    // first logging
    val logger = Logger.getLogger("init")
    val process = ProcessHandle.current()
    logger.info { "Process " + process.pid() + "; " + process.info().commandLine().orElse("") }

    // theme
    Platform.runLater(ThemeApplier::apply)
  }

  override fun start(primaryStage: Stage) {
    ctx.registerBean(MainConf::class.java, Supplier { MainConf(primaryStage) })
    ctx.refresh()
    ctx.start()
  }

  override fun stop() {
    ctx.close()
  }
}
