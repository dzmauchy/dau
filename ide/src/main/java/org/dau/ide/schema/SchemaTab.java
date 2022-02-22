package org.dau.ide.schema;

import javafx.scene.control.Tab;
import org.dau.di.Ctx;
import org.dau.ide.project.SchemaTabs;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class SchemaTab extends Tab {

  public final FxSchema schema;
  public final Ctx ctx;

  public SchemaTab(Ctx ctx, FxSchema schema, SchemaPane schemaPane) {
    this.schema = schema;
    this.ctx = ctx;
    textProperty().bind(schema.name);
    setGraphic(IconFactory.icon("icons/blocks.png", 20));
    setClosable(false);
    setContent(schemaPane);
  }

  @Autowired
  public void initWith(SchemaTabs tabPane) {
    tabPane.getTabs().add(this);
    ctx.addApplicationListener((ContextClosedEvent ev) -> tabPane.getTabs().remove(this));
  }
}
