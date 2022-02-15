package org.dau.ui.schematic.fx.controls;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.dau.ui.schematic.fx.model.FxBlock;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.dau.ui.schematic.fx.theme.ThemeApplier;
import org.dau.ui.schematic.layout.model.InputInfo;
import org.dau.ui.schematic.layout.model.OutputInfo;

import java.util.List;

public class FxBlockViewDemo {

  public static class App extends Application {

    @Override
    public void init() {
      Platform.runLater(ThemeApplier::apply);
    }

    @Override
    public void start(Stage primaryStage) {
      var schema = new FxSchema("id");
      var block = new FxBlock(
        schema,
        "block1",
        List.of(
          new InputInfo("clock"),
          new InputInfo("sync"),
          new InputInfo("x"),
          new InputInfo("y"),
          new InputInfo("z")
        ),
        List.of(
          new OutputInfo("distance"),
          new OutputInfo("delta"),
          new OutputInfo("out")
        )
      );
      var view = new FxBlockView(block);
      var pane = new Group(view);
      var scene = new Scene(new StackPane(pane), 800, 600);
      scene.setFill(Color.BLACK);
      primaryStage.setScene(scene);
      primaryStage.show();
    }
  }

  public static void main(String... args) {
    Application.launch(App.class, args);
  }
}
