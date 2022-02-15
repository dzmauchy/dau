package org.dau.ui.schematic.fx.controls;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public final class FxArrow extends Path {

  public final double length;

  public FxArrow(double length, double phi) {
    this.length = length;
    getElements().addAll(
      new MoveTo(0d, -length * Math.sin(phi)),
      new LineTo(0d, length * Math.sin(phi)),
      new LineTo(length * Math.cos(phi), 0d),
      new ClosePath()
    );
  }
}
