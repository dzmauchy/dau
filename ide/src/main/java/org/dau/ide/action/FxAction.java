package org.dau.ide.action;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import org.dau.di.Ctx;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.unmodifiableObservableList;

public final class FxAction {

  public static final int MENU_ICON_SIZE = 20;
  public static final int TOOLBAR_ICON_SIZE = 24;

  final SimpleStringProperty text = new SimpleStringProperty();
  final SimpleStringProperty description = new SimpleStringProperty();
  final SimpleStringProperty icon = new SimpleStringProperty();
  final SimpleObjectProperty<KeyCombination> key = new SimpleObjectProperty<>();
  final SimpleObjectProperty<EventHandler<ActionEvent>> handler = new SimpleObjectProperty<>();
  final SimpleBooleanProperty disabled = new SimpleBooleanProperty();

  public Object linkedObject;

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

  public FxAction handle(EventHandler<ActionEvent> handler) {
    this.handler.bind(new SimpleObjectProperty<>(handler));
    return this;
  }

  public FxAction on(Runnable task) {
    return handle(ev -> task.run());
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

  public FxAction linkedObject(Object linkedObject) {
    this.linkedObject = linkedObject;
    return this;
  }

  public void withSelected(Consumer<BooleanProperty> consumer) {
    if (selected != null) {
      consumer.accept(selected);
    }
  }

  public ObservableList<FxAction> getSubItems() {
    return subItems == null ? emptyObservableList() : unmodifiableObservableList(subItems);
  }

  @SafeVarargs
  private static Consumer<BiConsumer<FxAction, AnnotatedBeanDefinition>> forEach(Ctx ctx, Class<? extends Annotation>... qualifiers) {
    return e -> {
      var beanNames = ctx.getBeanNamesForType(FxAction.class, false, true);
      MainLoop:
      for (var beanName : beanNames) {
        var definition = ctx.getBeanDefinition(beanName);
        if (definition instanceof AnnotatedBeanDefinition abd) {
          var metadata = abd.getFactoryMethodMetadata();
          if (metadata == null) {
            continue;
          }
          for (var qualifier : qualifiers) {
            if (!metadata.isAnnotated(qualifier.getName())) {
              continue MainLoop;
            }
          }
          var bean = ctx.getBean(beanName, FxAction.class);
          e.accept(bean, abd);
        }
      }
    };
  }

  private static ActionKey key(MethodMetadata metadata, Class<? extends Annotation> groupAnnotation) {
    var data = metadata.getAnnotationAttributes(groupAnnotation.getName());
    if (data == null) {
      return new ActionKey("", 0);
    } else {
      var priority = (Integer) data.getOrDefault("priority", 0);
      var group = data.getOrDefault("name", "").toString();
      return new ActionKey(group, priority);
    }
  }

  public static void fillMenuBar(ApplicationContext ctx, MenuBar menuBar, Class<? extends Annotation> qualifier) {
    var menuActions = new TreeMap<ActionKey, TreeMap<ActionKey, ArrayList<FxAction>>>();
    forEach((Ctx) ctx, qualifier, MenuBarGroup.class).accept((action, definition) -> {
      var metadata = requireNonNull(definition.getFactoryMethodMetadata());
      var barKey = key(metadata, MenuBarGroup.class);
      var key = key(metadata, ActionGroup.class);
      menuActions
        .computeIfAbsent(barKey, k -> new TreeMap<>())
        .computeIfAbsent(key, k -> new ArrayList<>())
        .add(action);
    });
    menuActions.forEach((barKey, groups) -> {
      var menu = new Menu();
      menu.textProperty().bind(Localization.binding(barKey.group));
      menuBar.getMenus().add(menu);
      var first = new AtomicBoolean(true);
      groups.forEach((key, actions) -> {
        if (!first.compareAndSet(true, false)) {
          menu.getItems().add(new SeparatorMenuItem());
        }
        actions.forEach(a -> menu.getItems().add(menuItem(a)));
      });
    });
  }

  private record ActionKey(String group, int priority) implements Comparable<ActionKey> {
    @Override
    public int compareTo(@NonNull ActionKey o) {
      var c = Integer.compare(priority, o.priority);
      if (c != 0) {
        return c;
      }
      return group.compareTo(o.group);
    }
  }

  private static ObjectBinding<ImageView> iconBinding(ObservableStringValue icon, int size) {
    return createObjectBinding(() -> IconFactory.icon(icon.get(), size), icon);
  }

  private static void fillMenuItems(ObservableList<MenuItem> menuItems, ObservableList<FxAction> actions) {
    var map = new IdentityHashMap<FxAction, MenuItem>(actions.size());
    for (var a : actions) {
      var menuItem = menuItem(a);
      map.put(a, menuItem);
      menuItems.add(menuItem);
    }
    var lh = (ListChangeListener<FxAction>) c -> {
      while (c.next()) {
        if (c.wasRemoved()) {
          c.getRemoved().forEach(a -> menuItems.remove(map.remove(a)));
        }
        if (c.wasAdded()) {
          var newItems = c.getAddedSubList().stream()
            .map(a -> {
              var menuItem = menuItem(a);
              map.put(a, menuItem);
              return menuItem;
            })
            .toList();
          menuItems.addAll(c.getFrom(), newItems);
        }
        if (c.wasPermutated()) {
          var items = IntStream.range(c.getFrom(), c.getTo())
            .map(c::getPermutation)
            .mapToObj(menuItems::get)
            .toList();
          menuItems.remove(c.getFrom(), c.getTo());
          menuItems.addAll(c.getFrom(), items);
        }
      }
    };
    actions.addListener(lh);
  }

  public static MenuItem menuItem(FxAction action) {
    final MenuItem menuItem;
    var subItems = action.subItems;
    if (subItems == null) {
      var selected = action.selected;
      if (selected == null) {
        menuItem = new MenuItem();
      } else {
        var checkMenuItem = new CheckMenuItem();
        checkMenuItem.selectedProperty().bindBidirectional(selected);
        menuItem = checkMenuItem;
      }
    } else {
      var menu = new Menu();
      fillMenuItems(menu.getItems(), subItems);
      menuItem = menu;
    }
    menuItem.textProperty().bind(action.text);
    menuItem.acceleratorProperty().bind(action.key);
    menuItem.disableProperty().bind(action.disabled);
    menuItem.graphicProperty().bind(iconBinding(action.icon, MENU_ICON_SIZE));
    menuItem.setOnAction(ev -> {
      var h = action.handler.get();
      if (h != null) {
        h.handle(ev);
      }
    });
    return menuItem;
  }

  public static void fillToolbar(ApplicationContext context, ToolBar toolBar, Class<? extends Annotation> qualifier) {
    var toolbarActions = new TreeMap<ActionKey, ArrayList<FxAction>>();
    forEach((Ctx) context, ToolbarAction.class, qualifier).accept((action, definition) -> {
      var metadata = requireNonNull(definition.getFactoryMethodMetadata());
      var key = key(metadata, ActionGroup.class);
      toolbarActions
        .computeIfAbsent(key, k -> new ArrayList<>())
        .add(action);
    });
    var first = new AtomicBoolean(true);
    toolbarActions.forEach((key, actions) -> {
      if (!first.compareAndSet(true, false)) {
        toolBar.getItems().add(new Separator(Orientation.HORIZONTAL));
      }
      for (var action : actions) {
        toolBar.getItems().add(toolButton(action));
      }
    });
  }

  public static ButtonBase toolButton(FxAction action) {
    final ButtonBase control;
    var subItems = action.subItems;
    if (subItems == null) {
      var selected = action.selected;
      if (selected == null) {
        control = new Button();
      } else {
        var button = new ToggleButton();
        button.selectedProperty().bind(selected);
        control = button;
      }
    } else {
      var button = new MenuButton();
      fillMenuItems(button.getItems(), subItems);
      control = button;
    }
    control.setFocusTraversable(false);
    control.graphicProperty().bind(iconBinding(action.icon, TOOLBAR_ICON_SIZE));
    control.disableProperty().bind(action.disabled);
    control.setOnAction(ev -> {
      var h = action.handler.get();
      if (h != null) {
        h.handle(ev);
      }
    });
    var text = action.text.get();
    if (text != null) {
      var tooltip = new Tooltip();
      tooltip.textProperty().bind(action.text);
      control.setTooltip(tooltip);
    }
    return control;
  }
}
