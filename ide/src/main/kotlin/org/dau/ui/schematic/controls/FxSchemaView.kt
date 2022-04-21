package org.dau.ui.schematic.controls

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.WeakInvalidationListener
import javafx.beans.binding.DoubleBinding
import javafx.collections.SetChangeListener
import javafx.collections.SetChangeListener.Change
import javafx.collections.WeakSetChangeListener
import javafx.scene.Group
import javafx.scene.effect.Glow
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color.rgb
import org.dau.ui.schematic.model.FxBlock
import org.dau.ui.schematic.model.FxBlockConnection
import org.dau.ui.schematic.model.FxSchema
import org.springframework.util.DigestUtils.md5Digest
import java.lang.Byte.toUnsignedInt
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.math.max

class FxSchemaView(val schema: FxSchema) : StackPane() {

  private val blockLayer = Pane()
  private val connectionLayer = Pane()
  private val onBlockChange = SetChangeListener<FxBlock> { this.onBlocksChange(it) }
  private val onConnectionChange = SetChangeListener<FxBlockConnection> { this.onConnectionsChange(it) }
  private val blockViewMap = HashMap<Int, FxBlockView>()

  init {
    blockLayer.isPickOnBounds = false
    blockLayer.isFocusTraversable = false
    connectionLayer.isPickOnBounds = false
    connectionLayer.isFocusTraversable = false
    children.addAll(connectionLayer, blockLayer)
    schema.getBlocks().forEach { this.addBlock(it) }
    schema.getConnections().forEach { this.addConnection(it) }
    schema.addBlockListener(WeakSetChangeListener(onBlockChange))
    schema.addConnectionListener(WeakSetChangeListener(onConnectionChange))
  }

  private fun onBlocksChange(c: Change<out FxBlock>) {
    if (c.wasRemoved()) {
      val b = c.elementRemoved
      blockViewMap.remove(b.id)
      blockLayer.children.removeIf { n ->
        if (n is FxBlockView) {
          n.block === b
        } else {
          false
        }
      }
    }
    if (c.wasAdded()) {
      addBlock(c.elementAdded)
    }
  }

  private fun onConnectionsChange(c: Change<out FxBlockConnection>) {
    if (c.wasRemoved()) {
      val conn = c.elementRemoved
      connectionLayer.children.removeIf { child ->
        if (child is Conn) {
          child.connection === conn
        } else {
          false
        }
      }
    }
    if (c.wasAdded()) {
      addConnection(c.elementAdded)
    }
  }

  private fun addBlock(block: FxBlock) {
    val view = FxBlockView(block)
    blockLayer.children.add(view)
    blockViewMap[block.id] = view
  }

  private fun addConnection(connection: FxBlockConnection) {
    val conn = Conn(connection)
    connectionLayer.children.add(conn)
  }

  private inner class Conn constructor(val connection: FxBlockConnection) : Group() {

    private val x0p: DoubleBinding
    private val x1p: DoubleBinding
    private val y0p: DoubleBinding
    private val y1p: DoubleBinding
    private val onInvalidate = InvalidationListener { this.onInvalidate(it) }
    private val path: FxConnectionPath

    init {
      isPickOnBounds = false

      val outView = blockViewMap[connection.out.block.id]!!
      val inView = blockViewMap[connection.inp.block.id]!!

      x0p = outView.getOutputX(connection.out)
      y0p = outView.getOutputY(connection.out)
      x1p = inView.getInputX(connection.inp)
      y1p = inView.getInputY(connection.inp)

      path = FxConnectionPath(FxArrow(L, PHI), x0p, y0p, x1p, y1p)
      path.runningProperty().bind(hoverProperty())
      calculateStroke()
      children.addAll(path)

      val weakIH = WeakInvalidationListener(onInvalidate)
      x0p.addListener(weakIH)
      y0p.addListener(weakIH)
      x1p.addListener(weakIH)
      y1p.addListener(weakIH)

      onInvalidate(null)
      setOnMouseEntered {
        path.effect = SELECT_EFFECT
        isHover = true
      }
      setOnMouseExited {
        path.effect = null
        isHover = false
      }
    }

    private fun onInvalidate(@Suppress("UNUSED_PARAMETER") o: Observable?) {
      val x0 = x0p.get()
      val x1 = x1p.get()
      val y0 = y0p.get()
      val y1 = y1p.get()
      layoutX = x0
      layoutY = y0
      path.setBeginningX(L)
      if (x0 > x1) {
        val cy = this@FxSchemaView.height / 2.0
        val avgY = (y0 + y1) / 2.0
        val h = connection.out.block.h.get() + connection.inp.block.h.get()
        val offset = if (avgY < cy) -2.0 * h else 2.0 * h
        path.setControl1(connection.out.block.w.get() * 2.0, y1 - y0)
        path.setControl2(x1 - x0 - connection.inp.block.w.get() * 2.0, y1 - y0 + (y1 - y0 + offset))
      } else {
        path.setControl1(2.0 * L, 0.0)
        path.setControl2((x0 + x1) / 2.0 - x0, (y0 + y1 + (y1 - y0) / 1.5) / 2.0 - y0)
      }
      path.setEnd(x1 - x0, y1 - y0)
    }

    private fun calculateStroke() {
      val bytes = md5Digest(connection.out.id.toByteArray(UTF_8))
      val r1 = toUnsignedInt(bytes[0])
      val r2 = toUnsignedInt(bytes[1])
      val r3 = toUnsignedInt(bytes[2])
      val g1 = toUnsignedInt(bytes[3])
      val g2 = toUnsignedInt(bytes[4])
      val g3 = toUnsignedInt(bytes[5])
      val b1 = toUnsignedInt(bytes[6])
      val b2 = toUnsignedInt(bytes[7])
      val b3 = toUnsignedInt(bytes[8])
      val color = rgb(
        if (r1 >= 60) r1 else if (r2 >= 60) r2 else max(60, r3),
        if (g1 >= 60) g1 else if (g2 >= 60) g2 else max(60, g3),
        if (b1 >= 60) b1 else if (b2 >= 60) b2 else max(60, b3)
      )
      path.setStroke(color)
      path.setStrokeWidth(3.0)
      val strokeSource = toUnsignedInt(bytes[9])
      val stroke = STROKES[strokeSource % STROKES.size]
      if (stroke.isNotEmpty()) {
        path.setStrokeDashArray(*stroke)
      }
    }
  }

  companion object {

    private const val L = 10.0
    private const val PHI = 0.3
    private val STROKES = arrayOf(
      emptyArray(), emptyArray(), emptyArray(), arrayOf(8.0, 4.0), arrayOf(4.0, 4.0), arrayOf(8.0, 8.0)
    )
    private val SELECT_EFFECT = Glow(2.0)
  }
}
