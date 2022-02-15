package org.dau.ui.utils;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;

import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.MouseDragEvent.MOUSE_DRAG_RELEASED;
import static javafx.scene.input.MouseEvent.*;

public final class Draggables {

  private Draggables() {}

  public static void enableDrag(Node handle, Node node) {
    var base = new AtomicReference<Point2D>();
    var old = new AtomicReference<Point2D>();
    var oldEffect = new AtomicReference<Effect>();
    handle.addEventFilter(DRAG_DETECTED, ev -> {
      if (ev.getSource() != handle) {
        return;
      }
      base.set(new Point2D(ev.getSceneX(), ev.getSceneY()));
      old.set(new Point2D(node.getLayoutX(), node.getLayoutY()));
      oldEffect.set(node.getEffect());
      node.setEffect(new Glow(0.5));
      ((Node) ev.getSource()).startFullDrag();
      ev.consume();
    });
    var onRelease = (EventHandler<MouseEvent>) ev -> {
      if (ev.getSource() != handle || base.get() == null) {
        return;
      }
      base.set(null);
      old.set(null);
      node.setEffect(oldEffect.get());
      oldEffect.set(null);
      ev.consume();
    };
    handle.addEventFilter(MOUSE_DRAG_RELEASED, onRelease);
    handle.addEventFilter(MOUSE_RELEASED, onRelease);
    handle.addEventFilter(MOUSE_DRAGGED, ev -> {
      var b = base.get();
      if (b != null && ev.getSource() == handle) {
        var newX = ev.getSceneX() - base.get().getX() + old.get().getX();
        var newY = ev.getSceneY() - base.get().getY() + old.get().getY();
        node.relocate(newX, newY);
        ev.consume();
      }
    });
  }
}
