package org.dau.ui.utils

import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback
import org.dau.ide.l10n.Localization
import java.util.function.Consumer

class TableColumnBuilder<S, T>(text: StringBinding) {

  private val column = TableColumn<S, T>()

  constructor(name: String, vararg args: Any?) : this(Localization.binding(name, *args))

  init {
    column.textProperty().bind(text)
  }

  fun width(width: Int): TableColumnBuilder<S, T> {
    return width(width * 0.75, width.toDouble(), (width * 3).toDouble())
  }

  fun width(min: Double, pref: Double, max: Double): TableColumnBuilder<S, T> {
    column.minWidth = min
    column.prefWidth = pref
    column.maxWidth = max
    return this
  }

  fun graphic(node: Node?): TableColumnBuilder<S, T> {
    column.graphic = node
    return this
  }

  fun graphic(node: ObservableValue<Node?>?): TableColumnBuilder<S, T> {
    column.graphicProperty().bind(node)
    return this
  }

  fun value(v: Callback<TableColumn.CellDataFeatures<S, T>?, ObservableValue<T>?>?): TableColumnBuilder<S, T> {
    column.cellValueFactory = v
    return this
  }

  fun cell(c: Callback<TableColumn<S, T>?, TableCell<S, T>?>?): TableColumnBuilder<S, T> {
    column.cellFactory = c
    return this
  }

  fun with(consumer: Consumer<TableColumn<S, T>?>): TableColumnBuilder<S, T> {
    consumer.accept(column)
    return this
  }

  fun build(): TableColumn<S, T> {
    return column
  }
}