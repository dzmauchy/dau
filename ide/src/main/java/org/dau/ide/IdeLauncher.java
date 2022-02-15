package org.dau.ide;

import javafx.application.Application;
import org.dau.ide.logging.FxLogConfig;
import org.dau.ide.logging.FxLogManager;

public class IdeLauncher {

  public static void main(String... args) throws Exception {
    System.setProperty("java.util.logging.manager", FxLogManager.class.getName());
    System.setProperty("java.util.logging.config.class", FxLogConfig.class.getName());
    Application.launch(Ide.class, args);
  }
}
