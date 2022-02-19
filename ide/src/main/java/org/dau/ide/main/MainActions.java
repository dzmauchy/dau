package org.dau.ide.main;

import javafx.beans.binding.Bindings;
import org.dau.ide.action.FxAction;
import org.dau.ide.main.menu.SchemaGroup;
import org.dau.ide.main.menu.ViewGroup;
import org.dau.ide.schema.SchemaTab;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.stereotype.Component;

@Component
public class MainActions {

  @MainBean
  @SchemaGroup
  public FxAction createSchemaAction(MainSchemas schemas) {
    return new FxAction("icons/create.png", "Create a new schema")
      .on(() -> schemas.schemas.add(new FxSchema()));
  }

  @MainBean
  @SchemaGroup
  public FxAction removeSchemaAction(MainTabPane pane) {
    return new FxAction("icons/remove.png", "Remove the selected schema")
      .disabled(Bindings.createBooleanBinding(() -> {
        var tab = pane.getSelectionModel().getSelectedItem();
        return !(tab instanceof SchemaTab);
      }, pane.getSelectionModel().selectedItemProperty()))
      .on(ev -> pane.getTabs().remove(pane.getSelectionModel().getSelectedIndex()));
  }

  @MainBean
  @ViewGroup
  public FxAction selectThemeAction() {
    return new FxAction("Select theme").on(ev -> {

    });
  }
}
