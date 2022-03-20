package org.dau.ide.project;

import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ProjectManagementPane extends Accordion {

  public ProjectManagementPane(@Qualifier("management") TitledPane[] panes) {
    super(panes);
  }
}
