package org.dau.ui.schematic.fx.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import javafx.collections.WeakSetChangeListener;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.dau.ui.schematic.fx.model.FxBlock;
import org.dau.ui.schematic.fx.model.FxBlockConnection;
import org.dau.ui.schematic.fx.model.FxSchema;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

import static java.lang.Byte.toUnsignedInt;
import static java.lang.Math.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javafx.scene.paint.Color.rgb;
import static org.springframework.util.DigestUtils.md5Digest;

public final class FxSchemaView extends StackPane {

  private static final double L = 10d;
  private static final double PHI = 0.3;
  private static final Double[][] STROKES = {{}, {}, {}, {8d, 4d}, {4d, 4d}, {8d, 8d}};
  private static final Glow SELECT_EFFECT = new Glow(2d);

  private final Pane blockLayer = new Pane();
  private final Pane connectionLayer = new Pane();
  public final FxSchema schema;
  private final SetChangeListener<FxBlock> onBlockChange = this::onBlocksChange;
  private final SetChangeListener<FxBlockConnection> onConnectionChange = this::onConnectionsChange;
  private final HashMap<Integer, FxBlockView> blockViewMap = new HashMap<>();

  public FxSchemaView(FxSchema schema) {
    this.schema = schema;
    blockLayer.setPickOnBounds(false);
    blockLayer.setFocusTraversable(false);
    connectionLayer.setPickOnBounds(false);
    connectionLayer.setFocusTraversable(false);
    getChildren().addAll(connectionLayer, blockLayer);
    schema.getBlocks().forEach(this::addBlock);
    schema.getConnections().forEach(this::addConnection);
    schema.addBlockListener(new WeakSetChangeListener<>(onBlockChange));
    schema.addConnectionListener(new WeakSetChangeListener<>(onConnectionChange));
  }

  private void onBlocksChange(Change<? extends FxBlock> c) {
    if (c.wasRemoved()) {
      var b = c.getElementRemoved();
      blockViewMap.remove(b.id);
      blockLayer.getChildren().removeIf(n -> {
        if (n instanceof FxBlockView v) {
          return v.block == b;
        } else {
          return false;
        }
      });
    }
    if (c.wasAdded()) {
      addBlock(c.getElementAdded());
    }
  }

  private void onConnectionsChange(Change<? extends FxBlockConnection> c) {
    if (c.wasRemoved()) {
      var conn = c.getElementRemoved();
      connectionLayer.getChildren().removeIf(child -> {
        if (child instanceof Conn co) {
          return co.connection == conn;
        } else {
          return false;
        }
      });
    }
    if (c.wasAdded()) {
      addConnection(c.getElementAdded());
    }
  }

  private void addBlock(FxBlock block) {
    var view = new FxBlockView(block);
    blockLayer.getChildren().add(view);
    blockViewMap.put(block.id, view);
  }

  private void addConnection(FxBlockConnection connection) {
    var conn = new Conn(connection);
    connectionLayer.getChildren().add(conn);
  }

  private final class Conn extends Group {

    private final FxBlockConnection connection;
    private final DoubleBinding x0p;
    private final DoubleBinding x1p;
    private final DoubleBinding y0p;
    private final DoubleBinding y1p;
    private final InvalidationListener onInvalidate = this::onInvalidate;
    private final FxConnectionPath path;

    private Conn(FxBlockConnection connection) {
      this.connection = connection;
      setPickOnBounds(false);

      var outView = blockViewMap.get(connection.out().getBlock().id);
      var inView = blockViewMap.get(connection.in().getBlock().id);

      x0p = outView.getOutputX(connection.out());
      y0p = outView.getOutputY(connection.out());
      x1p = inView.getInputX(connection.in());
      y1p = inView.getInputY(connection.in());

      path = new FxConnectionPath(new FxArrow(L, PHI), x0p, y0p, x1p, y1p);
      path.runningProperty().bind(hoverProperty());
      calculateStroke();
      getChildren().addAll(path);

      var weakIH = new WeakInvalidationListener(onInvalidate);
      x0p.addListener(weakIH);
      y0p.addListener(weakIH);
      x1p.addListener(weakIH);
      y1p.addListener(weakIH);

      onInvalidate(null);
      setOnMouseEntered(e -> {
        path.setEffect(SELECT_EFFECT);
        setHover(true);
      });
      setOnMouseExited(e -> {
        path.setEffect(null);
        setHover(false);
      });
    }

    private void onInvalidate(Observable o) {
      double x0 = x0p.get(), x1 = x1p.get(), y0 = y0p.get(), y1 = y1p.get();
      setLayoutX(x0);
      setLayoutY(y0);
      path.setBeginningX(L);
      if (x0 > x1) {
        double cy = FxSchemaView.this.getHeight() / 2d;
        double avgY = (y0 + y1) / 2d;
        double h = connection.out().getBlock().h.get() + connection.in().getBlock().h.get();
        double offset = avgY < cy ? -2d * h : 2d * h;
        path.setControl1(connection.out().getBlock().w.get() * 2d, y1 - y0);
        path.setControl2(x1 - x0 - connection.in().getBlock().w.get() * 2d, y1 - y0 + (y1 - y0 + offset));
      } else {
        path.setControl1(2d * L, 0d);
        path.setControl2((x0 + x1) / 2d - x0, (y0 + y1 + (y1 - y0) / 1.5d) / 2d - y0);
      }
      path.setEnd(x1 - x0, y1 - y0);
    }

    private void calculateStroke() {
      var bytes = md5Digest(connection.out().id.getBytes(UTF_8));
      int r1 = toUnsignedInt(bytes[0]), r2 = toUnsignedInt(bytes[1]), r3 = toUnsignedInt(bytes[2]);
      int g1 = toUnsignedInt(bytes[3]), g2 = toUnsignedInt(bytes[4]), g3 = toUnsignedInt(bytes[5]);
      int b1 = toUnsignedInt(bytes[6]), b2 = toUnsignedInt(bytes[7]), b3 = toUnsignedInt(bytes[8]);
      var color = rgb(
        r1 >= 60 ? r1 : r2 >= 60 ? r2 : max(60, r3),
        g1 >= 60 ? g1 : g2 >= 60 ? g2 : max(60, g3),
        b1 >= 60 ? b1 : b2 >= 60 ? b2 : max(60, b3)
      );
      path.setStroke(color);
      path.setStrokeWidth(3d);
      var strokeSource = toUnsignedInt(bytes[9]);
      var stroke = STROKES[strokeSource % STROKES.length];
      if (stroke.length > 0) {
        path.setStrokeDashArray(stroke);
      }
    }
  }
}
