package org.dau.ide.project;

import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TabPane;
import org.dau.di.Ctx;
import org.dau.ide.schema.SchemaConf;
import org.dau.ide.schema.SchemaTab;
import org.dau.ui.schematic.fx.model.FxProject;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import static javafx.application.Platform.runLater;

@Component
public class ProjectTabs extends TabPane {

  public ProjectTabs(ProjectManagementTab settingsTab) {
    super(settingsTab);
  }

  @Autowired
  public void initSchemas(Ctx ctx, FxProject project) {
    project.schemas.addListener((SetChangeListener.Change<? extends FxSchema> c) -> {
      if (c.wasAdded()) {
        onAdd(c.getElementAdded(), ctx);
      }
      if (c.wasRemoved()) {
        var schema = c.getElementRemoved();
        getTabs().removeIf(tab -> tab instanceof SchemaTab t && t.schema == schema);
      }
    });
    if (project.schemas.isEmpty()) {
      runLater(() -> project.schemas.add(new FxSchema()));
    } else {
      runLater(() -> {
        for (var schema : project.schemas) {
          onAdd(schema, ctx);
        }
      });
    }
  }

  private void onAdd(FxSchema schema, Ctx ctx) {
    var newCtx = new Ctx(ctx, schema.name.get());
    var ih = (ChangeListener<String>) (o, ov, nv) -> newCtx.setDisplayName(nv);
    schema.name.addListener(ih);
    newCtx.addApplicationListener((ContextClosedEvent ev) -> schema.name.removeListener(ih));
    newCtx.registerBean(SchemaConf.class, () -> new SchemaConf(schema));
    newCtx.refresh();
    newCtx.start();
  }
}
