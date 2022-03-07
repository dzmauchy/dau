package org.dau.ui.schematic.fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.dau.util.Encoders;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.newDirectoryStream;

public final class FxProject {

  private static final AtomicInteger COUNTER = new AtomicInteger();

  public final String id;
  public final SimpleStringProperty name = new SimpleStringProperty(this, "name");
  public final ObservableSet<FxSchema> schemas = FXCollections.observableSet();

  private FxProject(String id) {
    this.id = id;
    this.name.set(id);
  }

  public FxProject() {
    this.id = Encoders.generateId(this);
    this.name.set("Project " + COUNTER.incrementAndGet());
  }

  public static FxProject load(Path directory) {
    var project = new FxProject(directory.getFileName().toString());
    try (var ds = newDirectoryStream(directory, "schema-*.xml")) {
      project.load0(directory.resolve("project.xml"));
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

  public Element toXml(Document doc) {
    var el = doc.createElement("project");
    el.setAttribute("name", name.get());
    return el;
  }

  private void save(Result result) {
    try {
      var dbf = DocumentBuilderFactory.newDefaultInstance();
      var db = dbf.newDocumentBuilder();
      var doc = db.newDocument();
      var root = doc.createElement("project");
      doc.appendChild(root);

      root.setAttribute("name", name.get());

      var tf = TransformerFactory.newDefaultInstance();
      tf.setAttribute("indent-number", 2);
      var t = tf.newTransformer();
      t.setOutputProperty(OutputKeys.STANDALONE, "yes");
      t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.transform(new DOMSource(doc), result);
    } catch (ParserConfigurationException | TransformerException e) {
      throw new IllegalStateException(e);
    }
  }

  public void save(Path path) {
    try (var out = Files.newOutputStream(path.resolve("project.xml"))) {
      save(new StreamResult(out));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    for (var schema : schemas) {
      schema.save(path.resolve("schema-" + schema.id + ".xml"));
    }
  }

  private void load0(Path path) {
    try {
      var dbf = DocumentBuilderFactory.newDefaultInstance();
      var db = dbf.newDocumentBuilder();
      var doc = db.parse(path.toFile());
      var root = doc.getDocumentElement();

      name.set(root.getAttribute("name"));
    } catch (IOException | ParserConfigurationException | SAXException e) {
      throw new IllegalStateException(e);
    }
  }

  public static boolean isProject(Path path) {
    return Files.isDirectory(path) && Files.isRegularFile(path.resolve("project.xml"));
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
