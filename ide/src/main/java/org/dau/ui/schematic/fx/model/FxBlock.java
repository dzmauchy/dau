package org.dau.ui.schematic.fx.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.dau.runtime.Block;
import org.dau.util.Xmls;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.invoke.MethodType.fromMethodDescriptorString;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;

public final class FxBlock {

  public final FxSchema schema;
  public final int id;

  public final SimpleStringProperty name = new SimpleStringProperty(this, "name");
  public final SimpleObjectProperty<Executable> executable = new SimpleObjectProperty<>(this, "executable");

  public final SimpleDoubleProperty x = new SimpleDoubleProperty(this, "x");
  public final SimpleDoubleProperty y = new SimpleDoubleProperty(this, "y");
  public final SimpleDoubleProperty w = new SimpleDoubleProperty(this, "w");
  public final SimpleDoubleProperty h = new SimpleDoubleProperty(this, "h");

  public final ObservableList<Input> inputs = FXCollections.observableArrayList();
  public final ObservableList<Output> outputs = FXCollections.observableArrayList();

  public FxBlock(FxSchema schema, Executable executable) {
    this(schema, schema.blockId(), executable);
  }

  private FxBlock(FxSchema schema, int id, Executable executable) {
    this.schema = schema;
    this.id = id;
    collectInputs();
    collectOutputs();
    this.executable.set(executable);
    this.name.set(getMetaName() + " " + id);
  }

  private void collectInputs() {
    executable.addListener((o, ov, e) -> {
      var params = e.getParameters();
      var inputs = stream(params).map(p -> new Input(p.getName())).toList();
      this.inputs.removeIf(i -> inputs.stream().noneMatch(i::equals));
      inputs.forEach(input -> {
        if (this.inputs.stream().noneMatch(input::equals)) {
          this.inputs.add(input);
        }
      });
      this.inputs.sort(comparingInt(inputs::indexOf));
    });
  }

  private void collectOutputs(Type type, ArrayList<Output> outputs) {
    if (type != void.class) {
      outputs.add(new Output("@"));
    }
    if (type instanceof ParameterizedType t) {
      collectOutputs(t.getRawType(), outputs);
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
        outputs.add(new Output(m.getName()));
      }
    }
  }

  private void collectOutputs() {
    executable.addListener((o, ov, e) -> {
      var outputs = new ArrayList<Output>();
      collectOutputs(e.getAnnotatedReturnType().getType(), outputs);
      this.outputs.removeIf(out -> outputs.stream().noneMatch(out::equals));
      outputs.forEach(output -> {
        if (this.outputs.stream().noneMatch(output::equals)) {
          this.outputs.add(output);
        }
      });
      this.outputs.sort(comparingInt(outputs::indexOf));
    });
  }

  public String getVar() {
    return "v_" + id;
  }

  public String getMetaName() {
    var executable = this.executable.get();
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
    return inputs.parallelStream()
      .filter(i -> i.id.equals(id))
      .findAny()
      .orElse(null);
  }

  public Output getOutput(String id) {
    return outputs.parallelStream()
      .filter(o -> o.id.equals(id))
      .findAny()
      .orElse(null);
  }

  public Stream<Input> getInputs() {
    return inputs.stream();
  }

  public Stream<Output> getOutputs() {
    return outputs.stream();
  }

  public void remove() {
    if (!schema.removeBlock(this)) {
      throw new NoSuchElementException(toString());
    }
  }

  public Element toXml(Document doc) {
    var el = doc.createElement("block");
    el.setAttribute("id", Integer.toString(id));
    el.setAttribute("name", name.get());
    el.setAttribute("x", Double.toString(x.get()));
    el.setAttribute("y", Double.toString(y.get()));
    el.setAttribute("w", Double.toString(w.get()));
    el.setAttribute("h", Double.toString(h.get()));
    try {
      var lookup = MethodHandles.publicLookup();
      if (executable.get() instanceof Constructor<?> c) {
        var mh = lookup.unreflectConstructor(c);
        el.setAttribute("type", mh.type().descriptorString());
        el.setAttribute("executable", "*");
      } else if (executable.get() instanceof Method m) {
        var mh = lookup.unreflect(m);
        el.setAttribute("class", m.getDeclaringClass().getName());
        el.setAttribute("type", mh.type().descriptorString());
        el.setAttribute("executable", m.getName());
      }
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
    var inputsEl = doc.createElement("inputs");
    inputs.forEach(input -> {
      var c = input.constant.get();
      if (c != null) {
        var e = doc.createElement("input");
        e.setAttribute("id", input.id);
        e.setTextContent(c);
        inputsEl.appendChild(e);
      }
    });
    if (inputsEl.hasChildNodes()) {
      el.appendChild(inputsEl);
    }
    return el;
  }

  public static FxBlock fromXml(FxSchema schema, ClassLoader classLoader, Element element) {
    int id = Integer.parseInt(element.getAttribute("id"));
    var executableName = element.getAttribute("executable");
    final Executable executable;
    try {
      var mt = fromMethodDescriptorString(element.getAttribute("type"), classLoader);
      if (executableName.equals("*")) {
        executable = mt.returnType().getConstructor(mt.parameterArray());
      } else {
        var className = element.getAttribute("class");
        var cl = Class.forName(className, false, classLoader);
        executable = cl.getMethod(executableName, mt.parameterArray());
      }
    } catch (NoSuchMethodException | ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
    var block = new FxBlock(schema, id, executable);
    block.name.set(element.getAttribute("name"));
    block.x.set(Double.parseDouble(element.getAttribute("x")));
    block.y.set(Double.parseDouble(element.getAttribute("y")));
    block.w.set(Double.parseDouble(element.getAttribute("w")));
    block.h.set(Double.parseDouble(element.getAttribute("h")));
    Xmls.elementsByTag(element, "inputs", "input").forEach(e -> {
      var inputId = e.getAttribute("id");
      var input = block.getInput(inputId);
      if (input != null) {
        input.constant.set(e.getTextContent());
      }
    });
    return block;
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

    public final String id;
    public final SimpleStringProperty constant = new SimpleStringProperty(this, "constant");

    public Input(String id) {
      this.id = id;
    }

    public FxBlock getBlock() {
      return FxBlock.this;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, getBlock().id);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Input b && b.id.equals(id) && b.getBlock().equals(getBlock());
    }

    @Override
    public String toString() {
      return FxBlock.this + "@in:" + id;
    }
  }

  public final class Output {

    public final String id;

    public Output(String id) {
      this.id = id;
    }

    public FxBlock getBlock() {
      return FxBlock.this;
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
