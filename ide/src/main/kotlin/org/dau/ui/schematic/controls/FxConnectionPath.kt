package org.dau.ui.schematic.controls

import javafx.animation.Animation
import javafx.animation.PathTransition
import javafx.animation.PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.WeakInvalidationListener
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.shape.*
import javafx.util.Duration
import org.dau.ui.fx.FxCleaner
import java.lang.Math.hypot
import java.lang.ref.Cleaner

class FxConnectionPath(private val arrow: FxArrow, vararg observables: Observable) : Group() {

  private val beginning = HLineTo()
  private val curve = CubicCurveTo()
  private val end = HLineTo()
  private val path = Path(MoveTo(), beginning, curve, end)
  private val ih = InvalidationListener { this.onInvalidate(it) }
  private val running = SimpleBooleanProperty(this, "running")

  private var pathTransition: PathTransition? = null
  private var cleanable: Cleaner.Cleanable? = null

  val isRunning: Boolean
    get() = running.get()

  init {
    isPickOnBounds = false
    children.addAll(path, arrow)
    running.addListener { o, ov, nv -> this.onRunningChange(o, ov, nv) }
    val wih = WeakInvalidationListener(ih)
    for (o in observables) {
      o.addListener(wih)
    }
  }

  fun setBeginningX(x: Double) {
    beginning.x = x
  }

  fun setControl1(x: Double, y: Double) {
    curve.controlX1 = x
    curve.controlY1 = y
  }

  fun setControl2(x: Double, y: Double) {
    curve.controlX2 = x
    curve.controlY2 = y
  }

  fun setEnd(x: Double, y: Double) {
    val arrowWidth = arrow.layoutBounds.width
    val endingWidth = arrowWidth + arrow.length / 2.0
    curve.x = x - endingWidth
    curve.y = y
    end.x = x - arrowWidth + 3.0
    arrow.layoutX = end.x
    arrow.layoutY = y
  }

  fun setStroke(color: Color) {
    path.stroke = color
    arrow.stroke = color
    arrow.fill = color
  }

  fun setStrokeWidth(width: Double) {
    path.strokeWidth = width
    arrow.strokeWidth = width
  }

  fun setStrokeDashArray(vararg array: Double?) {
    path.strokeDashArray.setAll(*array)
  }

  fun runningProperty(): SimpleBooleanProperty {
    return running
  }

  private fun calculateDuration(): Duration {
    val x0 = 0.0
    val y0 = 0.0
    val x1 = beginning.x
    val x2 = curve.controlX1
    val y2 = curve.controlY1
    val x3 = curve.controlX2
    val y3 = curve.controlY2
    val x4 = curve.x
    val y4 = curve.y
    val direct = hypot(x4 - x0, y4 - y0)
    val full = hypot(x1 - x0, y0 - y0) + hypot(x2 - x1, y2 - y0) + hypot(x3 - x2, y3 - y2) + hypot(x4 - x3, y4 - y3)
    val ratio = Math.pow(full / direct, 0.7)
    return if (ratio > 100.0) {
      Duration.seconds(10.0)
    } else {
      Duration.seconds(ratio)
    }
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onRunningChange(o: Observable, ov: Boolean?, nv: Boolean) {
    if (nv) {
      if (pathTransition == null) {
        clean()
        val ellipse = Ellipse(6.0, 4.0)
        ellipse.fill = (path.stroke as Color).brighter()
        ellipse.stroke = path.stroke
        ellipse.strokeWidth = path.strokeWidth
        children.add(ellipse)
        pathTransition = PathTransition(calculateDuration(), path, ellipse)
        pathTransition!!.cycleCount = Animation.INDEFINITE
        pathTransition!!.orientation = ORTHOGONAL_TO_TANGENT
        pathTransition!!.play()
        val pt = pathTransition
        val children = children
        cleanable = FxCleaner.clean(this) {
          children.remove(ellipse)
          pt!!.stop()
        }
      }
    } else {
      clean()
      if (pathTransition != null) {
        pathTransition!!.stop()
        pathTransition = null
      }
    }
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onInvalidate(o: Observable) {
    if (pathTransition != null) {
      pathTransition!!.stop()
      pathTransition = null
      onRunningChange(running, false, running.get())
    }
  }

  private fun clean() {
    if (cleanable != null) {
      cleanable!!.clean()
      cleanable = null
    }
  }
}
