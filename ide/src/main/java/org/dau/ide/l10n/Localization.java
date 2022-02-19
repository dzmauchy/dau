package org.dau.ide.l10n;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.util.Locale.US;
import static java.util.ResourceBundle.Control.FORMAT_PROPERTIES;
import static java.util.ResourceBundle.Control.getControl;
import static java.util.ResourceBundle.getBundle;
import static javafx.beans.binding.Bindings.createStringBinding;

public final class Localization {

  private static final SimpleObjectProperty<Locale> LOCALE_PROPERTY = new SimpleObjectProperty<>(US);

  private static final ObjectBinding<ResourceBundle> RESOURCE_BUNDLE = Bindings.createObjectBinding(
    () -> getBundle("i10n.ide", LOCALE_PROPERTY.get(), getControl(FORMAT_PROPERTIES)),
    LOCALE_PROPERTY
  );

  private Localization() {
  }

  private static String localizedText(String text) {
    try {
      return RESOURCE_BUNDLE.get().getString(text);
    } catch (MissingResourceException e) {
      return text;
    }
  }

  private static String localizedText(String text, Object... args) {
    return args.length == 0 ? text : MessageFormat.format(localizedText(text), args);
  }

  public static Menu menu(String text, Object... args) {
    var menu = new Menu();
    menu.textProperty().bind(createStringBinding(() -> localizedText(text, args), LOCALE_PROPERTY));
    return menu;
  }

  public static MenuItem menuItem(String text, Object... args) {
    var menuItem = new MenuItem();
    menuItem.textProperty().bind(createStringBinding(() -> localizedText(text, args), LOCALE_PROPERTY));
    return menuItem;
  }

  public static StringBinding binding(String text, Object... args) {
    return createStringBinding(() -> localizedText(text, args), LOCALE_PROPERTY);
  }

  public static StringBinding binding(ObservableValue<String> text, ObservableValue<?>... observables) {
    return switch (observables.length) {
      case 0 -> createStringBinding(
        () -> localizedText(
          text.getValue()
        ), text, LOCALE_PROPERTY
      );
      case 1 -> createStringBinding(
        () -> localizedText(
          text.getValue(), observables[0].getValue()
        ), text, LOCALE_PROPERTY, observables[0]
      );
      case 2 -> createStringBinding(
        () -> localizedText(
          text.getValue(), observables[0].getValue(), observables[1].getValue()
        ), text, LOCALE_PROPERTY, observables[0], observables[1]
      );
      default -> {
        var len = observables.length;
        var newObservables = Arrays.copyOf(observables, len + 2);
        newObservables[len] = text;
        newObservables[len + 1] = LOCALE_PROPERTY;
        yield createStringBinding(() -> {
          var array = new Object[len];
          for (int i = 0; i < len; i++) {
            array[i] = observables[i].getValue();
          }
          return localizedText(text.getValue(), array);
        }, newObservables);
      }
    };
  }
}
