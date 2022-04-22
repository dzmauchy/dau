package org.dau.ide.schema

import javafx.geometry.Orientation
import javafx.scene.control.Separator
import javafx.scene.control.ToolBar
import javafx.scene.layout.BorderPane
import org.dau.di.Ctx
import org.dau.ui.action.FxAction.Companion.fillToolbar
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SchemaPane : BorderPane() {

  private val toolBar = ToolBar().also { top = it }

  @Autowired
  fun initWith(schemaView: SchemaView) {
    center = schemaView
  }

  @Autowired
  fun initWith(ctx: Ctx, schemaView: SchemaView) {
    fillToolbar(ctx, toolBar, SchemaQualifier::class.java)
    toolBar.items.add(Separator(Orientation.HORIZONTAL))
    toolBar.items.add(schemaView.scaleSpinner())
  }
}
