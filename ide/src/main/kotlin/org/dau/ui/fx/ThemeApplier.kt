package org.dau.ui.fx

import com.sun.javafx.css.StyleManager
import javafx.application.Application

import javafx.application.Application.STYLESHEET_MODENA

object ThemeApplier {

  fun invoke() {
    val classLoader = Thread.currentThread().contextClassLoader
    val themeUrl = classLoader.getResource("theme.css")
    Application.setUserAgentStylesheet(STYLESHEET_MODENA)
    val styleManager = StyleManager.getInstance()
    if (themeUrl != null) {
      styleManager.addUserAgentStylesheet(themeUrl.toString())
    }
  }
}
