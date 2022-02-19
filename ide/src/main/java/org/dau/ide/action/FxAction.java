package org.dau.ide.action;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCombination;
import org.dau.ide.l10n.Localization;

public final class FxAction {

  final SimpleStringProperty text = new SimpleStringProperty();
  final SimpleStringProperty description = new SimpleStringProperty();
  final SimpleStringProperty icon = new SimpleStringProperty();
  final SimpleObjectProperty<KeyCombination> key = new SimpleObjectProperty<>();
  final SimpleObjectProperty<EventHandler<ActionEvent>> handler = new SimpleObjectProperty<>();
  final SimpleBooleanProperty disabled = new SimpleBooleanProperty();

  BooleanProperty selected;
  ObservableList<FxAction> subItems;

  public FxAction() {
  }

  public FxAction(String text, Object... args) {
    if (text != null) {
      this.text.bind(Localization.binding(text, args));
    }
  }

  public FxAction(String icon, String text, Object... args) {
    this(text, args);
    if (icon != null) {
      this.icon.bind(new SimpleStringProperty(icon));
    }
  }

  public FxAction(KeyCombination key, String icon, String text, Object... args) {
    this(icon, text, args);
    if (key != null) {
      this.key.bind(new SimpleObjectProperty<>(key));
    }
  }

  public FxAction(String description, KeyCombination key, String icon, String text, Object... args) {
    this(key, icon, text, args);
    if (description != null) {
      this.description.bind(Localization.binding(description, args));
    }
  }

  public FxAction(String description, String icon, String text, Object... args) {
    this(icon, text, args);
    if (description != null) {
      this.description.bind(Localization.binding(description, args));
    }
  }

  public FxAction text(ObservableValue<String> text, ObservableValue<?>... args) {
    this.text.bind(Localization.binding(text, args));
    return this;
  }

  public FxAction icon(ObservableValue<String> icon) {
    this.text.bind(icon);
    return this;
  }

  public FxAction description(ObservableValue<String> text, ObservableValue<?>... args) {
    this.text.bind(Localization.binding(text, args));
    return this;
  }

  public FxAction key(ObservableValue<KeyCombination> key) {
    this.key.bind(key);
    return this;
  }

  public FxAction subItems(FxAction... actions) {
    return subItems(FXCollections.observableArrayList(actions));
  }

  public FxAction subItems(ObservableList<FxAction> actions) {
    this.subItems = actions;
    return this;
  }

  public FxAction on(EventHandler<ActionEvent> handler) {
    this.handler.bind(new SimpleObjectProperty<>(handler));
    return this;
  }

  public FxAction on(Runnable task) {
    return on(ev -> task.run());
  }

  public FxAction handler(ObservableValue<EventHandler<ActionEvent>> handler) {
    this.handler.bind(handler);
    return this;
  }

  public FxAction selected(BooleanProperty property) {
    this.selected = property;
    return this;
  }

  public FxAction disabled(ObservableBooleanValue disabled) {
    this.disabled.bind(disabled);
    return this;
  }
}
