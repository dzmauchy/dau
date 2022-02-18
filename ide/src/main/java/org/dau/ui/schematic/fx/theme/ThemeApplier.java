package org.dau.ui.schematic.fx.theme;

import com.sun.javafx.css.StyleManager;
import javafx.application.Application;

import static javafx.application.Application.STYLESHEET_CASPIAN;

public interface ThemeApplier {

  static void apply() {
    var classLoader = Thread.currentThread().getContextClassLoader();
    var themeUrl = classLoader.getResource("theme.css");
    Application.setUserAgentStylesheet(STYLESHEET_CASPIAN);
    var styleManager = StyleManager.getInstance();
    if (themeUrl != null) {
      styleManager.addUserAgentStylesheet(themeUrl.toString());
    }
  }
}
