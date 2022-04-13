package org.dau.ide.schema;

import org.dau.ide.action.FxAction;
import org.dau.ide.action.ToolbarAction;
import org.dau.ide.project.ProjectConf;
import org.springframework.stereotype.Component;

@Component
public final class SchemaActions {

  @SchemaBean
  @ToolbarAction
  public FxAction saveAction(SchemaConf conf, ProjectConf projectConf) {
    return new FxAction("icons/save.png", "Save")
      .on(() -> {
        var file = projectConf.directory.resolve(conf.schema.toFileName());
        conf.schema.save(file);
      });
  }
}
