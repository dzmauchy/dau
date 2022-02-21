package org.dau.ide.main;

import javafx.collections.ObservableSet;
import org.dau.ui.schematic.fx.model.FxProject;
import org.springframework.stereotype.Component;

import static javafx.collections.FXCollections.observableSet;

@Component
public class MainProjects {
  public final ObservableSet<FxProject> projects = observableSet();
}
