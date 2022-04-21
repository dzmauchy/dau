package org.dau.ui.schematic.model

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import org.dau.runtime.Block
import org.dau.ui.utils.Xmls
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.fromMethodDescriptorString
import java.lang.reflect.*
import java.util.*
import java.util.Arrays.stream
import java.util.Comparator.comparingInt
import java.util.stream.Stream

class FxBlock private constructor(val schema: FxSchema, val id: Int, executable: Executable) {

  val name = SimpleStringProperty(this, "name")
  val executable = SimpleObjectProperty<Executable>(this, "executable")

  val x = SimpleDoubleProperty(this, "x")
  val y = SimpleDoubleProperty(this, "y")
  val w = SimpleDoubleProperty(this, "w")
  val h = SimpleDoubleProperty(this, "h")

  val inputs = FXCollections.observableArrayList<Input>()
  val outputs = FXCollections.observableArrayList<Output>()

  val varName: String
    get() = "v_$id"

  val metaName: String
    get() {
      val executable = this.executable.get()
      var block: Block? = executable.getAnnotation(Block::class.java)
      if (block != null) {
        return block.value
      }
      block = executable.declaringClass.getAnnotation(Block::class.java)
      return block?.value ?: executable.name
    }

  constructor(schema: FxSchema, executable: Executable) : this(schema, schema.blockId(), executable) {}

  init {
    collectInputs()
    collectOutputs()
    this.executable.set(executable)
    this.name.set("$metaName $id")
  }

  private fun collectInputs() {
    executable.addListener { _, _, e ->
      val params = e.parameters
      val inputs = stream(params).map { p -> Input(p.name) }.toList()
      this.inputs.removeIf { i -> inputs.stream().noneMatch { i == it } }
      inputs.forEach { input ->
        if (this.inputs.stream().noneMatch { input == it }) {
          this.inputs.add(input)
        }
      }
      this.inputs.sortWith(comparingInt { inputs.indexOf(it) })
    }
  }

  private fun collectOutputs(type: Type, outputs: ArrayList<Output>) {
    if (type !== Void.TYPE) {
      outputs.add(Output("@"))
    }
    if (type is ParameterizedType) {
      collectOutputs(type.rawType, outputs)
    } else if (type is Class<*>) {
      for (m in type.methods) {
        if (m.parameterCount > 2) {
          continue
        }
        if (m.isSynthetic || m.isBridge || m.isVarArgs) {
          continue
        }
        if (m.declaringClass == Any::class.java) {
          continue
        }
        outputs.add(Output(m.name))
      }
    }
  }

  private fun collectOutputs() {
    executable.addListener { _, _, e ->
      val outputs = ArrayList<Output>()
      collectOutputs(e.annotatedReturnType.type, outputs)
      this.outputs.removeIf { out -> outputs.stream().noneMatch { out == it } }
      outputs.forEach { output ->
        if (this.outputs.stream().noneMatch { output == it }) {
          this.outputs.add(output)
        }
      }
      this.outputs.sortWith(comparingInt { outputs.indexOf(it) })
    }
  }

  fun getInput(id: String): Input? {
    return inputs.parallelStream()
      .filter { i -> i.id == id }
      .findAny()
      .orElse(null)
  }

  fun getOutput(id: String): Output? {
    return outputs.parallelStream()
      .filter { o -> o.id == id }
      .findAny()
      .orElse(null)
  }

  fun getInputs(): Stream<Input> {
    return inputs.stream()
  }

  fun getOutputs(): Stream<Output> {
    return outputs.stream()
  }

  fun remove() {
    if (!schema.removeBlock(this)) {
      throw NoSuchElementException(toString())
    }
  }

  fun toXml(doc: Document): Element {
    val el = doc.createElement("block")
    el.setAttribute("id", id.toString())
    el.setAttribute("name", name.get())
    el.setAttribute("x", x.get().toString())
    el.setAttribute("y", y.get().toString())
    el.setAttribute("w", w.get().toString())
    el.setAttribute("h", h.get().toString())
    try {
      val lookup = MethodHandles.publicLookup()
      when (val e = executable.get()) {
        is Constructor<*> -> {
          val mh = lookup.unreflectConstructor(e)
          el.setAttribute("type", mh.type().descriptorString())
          el.setAttribute("executable", "*")
        }
        is Method -> {
          val mh = lookup.unreflect(e)
          el.setAttribute("class", e.getDeclaringClass().name)
          el.setAttribute("type", mh.type().descriptorString())
          el.setAttribute("executable", e.name)
        }
      }
    } catch (e: IllegalAccessException) {
      throw IllegalStateException(e)
    }

    val inputsEl = doc.createElement("inputs")
    inputs.forEach { input ->
      val c = input.constant.get()
      if (c != null) {
        val e = doc.createElement("input")
        e.setAttribute("id", input.id)
        e.textContent = c
        inputsEl.appendChild(e)
      }
    }
    if (inputsEl.hasChildNodes()) {
      el.appendChild(inputsEl)
    }
    return el
  }

  override fun hashCode(): Int {
    return id
  }

  override fun equals(other: Any?): Boolean {
    return other is FxBlock && id == other.id
  }

  override fun toString(): String {
    return "Block($id)"
  }

  inner class Input(val id: String) {
    val constant = SimpleStringProperty(this, "constant")

    val block: FxBlock
      get() = this@FxBlock

    override fun hashCode(): Int {
      return Objects.hash(id, block.id)
    }

    override fun equals(other: Any?): Boolean {
      return other is Input && other.id == id && other.block == block
    }

    override fun toString(): String {
      return this@FxBlock.toString() + "@in:" + id
    }
  }

  inner class Output(val id: String) {

    val block: FxBlock
      get() = this@FxBlock

    override fun hashCode(): Int {
      return Objects.hash(id, block.id)
    }

    override fun equals(other: Any?): Boolean {
      return other is Output && id == other.id && other.block == block
    }

    override fun toString(): String {
      return this@FxBlock.toString() + "@out:" + id
    }
  }

  companion object {

    fun fromXml(schema: FxSchema, classLoader: ClassLoader, element: Element): FxBlock {
      val id = Integer.parseInt(element.getAttribute("id"))
      val executableName = element.getAttribute("executable")
      val executable = try {
        val mt = fromMethodDescriptorString(element.getAttribute("type"), classLoader)
        if (executableName == "*") {
          mt.returnType().getConstructor(*mt.parameterArray())
        } else {
          val className = element.getAttribute("class")
          val cl = Class.forName(className, false, classLoader)
          cl.getMethod(executableName, *mt.parameterArray())
        }
      } catch (e: NoSuchMethodException) {
        throw IllegalStateException(e)
      } catch (e: ClassNotFoundException) {
        throw IllegalStateException(e)
      }

      val block = FxBlock(schema, id, executable)
      block.name.set(element.getAttribute("name"))
      block.x.set(java.lang.Double.parseDouble(element.getAttribute("x")))
      block.y.set(java.lang.Double.parseDouble(element.getAttribute("y")))
      block.w.set(java.lang.Double.parseDouble(element.getAttribute("w")))
      block.h.set(java.lang.Double.parseDouble(element.getAttribute("h")))
      Xmls.elementsByTag(element, "inputs", "input").forEach { e ->
        val inputId = e.getAttribute("id")
        val input = block.getInput(inputId)
        input?.constant?.set(e.textContent)
      }
      return block
    }
  }
}
