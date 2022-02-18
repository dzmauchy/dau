package org.dau.ide;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.dau.di.Ctx;
import org.dau.ide.logging.FxInitLogHandler;
import org.dau.ide.logging.FxLogHandler;
import org.dau.ide.main.MainConf;
import org.dau.ui.schematic.fx.theme.ThemeApplier;

import java.util.logging.Logger;

public class Ide extends Application {

  private final Ctx ctx = new Ctx("root", "Root");

  @Override
  public void init() throws Exception {
    //logging
    var rootLogger = Logger.getLogger("");
    var fxHandler = new FxLogHandler();
    rootLogger.addHandler(fxHandler);
    rootLogger.removeHandler(FxInitLogHandler.INSTANCE);
    fxHandler.copyFrom(FxInitLogHandler.INSTANCE);

    // first logging
    var logger = Logger.getLogger("init");
    var process = ProcessHandle.current();
    logger.info(() -> "Process " + process.pid() + "; " + process.info().commandLine().orElse(""));

    // theme
    Platform.runLater(ThemeApplier::apply);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    var button = new Button("X");
    primaryStage.setScene(new Scene(button, 800, 600));
    ctx.registerBean(MainConf.class, () -> new MainConf(primaryStage));
    ctx.refresh();
    ctx.start();
  }

  @Override
  public void stop() {
    ctx.close();
  }
}
