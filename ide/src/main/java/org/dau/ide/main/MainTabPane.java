package org.dau.ide.main;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.SetChangeListener.Change;
import javafx.scene.control.TabPane;
import org.dau.di.Ctx;
import org.dau.ide.schema.SchemaConf;
import org.dau.ide.schema.SchemaTab;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class MainTabPane extends TabPane {

  @Autowired
  public void initSchemas(Ctx ctx, MainSchemas schemas) {
    schemas.schemas.addListener((Change<? extends FxSchema> c) -> {
      if (c.wasAdded()) {
        var schema = c.getElementAdded();
        var newCtx = new Ctx(ctx, schema.name.get());
        var ih = (ChangeListener<String>) (o, ov, nv) -> newCtx.setDisplayName(nv);
        newCtx.addRoot(ih);
        schema.name.addListener(new WeakChangeListener<>(ih));
        newCtx.registerBean(SchemaConf.class, () -> new SchemaConf(schema));
        newCtx.refresh();
        newCtx.start();
      }
      if (c.wasRemoved()) {
        var schema = c.getElementRemoved();
        getTabs().removeIf(tab -> tab instanceof SchemaTab t && t.schema == schema);
      }
    });
  }
}
