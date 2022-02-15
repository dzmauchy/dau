package org.dau.ui.schematic.fx.controls;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.dau.ui.schematic.fx.model.FxBlock;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.dau.ui.schematic.fx.theme.ThemeApplier;
import org.dau.ui.schematic.layout.model.InputInfo;
import org.dau.ui.schematic.layout.model.OutputInfo;

import java.util.List;
import java.util.Random;

public class FxSchemaViewDemo {

  public static class App extends Application {
    @Override
    public void init() {
      Platform.runLater(ThemeApplier::apply);
    }

    @Override
    public void start(Stage primaryStage) {
      var schema = new FxSchema("mySchema");
      var random = new Random(10000L);
      for (int i = 0; i < 5; i++) {
        var block = new FxBlock(
          schema,
          "block " + i,
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
        block.xProperty().set(random.nextDouble(600d));
        block.yProperty().set(random.nextDouble(500d));
      }
      var schemaView = new FxSchemaView(schema);
      var scrollPane = new ScrollPane(new Group(schemaView));
      var scene = new Scene(scrollPane, 800, 600);
      scene.setFill(Color.BLACK);
      primaryStage.setScene(scene);
      primaryStage.show();
    }
  }

  public static void main(String... args) {
    Application.launch(App.class, args);
  }
}
