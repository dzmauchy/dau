package org.dau.ide.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.stereotype.Component;

@Component
public class MainSchemas {

  public final ObservableSet<FxSchema> schemas = FXCollections.observableSet();
}
