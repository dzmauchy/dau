package org.dau.ui.schematic.fx.model;

import javafx.beans.property.SimpleDoubleProperty;
import org.dau.ui.schematic.layout.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class FxBlock implements Block {

  private final FxSchema schema;
  private final String id;
  private final SimpleDoubleProperty x = new SimpleDoubleProperty(this, "x", 0);
  private final SimpleDoubleProperty y = new SimpleDoubleProperty(this, "y", 0);
  private final SimpleDoubleProperty w = new SimpleDoubleProperty(this, "w", 0);
  private final SimpleDoubleProperty h = new SimpleDoubleProperty(this, "h", 0);
  private final LinkedHashMap<String, Input> inputs;
  private final LinkedHashMap<String, Output> outputs;

  public FxBlock(FxSchema schema, String id, List<InputInfo> inputs, List<OutputInfo> outputs) {
    this.schema = schema;
    this.id = id;
    this.inputs = new LinkedHashMap<>(inputs.size());
    this.outputs = new LinkedHashMap<>(outputs.size());
    for (var i : inputs) {
      this.inputs.put(i.id(), new Input(i.id()));
    }
    for (var o : outputs) {
      this.outputs.put(o.id(), new Output(o.id()));
    }
    this.schema.blocks.add(this);
  }

  @Override
  public FxSchema getSchema() {
    return schema;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public double getX() {
    return x.get();
  }

  @Override
  public double getY() {
    return y.get();
  }

  @Override
  public double getW() {
    return w.get();
  }

  @Override
  public double getH() {
    return h.get();
  }

  @Override
  public Input getInput(String id) {
    return inputs.get(id);
  }

  @Override
  public Output getOutput(String id) {
    return outputs.get(id);
  }

  @Override
  public Stream<Input> getInputs() {
    return inputs.values().stream();
  }

  @Override
  public Stream<Output> getOutputs() {
    return outputs.values().stream();
  }

  public SimpleDoubleProperty xProperty() {
    return x;
  }

  public SimpleDoubleProperty yProperty() {
    return y;
  }

  public SimpleDoubleProperty wProperty() {
    return w;
  }

  public SimpleDoubleProperty hProperty() {
    return h;
  }

  public void remove() {
    if (!schema.blocks.remove(this)) {
      throw new IllegalStateException("Not exists");
    }
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FxBlock b && id.equals(b.id);
  }

  @Override
  public String toString() {
    return "Block(" + id + ")";
  }

  public final class Input implements BlockInput, BlockElement {

    private final String id;
    private final SimpleDoubleProperty connectionPoint = new SimpleDoubleProperty(this, "connectionPoint", 0);

    public Input(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public double getConnectionPoint() {
      return connectionPoint.get();
    }

    @Override
    public FxBlock getBlock() {
      return FxBlock.this;
    }

    public SimpleDoubleProperty connectionPointProperty() {
      return connectionPoint;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, getBlock().id);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Input b && id.equals(b.id) && b.getBlock().equals(getBlock());
    }

    @Override
    public String toString() {
      return FxBlock.this + "@in:" + id;
    }
  }

  public final class Output implements BlockOutput, BlockElement {

    private final String id;
    private final SimpleDoubleProperty connectionPoint = new SimpleDoubleProperty(this, "connectionPoint", 0);

    public Output(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public double getConnectionPoint() {
      return connectionPoint.get();
    }

    @Override
    public FxBlock getBlock() {
      return FxBlock.this;
    }

    public SimpleDoubleProperty connectionPointProperty() {
      return connectionPoint;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, getBlock().id);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Output b && id.equals(b.id) && b.getBlock().equals(getBlock());
    }

    @Override
    public String toString() {
      return FxBlock.this + "@out:" + id;
    }
  }
}
