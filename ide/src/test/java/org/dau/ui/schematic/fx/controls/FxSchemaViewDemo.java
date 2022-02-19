package org.dau.ui.schematic.fx.controls;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.dau.runtime.Block;
import org.dau.ui.schematic.fx.model.FxBlock;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.dau.ui.schematic.fx.theme.ThemeApplier;

import java.util.Random;

public class FxSchemaViewDemo {

  public static class App extends Application {
    @Override
    public void init() {
      Platform.runLater(ThemeApplier::apply);
    }

    @Override
    public void start(Stage primaryStage) {
      var schema = new FxSchema();
      var random = new Random(10000L);
      for (int i = 0; i < 5; i++) {
        var block = new FxBlock(
          schema,
          TestClass.class.getConstructors()[0]
        );
        block.x.set(random.nextDouble(600d));
        block.y.set(random.nextDouble(500d));
        schema.addBlock(block);
      }
      var schemaView = new FxSchemaView(schema);
      var group = new Group(schemaView);
      group.setFocusTraversable(false);
      var scrollPane = new ScrollPane(group);
      scrollPane.setFocusTraversable(false);
      var scene = new Scene(scrollPane, 800, 600);
      scene.setFill(Color.BLACK);
      primaryStage.setScene(scene);
      primaryStage.show();
    }
  }

  public static void main(String... args) {
    Application.launch(App.class, args);
  }

  @Block("Test block")
  public static final class TestClass {

    public TestClass(String sync, String name, int code, double len, double angle, double speed) {
    }

    public double distance() {
      return 0d;
    }

    public double out() {
      return 0d;
    }

    public double delta() {
      return 0;
    }
  }
}
