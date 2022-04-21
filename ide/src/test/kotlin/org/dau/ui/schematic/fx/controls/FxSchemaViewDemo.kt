package org.dau.ui.schematic.fx.controls

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.dau.runtime.Block
import org.dau.ui.fx.ThemeApplier
import org.dau.ui.schematic.controls.FxSchemaView
import org.dau.ui.schematic.model.FxBlock
import org.dau.ui.schematic.model.FxSchema
import java.util.*

object FxSchemaViewDemo {

  class App : Application() {
    override fun init() {
      Platform.runLater(ThemeApplier::invoke)
    }

    override fun start(primaryStage: Stage) {
      val schema = FxSchema()
      val random = Random(10000L)
      for (i in 0..4) {
        val block = FxBlock(
          schema, TestClass::class.java.constructors[0]
        )
        block.x.set(random.nextDouble(600.0))
        block.y.set(random.nextDouble(500.0))
        schema.addBlock(block)
      }
      val schemaView = FxSchemaView(schema)
      val group = Group(schemaView)
      group.isFocusTraversable = false
      val scrollPane = ScrollPane(group)
      scrollPane.isFocusTraversable = false
      val scene = Scene(scrollPane, 800.0, 600.0)
      scene.fill = Color.BLACK
      primaryStage.scene = scene
      primaryStage.show()
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
  }

  @Suppress("UNUSED_PARAMETER")
  @Block("Test block")
  class TestClass(sync: String, name: String, code: Int, len: Double, angle: Double, speed: Double) {

    fun distance(): Double {
      return 0.0
    }

    fun out(): Double {
      return 0.0
    }

    fun delta(): Double {
      return 0.0
    }
  }
}
