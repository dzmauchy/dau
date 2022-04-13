package org.dau.ide.main;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import org.dau.ui.schematic.fx.model.FxProject;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.util.HashMap;

import static javafx.collections.FXCollections.observableSet;

@Component
public class MainProjects {

  private final MainDirectories directories;
  public final ObservableSet<FxProject> projects = observableSet();
  private final HashMap<FxProject, SetChangeListener<FxSchema>> schemaListeners = new HashMap<>();

  public MainProjects(MainDirectories directories) {
    this.directories = directories;
    this.projects.addListener(this::onChangeProjects);
  }

  private void onChangeProjects(Change<? extends FxProject> change) {
    if (change.wasAdded()) {
      var project = change.getElementAdded();
      final SetChangeListener<FxSchema> l = e -> onChangeSchema(project, e);
      project.schemas.addListener(l);
      schemaListeners.put(project, l);
    }
    if (change.wasRemoved()) {
      var project = change.getElementRemoved();
      var listener = schemaListeners.remove(project);
      project.schemas.removeListener(listener);
    }
  }

  private void onChangeSchema(FxProject project, Change<? extends FxSchema> change) {
    final var dir = directories.homeDir.resolve(project.id);
    if (change.wasRemoved()) {
      var schema = change.getElementRemoved();
      var file = dir.resolve(schema.toFileName());
      if (Files.isRegularFile(file)) {
        if (!file.toFile().delete()) {
          throw new IllegalStateException("Could not delete " + file);
        }
      }
    }
  }
}
