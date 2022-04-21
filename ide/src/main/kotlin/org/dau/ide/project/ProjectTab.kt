package org.dau.ide.project

import javafx.scene.control.Tab
import org.dau.di.Ctx
import org.dau.ide.main.MainProjectTabs
import org.dau.ui.icons.IconFactory
import org.dau.ui.schematic.model.FxProject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProjectTab(val project: FxProject, tabs: ProjectTabs, val ctx: Ctx) : Tab() {

  init {
    graphic = IconFactory.icon("icons/project.png", 20)
    textProperty().bind(project.name)
    content = tabs
    setOnCloseRequest { ctx.close() }
  }

  @Autowired
  fun initWith(tabs: MainProjectTabs) {
    tabs.tabs.add(this)
  }

  @EventListener
  fun onClose(event: ContextClosedEvent) {
    assert(event.source != null)
    val tabPane = tabPane
    tabPane?.tabs?.remove(this)
  }
}
