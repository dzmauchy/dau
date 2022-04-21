package org.dau.ui.schematic.model

import javafx.beans.binding.Bindings
import javafx.beans.binding.IntegerBinding
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections.observableSet
import javafx.collections.SetChangeListener
import javafx.collections.SetChangeListener.Change
import org.dau.ui.schematic.model.FxBlock.Input
import org.dau.ui.schematic.model.FxBlock.Output
import org.dau.ui.utils.Encoders
import org.dau.ui.utils.Xmls.elementsByTag
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class FxSchema private constructor(id: String?) {

  private val ids = BitSet()
  val id: String
  val name = SimpleStringProperty(this, "name", "Schema")
  private val blocks = observableSet<FxBlock>()
  private val connections = observableSet<FxBlockConnection>()
  private val blockMap = HashMap<Int, FxBlock>()
  private val outputs = HashMap<Int, HashMap<String, ArrayList<Input>>>()
  private val inputs = HashMap<Int, HashMap<String, ArrayList<Output>>>()
  private val outMap = HashMap<Int, ArrayList<FxBlockConnection>>()
  private val inMap = HashMap<Int, ArrayList<FxBlockConnection>>()

  init {
    this.id = id ?: Encoders.generateId(this)
    blocks.addListener { c: Change<out FxBlock> ->
      if (c.wasRemoved()) {
        val block = c.elementRemoved
        blockMap.remove(block.id)
        ids.clear(block.id)
        connections.removeIf { co -> co.inp.block === block || co.out.block === block }
      }
      if (c.wasAdded()) {
        val block = c.elementAdded
        ids.set(block.id)
        blockMap[block.id] = block
      }
    }
    connections.addListener { c: Change<out FxBlockConnection> ->
      if (c.wasRemoved()) {
        val conn = c.elementRemoved
        outputs.computeIfPresent(conn.out.block.id) { _, old ->
          old.computeIfPresent(conn.out.id) { _, l ->
            l.removeIf { it === conn.inp }
            if (l.isEmpty()) null else l
          }
          if (old.isEmpty()) null else old
        }
        inputs.computeIfPresent(conn.inp.block.id) { _, old ->
          old.computeIfPresent(conn.inp.id) { _, l ->
            l.removeIf { it === conn.out }
            if (l.isEmpty()) null else l
          }
          if (old.isEmpty()) null else old
        }
        outMap.computeIfPresent(conn.out.block.id) { _, old ->
          if (old.removeIf { e -> conn === e }) {
            if (old.isEmpty()) return@computeIfPresent null
          }
          old
        }
        inMap.computeIfPresent(conn.inp.block.id) { _, old ->
          if (old.removeIf { e -> conn === e }) {
            if (old.isEmpty()) return@computeIfPresent null
          }
          old
        }
      }
      if (c.wasAdded()) {
        val conn = c.elementAdded
        outputs
          .computeIfAbsent(conn.out.block.id) { HashMap() }
          .computeIfAbsent(conn.out.id) { ArrayList() }
          .add(conn.inp)
        inputs
          .computeIfAbsent(conn.inp.block.id) { HashMap() }
          .computeIfAbsent(conn.inp.id) { ArrayList() }
          .add(conn.out)
      }
    }
  }

  constructor() : this(null)

  internal fun blockId(): Int {
    return ids.nextClearBit(0)
  }

  fun removeBlock(id: Int): Boolean {
    return blocks.removeIf { e -> e.id == id }
  }

  fun addConnection(output: Output, input: Input): Boolean {
    return connections.add(FxBlockConnection(output, input))
  }

  fun removeBlock(block: FxBlock): Boolean {
    return blocks.remove(block)
  }

  fun getBlock(id: Int): FxBlock? {
    return blockMap[id]
  }

  fun getBlocks(): Stream<FxBlock> {
    return blocks.stream()
  }

  fun getConnections(): Stream<FxBlockConnection> {
    return connections.stream()
  }

  fun getInputConnections(id: Int): Stream<FxBlockConnection> {
    val l = inMap[id]
    return l?.stream() ?: Stream.empty()
  }

  fun getOutputConnections(id: Int): Stream<FxBlockConnection> {
    val l = outMap[id]
    return l?.stream() ?: Stream.empty()
  }

  fun getOutputConnections(blockId: Int, outputId: String): Stream<Input> {
    return outputs[blockId]?.get(outputId)?.stream() ?: Stream.empty()
  }

  fun getInputConnections(blockId: Int, inputId: String): Stream<Output> {
    return inputs[blockId]?.get(inputId)?.stream() ?: Stream.empty()
  }

  fun addBlockListener(listener: SetChangeListener<FxBlock>) {
    blocks.addListener(listener)
  }

  fun removeBlockListener(listener: SetChangeListener<FxBlock>) {
    blocks.removeListener(listener)
  }

  fun addConnectionListener(listener: SetChangeListener<FxBlockConnection>) {
    connections.addListener(listener)
  }

  fun removeConnectionListener(listener: SetChangeListener<FxBlockConnection>) {
    connections.removeListener(listener)
  }

  fun addBlock(block: FxBlock) {
    blocks.add(block)
  }

  fun toXml(doc: Document): Element {
    val el = doc.createElement("schema")
    el.setAttribute("id", id)
    el.setAttribute("name", name.get())
    val blocksEl = doc.createElement("blocks")
    el.appendChild(blocksEl)
    for (block in blocks) {
      blocksEl.appendChild(block.toXml(doc))
    }
    val connectionsEl = doc.createElement("connections")
    el.appendChild(connectionsEl)
    for (connection in connections) {
      connectionsEl.appendChild(connection.toXml(doc))
    }
    return el
  }

  fun save(outputStream: OutputStream) {
    try {
      val dbf = DocumentBuilderFactory.newDefaultInstance()
      val db = dbf.newDocumentBuilder()
      val doc = db.newDocument()
      val schema = toXml(doc)
      doc.appendChild(schema)

      val tf = TransformerFactory.newDefaultInstance()
      tf.setAttribute("indent-number", 2)
      val t = tf.newTransformer()
      t.setOutputProperty(OutputKeys.INDENT, "yes")
      t.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
      t.setOutputProperty(OutputKeys.STANDALONE, "true")

      t.transform(DOMSource(doc), StreamResult(outputStream))
    } catch (e: ParserConfigurationException) {
      throw IllegalStateException(e)
    } catch (e: TransformerException) {
      throw IllegalStateException(e)
    }

  }

  fun save(path: Path) {
    try {
      Files.newOutputStream(path).use { outputStream -> save(outputStream) }
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }

  }

  fun blockCount(): IntegerBinding {
    return Bindings.size(blocks)
  }

  fun toFileName(): String {
    return "schema-$id.xml"
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return other is FxSchema && other.id == id
  }

  override fun toString(): String {
    return "Schema($id)"
  }

  companion object {

    fun fromXml(element: Element, classLoader: ClassLoader): FxSchema {
      val id = element.getAttribute("id")
      val name = element.getAttribute("name")
      val schema = FxSchema(id)
      schema.name.set(name)
      elementsByTag(element, "blocks", "block").forEach { el ->
        val block = FxBlock.fromXml(schema, classLoader, el)
        schema.blocks.add(block)
      }
      elementsByTag(element, "connections", "connection").forEach { el ->
        val outBlockId = Integer.parseInt(el.getAttribute("out-block-id"))
        val out = el.getAttribute("out")
        val inBlockId = Integer.parseInt(el.getAttribute("in-block-id"))
        val inp = el.getAttribute("in")
        val outBlock = schema.getBlock(outBlockId)
        val inBlock = schema.getBlock(inBlockId)
        if (outBlock == null || inBlock == null) {
          return@forEach
        }
        val output = outBlock.getOutput(out)
        val input = inBlock.getInput(inp)
        if (output == null || input == null) {
          return@forEach
        }
        schema.connections.add(FxBlockConnection(output, input))
      }
      return schema
    }

    fun load(inputStream: InputStream, classLoader: ClassLoader): FxSchema {
      try {
        val dbf = DocumentBuilderFactory.newDefaultInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(inputStream)
        return fromXml(doc.documentElement, classLoader)
      } catch (e: ParserConfigurationException) {
        throw IllegalStateException(e)
      } catch (e: SAXException) {
        throw IllegalStateException(e)
      } catch (e: IOException) {
        throw UncheckedIOException(e)
      }

    }

    fun load(reader: Reader, classLoader: ClassLoader): FxSchema {
      try {
        val dbf = DocumentBuilderFactory.newDefaultInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(InputSource(reader))
        return fromXml(doc.documentElement, classLoader)
      } catch (e: ParserConfigurationException) {
        throw IllegalStateException(e)
      } catch (e: SAXException) {
        throw IllegalStateException(e)
      } catch (e: IOException) {
        throw UncheckedIOException(e)
      }

    }

    fun load(path: Path, classLoader: ClassLoader): FxSchema {
      try {
        Files.newBufferedReader(path, UTF_8).use { reader -> return load(reader, classLoader) }
      } catch (e: IOException) {
        throw UncheckedIOException(e)
      }
    }
  }
}
