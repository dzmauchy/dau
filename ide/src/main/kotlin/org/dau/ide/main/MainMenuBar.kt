package org.dau.ide.main

import javafx.scene.control.MenuBar
import org.dau.ui.action.FxAction.Companion.fillMenuBar
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MainMenuBar : MenuBar() {

  @EventListener
  fun onRefresh(event: ContextRefreshedEvent) {
    fillMenuBar(event.applicationContext, this, MainQualifier::class.java)
  }
}
