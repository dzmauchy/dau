package org.dau.ide.project;

import org.dau.ide.action.FxAction;
import org.dau.ide.action.ToolbarAction;
import org.springframework.stereotype.Component;

@Component
public class ProjectActions {

  @ToolbarAction
  @ProjectBean
  public FxAction createNewSchemaAction() {
    return new FxAction("icons/create.png", "Create")
      .on(() -> {
      });
  }
}
