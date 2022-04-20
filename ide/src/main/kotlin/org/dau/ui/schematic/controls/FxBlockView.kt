package org.dau.ui.schematic.controls

import javafx.beans.binding.Bindings.createDoubleBinding
import javafx.beans.binding.DoubleBinding
import javafx.collections.ListChangeListener
import javafx.collections.WeakListChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Insets.EMPTY
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.ToggleButton
import javafx.scene.effect.Glow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.layout.BorderStrokeStyle.SOLID
import javafx.scene.paint.Color
import javafx.scene.paint.Color.GRAY
import org.dau.ui.icons.IconFactory
import org.dau.ui.schematic.fx.model.FxBlock
import org.dau.ui.schematic.fx.model.FxBlock.Input
import org.dau.ui.schematic.fx.model.FxBlock.Output
import org.dau.ui.utils.Draggables
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.ionicons4.Ionicons4Material
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ
import java.util.function.Function
import java.util.stream.IntStream

@Suppress("UNUSED_PARAMETER")
class FxBlockView(val block: FxBlock) : BorderPane() {

  private val center = Label()
  private val left = VBox()
  private val right = VBox()
  private val top = HBox()
  private val bottom = HBox()

  private val outputX = createDoubleBinding({ boundsInParent.maxX }, boundsInParentProperty())
  private val inputX = createDoubleBinding({ boundsInParent.minX }, boundsInParentProperty())

  private val inputListener = changeHandler(left, Function<Input, Node> { In(it) })
  private val outputListener = changeHandler(right, Function<Output, Node> { Out(it) })

  init {
    border = BORDER
    background = BACKGROUND
    center.text = block.getVar()
    setCenter(center)
    setLeft(left)
    setRight(right)
    setTop(top)
    setBottom(bottom)
    left.alignment = Pos.TOP_LEFT
    right.alignment = Pos.TOP_RIGHT
    setMargin(center, CENTER_INSETS)
    setMargin(top, TOP_INSETS)
    block.inputs.addListener(WeakListChangeListener(inputListener))
    block.outputs.addListener(WeakListChangeListener(outputListener))
    left.children.setAll(block.inputs.stream().map { In(it) }.toList())
    right.children.setAll(block.outputs.stream().map { Out(it) }.toList())
    top.setOnMouseClicked { ev ->
      toFront()
      ev.consume()
    }
    top.alignment = Pos.CENTER_LEFT
    top.style = "-fx-background-color: derive(-fx-focus-color, -30%)"
    top.padding = TITLE_INSETS
    top.spacing = 5.0
    top.children.addAll(
      button(Material2MZ.MENU) { this.onMenu(it) },
      button(Ionicons4Material.INFORMATION_CIRCLE) { this.onInfo(it) },
      title(),
      Separator(Orientation.VERTICAL),
      button(Material2AL.CLOSE) { this.onClose(it) }
    )

    relocate(block.x.get(), block.y.get())
    block.x.bind(layoutXProperty())
    block.y.bind(layoutYProperty())
    block.w.bind(widthProperty())
    block.h.bind(heightProperty())

    Draggables.enableDrag(top, this)
  }

  private fun title(): Label {
    val title = Label()
    title.textProperty().bind(block.name)
    title.alignment = Pos.BASELINE_CENTER
    title.maxWidth = java.lang.Double.MAX_VALUE
    title.isMouseTransparent = true
    HBox.setHgrow(title, Priority.ALWAYS)
    return title
  }

  private fun button(ikon: Ikon, action: EventHandler<MouseEvent>): FontIcon {
    val button = IconFactory.icon(ikon, 20)
    button.setOnMouseEntered {
      val effect = button.effect
      if (effect == null) {
        button.effect = BUTTON_EFFECT
      }
    }
    button.setOnMouseExited {
      val effect = button.effect
      if (effect != null) {
        button.effect = null
      }
    }
    button.isMouseTransparent = false
    button.isFocusTraversable = false
    button.setOnMouseClicked { ev ->
      action.handle(ev)
      ev.consume()
    }
    return button
  }

  private fun onClose(ev: MouseEvent) {
    block.remove()
  }

  private fun onInfo(ev: MouseEvent) {}

  private fun onMenu(ev: MouseEvent) {}

  fun getOutputX(output: Output): DoubleBinding {
    return outputX
  }

  fun getOutputY(output: Output): DoubleBinding {
    for (c in right.children) {
      if (c is Out && c.output === output) {
        return c.inputYBinding
      }
    }
    throw IllegalStateException("No output found")
  }

  fun getInputX(input: Input): DoubleBinding {
    return inputX
  }

  fun getInputY(input: Input): DoubleBinding {
    for (c in left.children) {
      if (c is In && c.input === input) {
        return c.inputYBinding
      }
    }
    throw IllegalStateException("No input found")
  }

  private inner class In constructor(val input: Input) : Button(input.id) {

    val inputYBinding = createDoubleBinding(
      { this.inputY() }, boundsInParentProperty(), this@FxBlockView.boundsInParentProperty()
    )

    init {
      isFocusTraversable = false
      onAction = EventHandler { this.onClick(it) }
    }

    private fun onClick(ev: ActionEvent) {
      when (val data = this@FxBlockView.parent.userData) {
        is ConnectionData -> {
          if (block.schema.addConnection(data.currentOut?.output, input)) {
            data.currentOut?.isSelected = false
            data.currentOut = null
          }
        }
      }
    }

    private fun inputY(): Double {
      val py = this@FxBlockView.boundsInParent.minY
      val ly = left.boundsInParent.minY
      val cy = boundsInParent.centerY
      return py + ly + cy
    }
  }

  private inner class Out constructor(val output: Output) : ToggleButton(output.id) {
    val inputYBinding = createDoubleBinding(
      { this.inputY() }, boundsInParentProperty(), this@FxBlockView.boundsInParentProperty()
    )

    init {
      isFocusTraversable = false
      onAction = EventHandler { this.onClick(it) }
    }

    private fun onClick(ev: ActionEvent) {
      if (isSelected) {
        val p = this@FxBlockView.parent
        var data = p.userData as ConnectionData?
        if (data == null) {
          data = ConnectionData()
          p.userData = data
        }
        if (data.currentOut != null) {
          data.currentOut!!.isSelected = false
        }
        data.currentOut = this
      }
    }

    private fun inputY(): Double {
      val py = this@FxBlockView.boundsInParent.minY
      val ry = right.boundsInParent.minY
      val cy = boundsInParent.centerY
      return py + ry + cy
    }
  }

  private class ConnectionData {
    var currentOut: Out? = null
  }

  companion object {

    private val RADII = CornerRadii(5.0)
    private val INSETS = Insets(5.0)

    private val BORDER = Border(BorderStroke(GRAY, SOLID, RADII, BorderWidths(3.0), EMPTY))
    private val TITLE_INSETS = Insets(3.0)
    private val CENTER_INSETS = Insets(5.0)
    private val TOP_INSETS = Insets(0.0, 0.0, 3.0, 0.0)
    private val BUTTON_EFFECT = Glow(3.0)
    private val BACKGROUND_COLOR = Color(0.1, 0.1, 0.1, 0.7)
    private val BACKGROUND = Background(BackgroundFill(BACKGROUND_COLOR, RADII, INSETS))

    private fun <T> changeHandler(box: VBox, f: Function<T, Node>): ListChangeListener<T> {
      return ListChangeListener<T> { c ->
        while (c.next()) {
          if (c.wasRemoved()) {
            box.children.remove(c.from, c.to)
          }
          if (c.wasAdded()) {
            box.children.addAll(c.from, c.addedSubList.stream().map(f).toList())
          }
          if (c.wasPermutated()) {
            val nodes = IntStream.range(c.from, c.to)
              .map { c.getPermutation(it) }
              .mapToObj { box.children[it] }
              .toList()
            box.children.remove(c.from, c.to)
            box.children.addAll(c.from, nodes)
          }
        }
      }
    }
  }
}
