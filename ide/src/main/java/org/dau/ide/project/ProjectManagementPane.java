package org.dau.ide.project;

import javafx.scene.control.Accordion;
import org.dau.ide.project.management.SchemasManagementPane;
import org.springframework.stereotype.Component;

@Component
public class ProjectManagementPane extends Accordion {

  public ProjectManagementPane(SchemasManagementPane schemasManagementPane) {
    super(schemasManagementPane);
  }
}
