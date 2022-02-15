package org.dau.ui.schematic.fx.model;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import org.dau.ui.schematic.IdEntity;
import org.dau.ui.schematic.fx.model.FxBlock.Input;
import org.dau.ui.schematic.fx.model.FxBlock.Output;
import org.dau.ui.schematic.layout.model.Layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static javafx.collections.FXCollections.observableSet;

public final class FxSchema implements Layout, IdEntity {

  private final String id;
  final ObservableSet<FxBlock> blocks = observableSet();
  final ObservableSet<FxBlockConnection> connections = observableSet();
  private final HashMap<String, FxBlock> blockMap = new HashMap<>();
  private final HashMap<String, HashMap<String, ArrayList<Input>>> outputs = new HashMap<>();
  private final HashMap<String, HashMap<String, ArrayList<Output>>> inputs = new HashMap<>();
  private final HashMap<String, ArrayList<FxBlockConnection>> outMap = new HashMap<>();
  private final HashMap<String, ArrayList<FxBlockConnection>> inMap = new HashMap<>();

  public FxSchema(String id) {
    this.id = id;
    blocks.addListener((Change<? extends FxBlock> c) -> {
      if (c.wasRemoved()) {
        blockMap.remove(c.getElementRemoved().getId());
      }
      if (c.wasAdded()) {
        blockMap.put(c.getElementAdded().getId(), c.getElementAdded());
      }
    });
    connections.addListener((Change<? extends FxBlockConnection> c) -> {
      if (c.wasRemoved()) {
        var conn = c.getElementRemoved();
        outputs.computeIfPresent(conn.out().getBlock().getId(), (blockId, old) -> {
          old.computeIfPresent(conn.out().getId(), (oId, l) -> {
            l.removeIf(e -> e == conn.in());
            return l.isEmpty() ? null : l;
          });
          return old.isEmpty() ? null : old;
        });
        inputs.computeIfPresent(conn.in().getBlock().getId(), (blockId, old) -> {
          old.computeIfPresent(conn.in().getId(), (iId, l) -> {
            l.removeIf(e -> e == conn.out());
            return l.isEmpty() ? null : l;
          });
          return old.isEmpty() ? null : old;
        });
        outMap.computeIfPresent(conn.out().getBlock().getId(), (blockId, old) -> {
          if (old.removeIf(e -> conn == e)) {
            if (old.isEmpty()) {
              return null;
            }
          }
          return old;
        });
        inMap.computeIfPresent(conn.in().getBlock().getId(), (blockId, old) -> {
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
        outputs.computeIfAbsent(conn.out().getBlock().getId(), k -> new HashMap<>()).computeIfAbsent(conn.out().getId(), k -> new ArrayList<>()).add(conn.in());
        inputs.computeIfAbsent(conn.in().getBlock().getId(), k -> new HashMap<>()).computeIfAbsent(conn.in().getId(), k -> new ArrayList<>()).add(conn.out());
      }
    });
  }

  public boolean removeBlock(String id) {
    return blocks.removeIf(e -> e.getId().equals(id));
  }

  public boolean addConnection(Output output, Input input) {
    return connections.add(new FxBlockConnection(output, input));
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public FxBlock getBlock(String id) {
    return blockMap.get(id);
  }

  @Override
  public Stream<FxBlock> getBlocks() {
    return blocks.stream();
  }

  @Override
  public Stream<FxBlockConnection> getConnections() {
    return connections.stream();
  }

  @Override
  public Stream<FxBlockConnection> getInputConnection(String id) {
    var l = inMap.get(id);
    return l == null ? Stream.empty() : l.stream();
  }

  @Override
  public Stream<FxBlockConnection> getOutputConnections(String id) {
    var l = outMap.get(id);
    return l == null ? Stream.empty() : l.stream();
  }

  @Override
  public Stream<Input> getOutputConnections(String blockId, String outputId) {
    return Stream.ofNullable(outputs.get(blockId)).flatMap(m -> Stream.ofNullable(m.get(outputId))).flatMap(ArrayList::stream);
  }

  @Override
  public Stream<Output> getInputConnections(String blockId, String inputId) {
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

  @Override
  public String toString() {
    return "Schema(" + id + ")";
  }
}
