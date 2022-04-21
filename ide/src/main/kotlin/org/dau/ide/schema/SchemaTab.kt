package org.dau.ide.schema

import javafx.scene.control.Tab
import org.dau.di.Ctx
import org.dau.ide.project.ProjectTabs
import org.dau.ui.icons.IconFactory
import org.dau.ui.schematic.model.FxSchema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component

@Component
class SchemaTab(val ctx: Ctx, val schema: FxSchema, schemaPane: SchemaPane) : Tab() {

  init {
    textProperty().bind(schema.name)
    graphic = IconFactory.icon("icons/blocks.png", 20)
    content = schemaPane
    setOnCloseRequest { ev -> ctx.close() }
  }

  @Autowired
  fun initWith(tabPane: ProjectTabs) {
    tabPane.tabs.add(this)
    tabPane.selectionModel.selectLast()
    ctx.addApplicationListener(ApplicationListener { _: ContextClosedEvent -> tabPane.tabs.remove(this) })
  }
}
