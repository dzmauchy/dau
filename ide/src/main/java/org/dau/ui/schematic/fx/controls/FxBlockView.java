package org.dau.ui.schematic.fx.controls;

import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.fx.model.FxBlock;
import org.dau.ui.utils.Draggables;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.ionicons4.Ionicons4Material;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.geometry.Insets.EMPTY;
import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.paint.Color.GRAY;

public final class FxBlockView extends BorderPane {

  private static final CornerRadii RADII = new CornerRadii(5d);
  private static final Insets INSETS = new Insets(5d);

  private static final Border BORDER = new Border(new BorderStroke(GRAY, SOLID, RADII, new BorderWidths(3d), EMPTY));
  private static final Insets TITLE_INSETS = new Insets(3d);
  private static final Insets CENTER_INSETS = new Insets(5d);
  private static final Insets TOP_INSETS = new Insets(0d, 0d, 3d, 0d);
  private static final Effect BUTTON_EFFECT = new Glow(3d);
  private static final Color BACKGROUND_COLOR = new Color(0.1, 0.1, 0.1, 0.7);
  private static final Background BACKGROUND = new Background(new BackgroundFill(BACKGROUND_COLOR, RADII, INSETS));

  public final FxBlock block;
  private final Label center = new Label();
  private final VBox left = new VBox();
  private final VBox right = new VBox();
  private final HBox top = new HBox();
  private final HBox bottom = new HBox();

  private final DoubleBinding outputX = createDoubleBinding(() -> getBoundsInParent().getMaxX(), boundsInParentProperty());
  private final DoubleBinding inputX = createDoubleBinding(() -> getBoundsInParent().getMinX(), boundsInParentProperty());

  public FxBlockView(FxBlock block) {
    this.block = block;
    setBorder(BORDER);
    setBackground(BACKGROUND);
    center.setText(block.getVar());
    setCenter(center);
    setLeft(left);
    setRight(right);
    setTop(top);
    setBottom(bottom);
    left.setAlignment(Pos.TOP_LEFT);
    right.setAlignment(Pos.TOP_RIGHT);
    setMargin(center, CENTER_INSETS);
    setMargin(top, TOP_INSETS);
    block.getInputs().forEach(i -> {
      var in = new In(i);
      left.getChildren().add(in);
    });
    block.getOutputs().forEach(o -> {
      var out = new Out(o);
      right.getChildren().add(out);
    });
    top.setOnMouseClicked(ev -> {
      toFront();
      ev.consume();
    });
    top.setAlignment(Pos.CENTER_LEFT);
    top.setStyle("-fx-background-color: derive(-fx-focus-color, -30%)");
    top.setPadding(TITLE_INSETS);
    top.setSpacing(5d);
    top.getChildren().addAll(
      button(Material2MZ.MENU, this::onMenu),
      button(Ionicons4Material.INFORMATION_CIRCLE, this::onInfo),
      title(),
      new Separator(Orientation.VERTICAL),
      button(Material2AL.CLOSE, this::onClose)
    );

    relocate(block.x.get(), block.y.get());
    block.x.bind(layoutXProperty());
    block.y.bind(layoutYProperty());
    block.w.bind(widthProperty());
    block.h.bind(heightProperty());

    Draggables.enableDrag(top, this);
  }

  private Label title() {
    var title = new Label();
    title.textProperty().bind(block.name);
    title.setAlignment(Pos.BASELINE_CENTER);
    title.setMaxWidth(Double.MAX_VALUE);
    title.setMouseTransparent(true);
    HBox.setHgrow(title, Priority.ALWAYS);
    return title;
  }

  private FontIcon button(Ikon ikon, EventHandler<MouseEvent> action) {
    var button = IconFactory.icon(ikon, 20);
    button.setOnMouseEntered(ev -> {
      var effect = button.getEffect();
      if (effect == null) {
        button.setEffect(BUTTON_EFFECT);
      }
    });
    button.setOnMouseExited(ev -> {
      var effect = button.getEffect();
      if (effect != null) {
        button.setEffect(null);
      }
    });
    button.setMouseTransparent(false);
    button.setFocusTraversable(false);
    button.setOnMouseClicked(ev -> {
      action.handle(ev);
      ev.consume();
    });
    return button;
  }

  private void onClose(MouseEvent ev) {
    block.remove();
  }

  private void onInfo(MouseEvent ev) {
  }

  private void onMenu(MouseEvent ev) {

  }

  public DoubleBinding getOutputX(FxBlock.Output output) {
    return outputX;
  }

  public DoubleBinding getOutputY(FxBlock.Output output) {
    for (var c : right.getChildren()) {
      if (c instanceof Out o && o.output == output) {
        return o.inputYBinding;
      }
    }
    throw new IllegalStateException("No output found");
  }

  public DoubleBinding getInputX(FxBlock.Input input) {
    return inputX;
  }

  public DoubleBinding getInputY(FxBlock.Input input) {
    for (var c : left.getChildren()) {
      if (c instanceof In i && i.input == input) {
        return i.inputYBinding;
      }
    }
    throw new IllegalStateException("No input found");
  }

  private final class In extends Button {

    private final FxBlock.Input input;
    private final DoubleBinding inputYBinding = createDoubleBinding(
      this::inputY, boundsInParentProperty(), FxBlockView.this.boundsInParentProperty()
    );

    private In(FxBlock.Input input) {
      super(input.getId());
      this.input = input;
      setFocusTraversable(false);
      setOnAction(this::onClick);
    }

    private void onClick(ActionEvent ev) {
      var p = FxBlockView.this.getParent();
      var data = (ConnectionData) p.getUserData();
      if (data != null && data.currentOut != null) {
        data.currentOut.setSelected(false);
        block.schema.addConnection(data.currentOut.output, input);
        data.currentOut = null;
      }
    }

    private double inputY() {
      var py = FxBlockView.this.getBoundsInParent().getMinY();
      var ly = left.getBoundsInParent().getMinY();
      var cy = getBoundsInParent().getCenterY();
      return py + ly + cy;
    }
  }

  private final class Out extends ToggleButton {

    private final FxBlock.Output output;
    private final DoubleBinding inputYBinding = createDoubleBinding(
      this::inputY, boundsInParentProperty(), FxBlockView.this.boundsInParentProperty()
    );

    private Out(FxBlock.Output output) {
      super(output.getId());
      this.output = output;
      setFocusTraversable(false);
      setOnAction(this::onClick);
    }

    private void onClick(ActionEvent ev) {
      if (isSelected()) {
        var p = FxBlockView.this.getParent();
        var data = (ConnectionData) p.getUserData();
        if (data == null) {
          data = new ConnectionData();
          p.setUserData(data);
        }
        if (data.currentOut != null) {
          data.currentOut.setSelected(false);
        }
        data.currentOut = this;
      }
    }

    private double inputY() {
      var py = FxBlockView.this.getBoundsInParent().getMinY();
      var ry = right.getBoundsInParent().getMinY();
      var cy = getBoundsInParent().getCenterY();
      return py + ry + cy;
    }
  }

  private static final class ConnectionData {
    private Out currentOut;
  }
}
