package org.dau.ide.main;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class MainPane extends BorderPane {

  public MainPane(MainTabPane tabPane, MainMenuBar menuBar) {
    super(tabPane, menuBar, null, null, null);
  }

  @Autowired
  public void initPrimaryStage(@MainQualifier Stage primaryStage) {
    var scene = new Scene(this, 800d, 600d);
    scene.setFill(Color.BLACK);
    primaryStage.setScene(scene);
  }
}
