package org.dau.ui.utils;

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;
import org.dau.ide.l10n.Localization;

import java.util.function.Consumer;

public final class TableColumnBuilder<S, T> {

  private final TableColumn<S, T> column = new TableColumn<>();

  public TableColumnBuilder(String name, Object... args) {
    this(Localization.binding(name, args));
  }

  public TableColumnBuilder(StringBinding text) {
    column.textProperty().bind(text);
  }

  public TableColumnBuilder<S, T> width(int width) {
    return width(width * 0.75, width, width * 3);
  }

  public TableColumnBuilder<S, T> width(double min, double pref, double max) {
    column.setMinWidth(min);
    column.setPrefWidth(pref);
    column.setMaxWidth(max);
    return this;
  }

  public TableColumnBuilder<S, T> graphic(Node node) {
    column.setGraphic(node);
    return this;
  }

  public TableColumnBuilder<S, T> graphic(ObservableValue<Node> node) {
    column.graphicProperty().bind(node);
    return this;
  }

  public TableColumnBuilder<S, T> value(Callback<CellDataFeatures<S, T>, ObservableValue<T>> v) {
    column.setCellValueFactory(v);
    return this;
  }

  public TableColumnBuilder<S, T> cell(Callback<TableColumn<S, T>, TableCell<S, T>> c) {
    column.setCellFactory(c);
    return this;
  }

  public TableColumnBuilder<S, T> with(Consumer<TableColumn<S, T>> consumer) {
    consumer.accept(column);
    return this;
  }

  public TableColumn<S, T> build() {
    return column;
  }
}
