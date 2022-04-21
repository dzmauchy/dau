package org.dau.ide.schema;

import javafx.scene.control.Tab;
import org.dau.di.Ctx;
import org.dau.ide.project.ProjectTabs;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.model.FxSchema;
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
    textProperty().bind(schema.getName());
    setGraphic(IconFactory.icon("icons/blocks.png", 20));
    setContent(schemaPane);
    setOnCloseRequest(ev -> ctx.close());
  }

  @Autowired
  public void initWith(ProjectTabs tabPane) {
    tabPane.getTabs().add(this);
    tabPane.getSelectionModel().selectLast();
    ctx.addApplicationListener((ContextClosedEvent ev) -> tabPane.getTabs().remove(this));
  }
}
