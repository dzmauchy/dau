package org.dau.ui.schematic.controls

import javafx.scene.shape.ClosePath
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path

class FxArrow(val length: Double, phi: Double) : Path() {

  init {
    elements.addAll(
      MoveTo(0.0, -length * Math.sin(phi)),
      LineTo(0.0, length * Math.sin(phi)),
      LineTo(length * Math.cos(phi), 0.0),
      ClosePath()
    )
  }
}
