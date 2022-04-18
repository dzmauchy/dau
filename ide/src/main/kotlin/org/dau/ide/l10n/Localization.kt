package org.dau.ide.l10n

import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.binding.Bindings.createStringBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import java.text.MessageFormat
import java.util.*
import java.util.Locale.US
import java.util.ResourceBundle.Control.FORMAT_PROPERTIES
import java.util.ResourceBundle.Control.getControl
import java.util.ResourceBundle.getBundle

object Localization {

  private val LOCALE_PROPERTY = SimpleObjectProperty(US)

  private val RESOURCE_BUNDLE = Bindings.createObjectBinding(
    { getBundle("i10n.ide", LOCALE_PROPERTY.get(), getControl(FORMAT_PROPERTIES)) }, LOCALE_PROPERTY
  )

  private fun localizedText(text: String): String? = try {
    RESOURCE_BUNDLE.get().getString(text)
  } catch (e: MissingResourceException) {
    text
  }

  private fun localizedText(text: String, vararg args: Any?): String {
    return if (args.isEmpty()) text else MessageFormat.format(localizedText(text)!!, *args)
  }

  @JvmStatic
  fun menu(text: String, vararg args: Any): Menu {
    val menu = Menu()
    menu.textProperty().bind(createStringBinding({ localizedText(text, *args) }, LOCALE_PROPERTY))
    return menu
  }

  @JvmStatic
  fun menuItem(text: String, vararg args: Any): MenuItem {
    val menuItem = MenuItem()
    menuItem.textProperty().bind(createStringBinding({ localizedText(text, *args) }, LOCALE_PROPERTY))
    return menuItem
  }

  @JvmStatic
  fun binding(text: String, vararg args: Any?): StringBinding {
    return createStringBinding({ localizedText(text, *args) }, LOCALE_PROPERTY)
  }

  @JvmStatic
  fun binding(text: ObservableValue<String>, vararg observables: ObservableValue<*>): StringBinding =
    when (observables.size) {
      0 -> createStringBinding({ localizedText(text.value) }, text, LOCALE_PROPERTY)
      1 -> createStringBinding(
        { localizedText(text.value, observables[0].value) }, text, LOCALE_PROPERTY, observables[0]
      )
      2 -> createStringBinding(
        { localizedText(text.value, observables[0].value, observables[1].value) },
        text,
        LOCALE_PROPERTY,
        observables[0],
        observables[1]
      )
      3 -> createStringBinding(
        { localizedText(text.value, observables[0].value, observables[1].value, observables[2].value) },
        text,
        LOCALE_PROPERTY,
        observables[0],
        observables[1],
        observables[2]
      )
      else -> createStringBinding(
        { localizedText(text.value, *Array(observables.size) { observables[it].value }) },
        *arrayOf<Observable>(text, LOCALE_PROPERTY) + observables
      )
    }

  fun String?.l(vararg args: Any?): StringBinding = when (this) {
    null -> createStringBinding({ null })
    else -> binding(this, *args)
  }
}
