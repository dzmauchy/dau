package org.dau.ide.project;

import javafx.collections.SetChangeListener;
import javafx.scene.control.TabPane;
import org.dau.ide.schema.SchemaTab;
import org.dau.ui.schematic.model.FxProject;
import org.dau.ui.schematic.model.FxSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectTabs extends TabPane {

  public ProjectTabs(ProjectManagementTab settingsTab) {
    super(settingsTab);
  }

  @Autowired
  public void initSchemas(FxProject project) {
    project.getSchemas().addListener((SetChangeListener.Change<? extends FxSchema> c) -> {
      if (c.wasRemoved()) {
        var schema = c.getElementRemoved();
        getTabs().removeIf(tab -> tab instanceof SchemaTab t && t.getSchema() == schema);
      }
    });
  }
}
