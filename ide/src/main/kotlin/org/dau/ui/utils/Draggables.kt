package org.dau.ui.utils

import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.effect.Effect
import javafx.scene.effect.Glow
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import java.util.concurrent.atomic.AtomicReference

object Draggables {
  @JvmStatic
  fun enableDrag(handle: Node, node: Node) {
    val base = AtomicReference<Point2D?>()
    val old = AtomicReference<Point2D?>()
    val oldEffect = AtomicReference<Effect?>()
    handle.addEventFilter(MouseEvent.DRAG_DETECTED) { ev ->
      if (ev.source !== handle) {
        return@addEventFilter
      }
      base.set(node.parent.sceneToLocal(ev.sceneX, ev.sceneY))
      old.set(Point2D(node.layoutX, node.layoutY))
      oldEffect.set(node.effect)
      node.effect = Glow(0.5)
      (ev.source as Node).startFullDrag()
      ev.consume()
    }
    val onRelease = EventHandler<MouseEvent> { ev ->
      if (ev.source !== handle || base.get() == null) {
        return@EventHandler
      }
      base.set(null)
      old.set(null)
      node.effect = oldEffect.get()
      oldEffect.set(null)
      ev.consume()
    }
    handle.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, onRelease)
    handle.addEventFilter(MouseEvent.MOUSE_RELEASED, onRelease)
    handle.addEventFilter(MouseEvent.MOUSE_DRAGGED) { ev: MouseEvent ->
      val b = base.get()
      if (b != null && ev.source === handle) {
        val lp = node.parent.sceneToLocal(ev.sceneX, ev.sceneY)
        val newX = lp.x - base.get()!!.x + old.get()!!.x
        val newY = lp.y - base.get()!!.y + old.get()!!.y
        node.relocate(newX, newY)
        ev.consume()
      }
    }
  }
}