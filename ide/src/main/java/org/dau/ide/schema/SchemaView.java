package org.dau.ide.schema;

import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import org.dau.ide.action.FxAction;
import org.dau.ide.action.ToolbarAction;
import org.dau.ui.schematic.controls.FxSchemaView;
import org.dau.ui.schematic.model.FxBlock;
import org.dau.ui.schematic.model.FxSchema;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public final class SchemaView extends ScrollPane {

  private final FxSchemaView view;
  private final Group group;
  private final SimpleDoubleProperty scale = new SimpleDoubleProperty(1.0);
  private final ChangeListener<Double> onScaleChange = this::onScaleChange;

  public SchemaView(FxSchema schema) {
    view = new FxSchemaView(schema);
    group = new Group(view);
    view.scaleXProperty().bind(scale);
    view.scaleYProperty().bind(scale);
    setContent(group);
    setPannable(true);
  }

  @ToolbarAction
  @SchemaBean
  public FxAction addDemoAction() {
    return new FxAction("icons/demo.png", "Add a demo block")
      .on(() -> {
        var block = new FxBlock(view.getSchema(), SchemaDemoBlock.class.getConstructors()[0]);
        block.getX().set(ThreadLocalRandom.current().nextDouble(500d));
        block.getY().set(ThreadLocalRandom.current().nextDouble(500d));
        view.getSchema().addBlock(block);
      });
  }

  public Spinner<Double> scaleSpinner() {
    var spinner = new Spinner<Double>(0.1, 10.0, scale.get(), 0.1);
    spinner.valueProperty().addListener(new WeakChangeListener<>(onScaleChange));
    spinner.setFocusTraversable(false);
    return spinner;
  }

  private void onScaleChange(Observable o, Double ov, Double nv) {
    scale.set(nv);
  }
}
