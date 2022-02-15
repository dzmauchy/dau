package org.dau.ui.schematic.fx.controls;

import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.dau.ui.fx.FxCleaner;

import java.lang.ref.Cleaner;

import static java.lang.Math.hypot;
import static javafx.animation.PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT;

public class FxConnectionPath extends Group {

  private final HLineTo beginning = new HLineTo();
  private final CubicCurveTo curve = new CubicCurveTo();
  private final HLineTo end = new HLineTo();
  private final Path path = new Path(new MoveTo(), beginning, curve, end);
  private final FxArrow arrow;
  private final InvalidationListener ih = this::onInvalidate;
  private final SimpleBooleanProperty running = new SimpleBooleanProperty(this, "running");

  private PathTransition pathTransition;
  private Cleaner.Cleanable cleanable;

  public FxConnectionPath(FxArrow arrow, Observable... observables) {
    this.arrow = arrow;
    setPickOnBounds(false);
    getChildren().addAll(path, arrow);
    running.addListener(this::onRunningChange);
    var wih = new WeakInvalidationListener(ih);
    for (var o : observables) {
      o.addListener(wih);
    }
  }

  public void setBeginningX(double x) {
    beginning.setX(x);
  }

  public void setControl1(double x, double y) {
    curve.setControlX1(x);
    curve.setControlY1(y);
  }

  public void setControl2(double x, double y) {
    curve.setControlX2(x);
    curve.setControlY2(y);
  }

  public void setEnd(double x, double y) {
    double arrowWidth = arrow.getLayoutBounds().getWidth();
    double endingWidth = arrowWidth + arrow.length / 2d;
    curve.setX(x - endingWidth);
    curve.setY(y);
    end.setX(x - arrowWidth + 3d);
    arrow.setLayoutX(end.getX());
    arrow.setLayoutY(y);
  }

  public void setStroke(Color color) {
    path.setStroke(color);
    arrow.setStroke(color);
    arrow.setFill(color);
  }

  public void setStrokeWidth(double width) {
    path.setStrokeWidth(width);
    arrow.setStrokeWidth(width);
  }

  public void setStrokeDashArray(Double... array) {
    path.getStrokeDashArray().setAll(array);
  }

  public boolean isRunning() {
    return running.get();
  }

  public SimpleBooleanProperty runningProperty() {
    return running;
  }

  private Duration calculateDuration() {
    double x0 = 0d, y0 = 0d;
    double x1 = beginning.getX(), y1 = y0;
    double x2 = curve.getControlX1(), y2 = curve.getControlY1();
    double x3 = curve.getControlX2(), y3 = curve.getControlY2();
    double x4 = curve.getX(), y4 = curve.getY();
    double direct = hypot(x4 - x0, y4 - y0);
    double full = hypot(x1 - x0, y1 - y0) + hypot(x2 - x1, y2 - y1) + hypot(x3 - x2, y3 - y2) + hypot(x4 - x3, y4 - y3);
    double ratio = Math.pow(full / direct, 0.7);
    if (ratio > 100d) {
      return Duration.seconds(10d);
    } else {
      return Duration.seconds(ratio);
    }
  }

  private void onRunningChange(Observable o, Boolean ov, Boolean nv) {
    if (nv) {
      if (pathTransition == null) {
        clean();
        var ellipse = new Ellipse(6d, 4d);
        ellipse.setFill(((Color) path.getStroke()).brighter());
        ellipse.setStroke(path.getStroke());
        ellipse.setStrokeWidth(path.getStrokeWidth());
        getChildren().add(ellipse);
        pathTransition = new PathTransition(calculateDuration(), path, ellipse);
        pathTransition.setCycleCount(Animation.INDEFINITE);
        pathTransition.setOrientation(ORTHOGONAL_TO_TANGENT);
        pathTransition.play();
        var pt = pathTransition;
        var children = getChildren();
        cleanable = FxCleaner.clean(this, () -> {
          children.remove(ellipse);
          pt.stop();
        });
      }
    } else {
      clean();
      if (pathTransition != null) {
        pathTransition.stop();
        pathTransition = null;
      }
    }
  }

  private void onInvalidate(Observable o) {
    if (pathTransition != null) {
      pathTransition.stop();
      pathTransition = null;
      onRunningChange(running, false, running.get());
    }
  }

  private void clean() {
    if (cleanable != null) {
      cleanable.clean();
      cleanable = null;
    }
  }
}
