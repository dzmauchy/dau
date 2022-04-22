package org.dau.ide.main

import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MainPane(tabPane: MainProjectTabs, menuBar: MainMenuBar) : BorderPane(tabPane, menuBar, null, null, null) {

  @Autowired
  fun initPrimaryStage(@MainQualifier primaryStage: Stage) {
    val scene = Scene(this, 800.0, 600.0)
    scene.fill = Color.BLACK
    primaryStage.scene = scene
  }
}
