package org.dau.ui.schematic.fx.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.dau.runtime.Block;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Stream;

public final class FxBlock {

  public final FxSchema schema;
  public final int id;
  public final Executable executable;

  public final SimpleStringProperty name = new SimpleStringProperty(this, "name");

  public final SimpleDoubleProperty x = new SimpleDoubleProperty(this, "x");
  public final SimpleDoubleProperty y = new SimpleDoubleProperty(this, "y");
  public final SimpleDoubleProperty w = new SimpleDoubleProperty(this, "w");
  public final SimpleDoubleProperty h = new SimpleDoubleProperty(this, "h");

  private final LinkedHashMap<String, Input> inputs;
  private final LinkedHashMap<String, Output> outputs;

  public FxBlock(FxSchema schema, Executable executable) {
    this.schema = schema;
    this.id = schema.blockId();
    this.executable = executable;
    this.inputs = new LinkedHashMap<>(executable.getParameterCount());
    this.outputs = new LinkedHashMap<>(1, 0.9f);
    collectInputs();
    outputs.put("*", new Output(executable, "*"));
    collectOutputs(executable.getAnnotatedReturnType().getType());
    this.name.set(getMetaName() + " " + id);
  }

  private void collectInputs() {
    for (var p : executable.getParameters()) {
      inputs.put(p.getName(), new Input(p));
    }
  }

  private void collectOutputs(Type type) {
    if (type instanceof ParameterizedType t) {
      collectOutputs(t.getRawType());
    } else if (type instanceof Class<?> c) {
      for (var m : c.getMethods()) {
        if (m.getParameterCount() > 2) {
          continue;
        }
        if (m.isSynthetic() || m.isBridge() || m.isVarArgs()) {
          continue;
        }
        if (m.getDeclaringClass() == Object.class) {
          continue;
        }
        outputs.put(m.getName(), new Output(m, m.getName()));
      }
    }
  }

  public String getVar() {
    return "v_" + id;
  }

  public String getMetaName() {
    var block = executable.getAnnotation(Block.class);
    if (block != null) {
      return block.value();
    }
    block = executable.getDeclaringClass().getAnnotation(Block.class);
    if (block != null) {
      return block.value();
    }
    return executable.getName();
  }

  public Input getInput(String id) {
    return inputs.get(id);
  }

  public Output getOutput(String id) {
    return outputs.get(id);
  }

  public Stream<Input> getInputs() {
    return inputs.values().stream();
  }

  public Stream<Output> getOutputs() {
    return outputs.values().stream();
  }

  public void remove() {
    if (!schema.blocks.remove(this)) {
      throw new IllegalStateException("Not exists");
    }
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FxBlock b && id == b.id;
  }

  @Override
  public String toString() {
    return "Block(" + id + ")";
  }

  public final class Input {

    public final Parameter parameter;

    public Input(Parameter parameter) {
      this.parameter = parameter;
    }

    public String getId() {
      return parameter.getName();
    }

    public FxBlock getBlock() {
      return FxBlock.this;
    }

    @Override
    public int hashCode() {
      return Objects.hash(getId(), getBlock().id);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Input b && getId().equals(b.getId()) && b.getBlock().equals(getBlock());
    }

    @Override
    public String toString() {
      return FxBlock.this + "@in:" + getId();
    }
  }

  public final class Output {

    public final Executable out;
    public final String id;

    public Output(Executable out, String id) {
      this.out = out;
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public FxBlock getBlock() {
      return FxBlock.this;
    }

    @Override
    public int hashCode() {
      return Objects.hash(getId(), getBlock().id);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Output b && getId().equals(b.getId()) && b.getBlock().equals(getBlock());
    }

    @Override
    public String toString() {
      return FxBlock.this + "@out:" + getId();
    }
  }
}
