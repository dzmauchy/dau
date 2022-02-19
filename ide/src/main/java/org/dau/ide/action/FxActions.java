package org.dau.ide.action;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.dau.di.Ctx;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createObjectBinding;

public final class FxActions {

  public static final int MENU_ICON_SIZE = 20;
  public static final int TOOLBAR_ICON_SIZE = 24;

  private FxActions() {}

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
    var map = new HashMap<FxAction, MenuItem>(actions.size());
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
      }
    };
    // we need to capture lh here
    menuItems.addListener((InvalidationListener) o -> Objects.hashCode(lh));
    actions.addListener(new WeakListChangeListener<>(lh));
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
        checkMenuItem.selectedProperty().bind(selected);
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
