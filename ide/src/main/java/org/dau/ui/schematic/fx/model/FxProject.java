package org.dau.ui.schematic.fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.dau.util.Encoders;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.newDirectoryStream;

public final class FxProject {

  private static final AtomicInteger COUNTER = new AtomicInteger();

  public final String id;
  public final SimpleStringProperty name = new SimpleStringProperty(this, "name", "Project " + COUNTER.incrementAndGet());
  public final ObservableSet<FxSchema> schemas = FXCollections.observableSet();

  private FxProject(String id) {
    this.id = id == null ? Encoders.generateId(this) : id;
  }

  public FxProject() {
    this(null);
  }

  public static FxProject load(Path directory) {
    var project = new FxProject(directory.getFileName().toString());
    try (var ds = newDirectoryStream(directory, "schema-*.xml")) {
      for (var file : ds) {
        var schema = FxSchema.load(file, currentThread().getContextClassLoader());
        project.schemas.add(schema);
      }
    } catch (NotDirectoryException ignore) {
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return project;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FxProject p && p.id.equals(id);
  }

  @Override
  public String toString() {
    return name.get();
  }
}
