package org.dau.ide.project;

import javafx.beans.value.ChangeListener;
import org.dau.di.Ctx;
import org.dau.ide.schema.SchemaConf;
import org.dau.ide.schema.SchemaTab;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public final class ProjectSchemas {

  private final Ctx ctx;
  private final ObjectFactory<ProjectTabs> tabs;

  public ProjectSchemas(Ctx ctx, ObjectFactory<ProjectTabs> tabs) {
    this.ctx = ctx;
    this.tabs = tabs;
  }

  public void addSchema(FxSchema schema) {
    if (tabs.getObject().getTabs().stream().anyMatch(t -> t instanceof SchemaTab v && v.schema.id.equals(schema.id))) {
      return;
    }
    var newCtx = new Ctx(ctx, schema.name.get());
    var ih = (ChangeListener<String>) (o, ov, nv) -> newCtx.setDisplayName(nv);
    schema.name.addListener(ih);
    newCtx.addApplicationListener((ContextClosedEvent ev) -> schema.name.removeListener(ih));
    newCtx.registerBean(SchemaConf.class, () -> new SchemaConf(schema));
    newCtx.refresh();
    newCtx.start();
  }
}
