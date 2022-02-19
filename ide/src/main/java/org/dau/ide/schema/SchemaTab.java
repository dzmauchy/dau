package org.dau.ide.schema;

import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Tab;
import org.dau.di.Ctx;
import org.dau.ide.main.MainTabPane;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SchemaTab extends Tab {

  public final FxSchema schema;
  private final ListChangeListener<Tab> lcl;

  public SchemaTab(Ctx ctx, FxSchema schema) {
    this.schema = schema;
    textProperty().bind(schema.name);
    setGraphic(IconFactory.icon("icons/blocks.png", 20));
    setClosable(true);
    lcl = c -> {
      while (c.next()) {
        if (c.wasRemoved()) {
          for (var tab : c.getRemoved()) {
            if (tab instanceof SchemaTab t) {
              if (t.schema == SchemaTab.this.schema) {
                ctx.close();
              }
            }
          }
        }
      }
    };
  }

  @Autowired
  public void initWith(SchemaPane schemaPane) {
    setContent(schemaPane);
  }

  @Autowired
  public void initWith(MainTabPane tabPane) {
    tabPane.getTabs().add(this);
    tabPane.getTabs().addListener(new WeakListChangeListener<>(lcl));
  }
}
