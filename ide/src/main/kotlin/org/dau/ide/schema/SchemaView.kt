package org.dau.ide.schema

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import javafx.scene.Group
import javafx.scene.control.ScrollPane
import javafx.scene.control.Spinner
import org.dau.ide.action.FxAction
import org.dau.ide.action.ToolbarAction
import org.dau.ui.schematic.controls.FxSchemaView
import org.dau.ui.schematic.model.FxBlock
import org.dau.ui.schematic.model.FxSchema
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@Component
class SchemaView(schema: FxSchema) : ScrollPane() {

  private val view: FxSchemaView
  private val group: Group
  private val scale = SimpleDoubleProperty(1.0)
  private val onScaleChange = ChangeListener<Double> { _, _, nv -> onScaleChange(nv) }

  init {
    view = FxSchemaView(schema)
    group = Group(view)
    view.scaleXProperty().bind(scale)
    view.scaleYProperty().bind(scale)
    content = group
    isPannable = true
  }

  @ToolbarAction
  @SchemaBean
  fun addDemoAction(): FxAction {
    return FxAction("icons/demo.png", "Add a demo block")
      .on {
        val block = FxBlock(view.schema, SchemaDemoBlock::class.java.constructors[0])
        block.x.set(ThreadLocalRandom.current().nextDouble(500.0))
        block.y.set(ThreadLocalRandom.current().nextDouble(500.0))
        view.schema.addBlock(block)
      }
  }

  fun scaleSpinner(): Spinner<Double> {
    val spinner = Spinner<Double>(0.1, 10.0, scale.get(), 0.1)
    spinner.valueProperty().addListener(WeakChangeListener(onScaleChange))
    spinner.isFocusTraversable = false
    return spinner
  }

  private fun onScaleChange(nv: Double?) {
    scale.set(nv!!)
  }
}
