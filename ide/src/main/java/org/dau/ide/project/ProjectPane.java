package org.dau.ide.project;

import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Component;

@Component
public class ProjectPane extends StackPane {

  public ProjectPane(SchemaTabs tabs) {
    super(tabs);
  }
}
