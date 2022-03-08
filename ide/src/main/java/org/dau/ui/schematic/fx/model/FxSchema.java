package org.dau.ui.schematic.fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import org.dau.ui.schematic.fx.model.FxBlock.Input;
import org.dau.ui.schematic.fx.model.FxBlock.Output;
import org.dau.util.Encoders;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javafx.collections.FXCollections.observableSet;
import static org.dau.util.Xmls.elementsByTag;

public final class FxSchema {

  private final BitSet ids = new BitSet();
  public final String id;
  public final SimpleStringProperty name = new SimpleStringProperty(this, "name", "Schema");
  final ObservableSet<FxBlock> blocks = observableSet();
  final ObservableSet<FxBlockConnection> connections = observableSet();
  private final HashMap<Integer, FxBlock> blockMap = new HashMap<>();
  private final HashMap<Integer, HashMap<String, ArrayList<Input>>> outputs = new HashMap<>();
  private final HashMap<Integer, HashMap<String, ArrayList<Output>>> inputs = new HashMap<>();
  private final HashMap<Integer, ArrayList<FxBlockConnection>> outMap = new HashMap<>();
  private final HashMap<Integer, ArrayList<FxBlockConnection>> inMap = new HashMap<>();

  private FxSchema(String id) {
    this.id = id == null ? Encoders.generateId(this) : id;
    blocks.addListener((Change<? extends FxBlock> c) -> {
      if (c.wasRemoved()) {
        var block = c.getElementRemoved();
        blockMap.remove(block.id);
        ids.clear(block.id);
        connections.removeIf(co -> co.in().getBlock() == block || co.out().getBlock() == block);
      }
      if (c.wasAdded()) {
        var block = c.getElementAdded();
        ids.set(block.id);
        blockMap.put(block.id, block);
      }
    });
    connections.addListener((Change<? extends FxBlockConnection> c) -> {
      if (c.wasRemoved()) {
        var conn = c.getElementRemoved();
        outputs.computeIfPresent(conn.out().getBlock().id, (blockId, old) -> {
          old.computeIfPresent(conn.out().id, (oId, l) -> {
            l.removeIf(e -> e == conn.in());
            return l.isEmpty() ? null : l;
          });
          return old.isEmpty() ? null : old;
        });
        inputs.computeIfPresent(conn.in().getBlock().id, (blockId, old) -> {
          old.computeIfPresent(conn.in().id, (iId, l) -> {
            l.removeIf(e -> e == conn.out());
            return l.isEmpty() ? null : l;
          });
          return old.isEmpty() ? null : old;
        });
        outMap.computeIfPresent(conn.out().getBlock().id, (blockId, old) -> {
          if (old.removeIf(e -> conn == e)) {
            if (old.isEmpty()) {
              return null;
            }
          }
          return old;
        });
        inMap.computeIfPresent(conn.in().getBlock().id, (blockId, old) -> {
          if (old.removeIf(e -> conn == e)) {
            if (old.isEmpty()) {
              return null;
            }
          }
          return old;
        });
      }
      if (c.wasAdded()) {
        var conn = c.getElementAdded();
        outputs
          .computeIfAbsent(conn.out().getBlock().id, k -> new HashMap<>())
          .computeIfAbsent(conn.out().id, k -> new ArrayList<>())
          .add(conn.in());
        inputs
          .computeIfAbsent(conn.in().getBlock().id, k -> new HashMap<>())
          .computeIfAbsent(conn.in().id, k -> new ArrayList<>())
          .add(conn.out());
      }
    });
  }

  public FxSchema() {
    this(null);
  }

  int blockId() {
    return ids.nextClearBit(0);
  }

  public boolean removeBlock(int id) {
    return blocks.removeIf(e -> e.id == id);
  }

  public boolean addConnection(Output output, Input input) {
    return connections.add(new FxBlockConnection(output, input));
  }

  public FxBlock getBlock(int id) {
    return blockMap.get(id);
  }

  public Stream<FxBlock> getBlocks() {
    return blocks.stream();
  }

  public Stream<FxBlockConnection> getConnections() {
    return connections.stream();
  }

  public Stream<FxBlockConnection> getInputConnections(int id) {
    var l = inMap.get(id);
    return l == null ? Stream.empty() : l.stream();
  }

  public Stream<FxBlockConnection> getOutputConnections(int id) {
    var l = outMap.get(id);
    return l == null ? Stream.empty() : l.stream();
  }

  public Stream<Input> getOutputConnections(int blockId, String outputId) {
    return Stream.ofNullable(outputs.get(blockId)).flatMap(m -> Stream.ofNullable(m.get(outputId))).flatMap(ArrayList::stream);
  }

  public Stream<Output> getInputConnections(int blockId, String inputId) {
    return Stream.ofNullable(inputs.get(blockId)).flatMap(m -> Stream.ofNullable(m.get(inputId))).flatMap(ArrayList::stream);
  }

  public void addBlockListener(SetChangeListener<FxBlock> listener) {
    blocks.addListener(listener);
  }

  public void removeBlockListener(SetChangeListener<FxBlock> listener) {
    blocks.removeListener(listener);
  }

  public void addConnectionListener(SetChangeListener<FxBlockConnection> listener) {
    connections.addListener(listener);
  }

  public void removeConnectionListener(SetChangeListener<FxBlockConnection> listener) {
    connections.removeListener(listener);
  }

  public void addBlock(FxBlock block) {
    blocks.add(block);
  }

  public Element toXml(Document doc) {
    var el = doc.createElement("schema");
    el.setAttribute("id", id);
    el.setAttribute("name", name.get());
    var blocksEl = doc.createElement("blocks");
    el.appendChild(blocksEl);
    for (var block : blocks) {
      blocksEl.appendChild(block.toXml(doc));
    }
    var connectionsEl = doc.createElement("connections");
    el.appendChild(connectionsEl);
    for (var connection : connections) {
      connectionsEl.appendChild(connection.toXml(doc));
    }
    return el;
  }

  public static FxSchema fromXml(Element element, ClassLoader classLoader) {
    var id = element.getAttribute("id");
    var name = element.getAttribute("name");
    var schema = new FxSchema(id);
    elementsByTag(element, "blocks", "block").forEach(el -> {
      var block = FxBlock.fromXml(schema, classLoader, el);
      schema.blocks.add(block);
    });
    elementsByTag(element, "connections", "connection").forEach(el -> {
      var outBlockId = Integer.parseInt(el.getAttribute("out-block-id"));
      var out = el.getAttribute("out");
      var inBlockId = Integer.parseInt(el.getAttribute("in-block-id"));
      var in = el.getAttribute("in");
      var outBlock = schema.getBlock(outBlockId);
      var inBlock = schema.getBlock(inBlockId);
      if (outBlock == null || inBlock == null) {
        return;
      }
      var output = outBlock.getOutput(out);
      var input = inBlock.getInput(in);
      if (output == null || input == null) {
        return;
      }
      schema.connections.add(new FxBlockConnection(output, input));
    });
    return schema;
  }

  public void save(OutputStream outputStream) {
    try {
      var dbf = DocumentBuilderFactory.newDefaultInstance();
      var db = dbf.newDocumentBuilder();
      var doc = db.newDocument();
      var schema = toXml(doc);
      doc.appendChild(schema);

      var tf = TransformerFactory.newDefaultInstance();
      tf.setAttribute("indent-number", 2);
      var t = tf.newTransformer();
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      t.setOutputProperty(OutputKeys.STANDALONE, "true");

      t.transform(new DOMSource(doc), new StreamResult(outputStream));
    } catch (ParserConfigurationException | TransformerException e) {
      throw new IllegalStateException(e);
    }
  }

  public void save(Path path) {
    try (var outputStream = Files.newOutputStream(path)) {
      save(outputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static FxSchema load(InputStream inputStream, ClassLoader classLoader) {
    try {
      var dbf = DocumentBuilderFactory.newDefaultInstance();
      var db = dbf.newDocumentBuilder();
      var doc=  db.parse(inputStream);
      return fromXml(doc.getDocumentElement(), classLoader);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static FxSchema load(Reader reader, ClassLoader classLoader) {
    try {
      var dbf = DocumentBuilderFactory.newDefaultInstance();
      var db = dbf.newDocumentBuilder();
      var doc=  db.parse(new InputSource(reader));
      return fromXml(doc.getDocumentElement(), classLoader);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static FxSchema load(Path path, ClassLoader classLoader) {
    try (var reader = Files.newBufferedReader(path, UTF_8)) {
      return load(reader, classLoader);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof FxSchema s) {
      return s == this || s.id.equals(id);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "Schema(" + id + ")";
  }
}
