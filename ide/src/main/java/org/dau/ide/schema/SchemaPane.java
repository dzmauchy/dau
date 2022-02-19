package org.dau.ide.schema;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.dau.di.Ctx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.dau.ide.action.FxActions.fillToolbar;

@Component
public class SchemaPane extends BorderPane {

  private final ToolBar toolBar = new ToolBar();

  public SchemaPane() {
    setTop(toolBar);
  }

  @Autowired
  public void initWith(SchemaView schemaView) {
    setCenter(schemaView);
  }

  @Autowired
  public void initWith(Ctx ctx, SchemaView schemaView) {
    fillToolbar(ctx, toolBar, SchemaQualifier.class);
    toolBar.getItems().add(new Separator(Orientation.HORIZONTAL));
    toolBar.getItems().add(schemaView.scaleSpinner());
  }
}
