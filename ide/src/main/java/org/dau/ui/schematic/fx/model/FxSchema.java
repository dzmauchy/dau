package org.dau.ui.schematic.fx.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import org.dau.ui.schematic.fx.model.FxBlock.Input;
import org.dau.ui.schematic.fx.model.FxBlock.Output;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.stream.Stream;

import static javafx.collections.FXCollections.observableSet;

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

  public FxSchema() {
    id = new UID().toString();
    blocks.addListener((Change<? extends FxBlock> c) -> {
      if (c.wasRemoved()) {
        var block = c.getElementRemoved();
        blockMap.remove(block.id);
        connections.removeIf(co -> co.in().getBlock() == block || co.out().getBlock() == block);
      }
      if (c.wasAdded()) {
        blockMap.put(c.getElementAdded().id, c.getElementAdded());
      }
    });
    connections.addListener((Change<? extends FxBlockConnection> c) -> {
      if (c.wasRemoved()) {
        var conn = c.getElementRemoved();
        outputs.computeIfPresent(conn.out().getBlock().id, (blockId, old) -> {
          old.computeIfPresent(conn.out().getId(), (oId, l) -> {
            l.removeIf(e -> e == conn.in());
            return l.isEmpty() ? null : l;
          });
          return old.isEmpty() ? null : old;
        });
        inputs.computeIfPresent(conn.in().getBlock().id, (blockId, old) -> {
          old.computeIfPresent(conn.in().getId(), (iId, l) -> {
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
        outputs.computeIfAbsent(conn.out().getBlock().id, k -> new HashMap<>()).computeIfAbsent(conn.out().getId(), k -> new ArrayList<>()).add(conn.in());
        inputs.computeIfAbsent(conn.in().getBlock().id, k -> new HashMap<>()).computeIfAbsent(conn.in().getId(), k -> new ArrayList<>()).add(conn.out());
      }
    });
  }

  int blockId() {
    int n = ids.nextClearBit(0);
    ids.set(n);
    return n;
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
