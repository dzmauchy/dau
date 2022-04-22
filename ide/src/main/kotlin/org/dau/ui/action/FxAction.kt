package org.dau.ui.action

import javafx.beans.binding.Bindings.createObjectBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableStringValue
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.FXCollections.emptyObservableList
import javafx.collections.FXCollections.unmodifiableObservableList
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCombination
import org.dau.di.Ctx
import org.dau.ide.l10n.Localization
import org.dau.ui.icons.IconFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.type.MethodMetadata
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.stream.IntStream
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition as ABD

class FxAction {

  private val text = SimpleStringProperty()
  private val description = SimpleStringProperty()
  private val icon = SimpleStringProperty()
  private val key = SimpleObjectProperty<KeyCombination>()
  private val handler = SimpleObjectProperty<EventHandler<ActionEvent>>()
  private val disabled = SimpleBooleanProperty()

  var linkedObject: Any? = null

  private var selected: BooleanProperty? = null
  private var subItems: ObservableList<FxAction>? = null

  constructor()

  constructor(text: String?, vararg args: Any) {
    if (text != null) {
      this.text.bind(Localization.binding(text, *args))
    }
  }

  constructor(icon: String?, text: String, vararg args: Any) : this(text, *args) {
    if (icon != null) {
      this.icon.bind(SimpleStringProperty(icon))
    }
  }

  constructor(key: KeyCombination?, icon: String, text: String, vararg args: Any) : this(icon, text, *args) {
    if (key != null) {
      this.key.bind(SimpleObjectProperty(key))
    }
  }

  constructor(description: String?, icon: String, text: String, vararg args: Any) : this(icon, text, *args) {
    if (description != null) {
      this.description.bind(Localization.binding(description, *args))
    }
  }

  fun text(text: ObservableValue<String>, vararg args: ObservableValue<*>): FxAction {
    this.text.bind(Localization.binding(text, *args))
    return this
  }

  fun icon(icon: ObservableValue<String>): FxAction {
    this.text.bind(icon)
    return this
  }

  fun description(text: ObservableValue<String>, vararg args: ObservableValue<*>): FxAction {
    this.text.bind(Localization.binding(text, *args))
    return this
  }

  fun key(key: ObservableValue<KeyCombination>): FxAction {
    this.key.bind(key)
    return this
  }

  fun subItems(vararg actions: FxAction): FxAction {
    return subItems(FXCollections.observableArrayList(*actions))
  }

  fun subItems(actions: ObservableList<FxAction>): FxAction {
    this.subItems = actions
    return this
  }

  fun handle(handler: EventHandler<ActionEvent>): FxAction {
    this.handler.bind(SimpleObjectProperty(handler))
    return this
  }

  fun on(task: Runnable): FxAction {
    return handle { task.run() }
  }

  fun handler(handler: ObservableValue<EventHandler<ActionEvent>>): FxAction {
    this.handler.bind(handler)
    return this
  }

  fun selected(property: BooleanProperty): FxAction {
    this.selected = property
    return this
  }

  fun disabled(disabled: ObservableBooleanValue): FxAction {
    this.disabled.bind(disabled)
    return this
  }

  fun linkedObject(linkedObject: Any): FxAction {
    this.linkedObject = linkedObject
    return this
  }

  fun withSelected(consumer: Consumer<BooleanProperty>) {
    when (val s = selected) {
      null -> {}
      else -> consumer.accept(s)
    }
  }

  fun getSubItems(): ObservableList<FxAction> {
    return if (subItems == null) emptyObservableList() else unmodifiableObservableList(subItems!!)
  }

  companion object {

    val MENU_ICON_SIZE = 20
    val TOOLBAR_ICON_SIZE = 24

    @SafeVarargs
    private fun forEach(ctx: Ctx, vararg qualifiers: Class<out Annotation>): Consumer<BiConsumer<FxAction, ABD>> {
      return Consumer { e ->
        val beanNames = ctx.getBeanNamesForType(FxAction::class.java, false, true)
        MainLoop@ for (beanName in beanNames) {
          val definition = ctx.getBeanDefinition(beanName)
          if (definition is ABD) {
            val metadata = definition.factoryMethodMetadata ?: continue
            for (qualifier in qualifiers) {
              if (!metadata.isAnnotated(qualifier.name)) {
                continue@MainLoop
              }
            }
            val bean = ctx.getBean(beanName, FxAction::class.java)
            e.accept(bean, definition)
          }
        }
      }
    }

    private fun key(metadata: MethodMetadata, groupAnnotation: Class<out Annotation>): ActionKey {
      val data = metadata.getAnnotationAttributes(groupAnnotation.name)
      return if (data == null) {
        ActionKey("", 0)
      } else {
        val priority = data.getOrDefault("priority", 0) as Int
        val group = data.getOrDefault("name", "").toString()
        ActionKey(group, priority)
      }
    }

    @JvmStatic
    fun fillMenuBar(ctx: ApplicationContext, menuBar: MenuBar, qualifier: Class<out Annotation>) {
      val menuActions = TreeMap<ActionKey, TreeMap<ActionKey, ArrayList<FxAction>>>()
      forEach(ctx as Ctx, qualifier, MenuBarGroup::class.java).accept { action, definition ->
        val metadata = definition.factoryMethodMetadata!!
        val barKey = key(metadata, MenuBarGroup::class.java)
        val key = key(metadata, ActionGroup::class.java)
        menuActions.computeIfAbsent(barKey) { TreeMap() }.computeIfAbsent(key) { ArrayList() }.add(action)
      }
      menuActions.forEach { (barKey, groups) ->
        val menu = Menu()
        menu.textProperty().bind(Localization.binding(barKey.group))
        menuBar.menus.add(menu)
        val first = AtomicBoolean(true)
        groups.forEach { _, actions ->
          if (!first.compareAndSet(true, false)) {
            menu.items.add(SeparatorMenuItem())
          }
          actions.forEach { a -> menu.items.add(menuItem(a)) }
        }
      }
    }

    private fun iconBinding(icon: ObservableStringValue, size: Int): ObjectBinding<ImageView> {
      return createObjectBinding({ IconFactory.icon(icon.get(), size) }, icon)
    }

    private fun fillMenuItems(menuItems: ObservableList<MenuItem>, actions: ObservableList<FxAction>) {
      val map = IdentityHashMap<FxAction, MenuItem>(actions.size)
      for (a in actions) {
        val menuItem = menuItem(a)
        map[a] = menuItem
        menuItems.add(menuItem)
      }
      val lh = ListChangeListener<FxAction> { c ->
        while (c.next()) {
          if (c.wasRemoved()) {
            c.removed.forEach { a -> menuItems.remove(map.remove(a)) }
          }
          if (c.wasAdded()) {
            val newItems = c.addedSubList.stream().map { a ->
              val menuItem = menuItem(a)
              map[a] = menuItem
              menuItem
            }.toList()
            menuItems.addAll(c.from, newItems)
          }
          if (c.wasPermutated()) {
            val items = IntStream.range(c.from, c.to).map(c::getPermutation).mapToObj { menuItems[it] }.toList()
            menuItems.remove(c.from, c.to)
            menuItems.addAll(c.from, items)
          }
        }
      }
      actions.addListener(lh)
    }

    fun menuItem(action: FxAction): MenuItem {
      val subItems = action.subItems
      val menuItem = if (subItems == null) {
        val selected = action.selected
        if (selected == null) {
          MenuItem()
        } else {
          val checkMenuItem = CheckMenuItem()
          checkMenuItem.selectedProperty().bindBidirectional(selected)
          checkMenuItem
        }
      } else {
        val menu = Menu()
        fillMenuItems(menu.items, subItems)
        menu
      }
      menuItem.textProperty().bind(action.text)
      menuItem.acceleratorProperty().bind(action.key)
      menuItem.disableProperty().bind(action.disabled)
      menuItem.graphicProperty().bind(iconBinding(action.icon, MENU_ICON_SIZE))
      menuItem.setOnAction { ev ->
        val h = action.handler.get()
        h?.handle(ev)
      }
      return menuItem
    }

    @JvmStatic
    fun fillToolbar(context: ApplicationContext, toolBar: ToolBar, qualifier: Class<out Annotation>) {
      val toolbarActions = TreeMap<ActionKey, ArrayList<FxAction>>()
      forEach(context as Ctx, ToolbarAction::class.java, qualifier).accept { action, definition ->
        val metadata = definition.factoryMethodMetadata!!
        val key = key(metadata, ActionGroup::class.java)
        toolbarActions.computeIfAbsent(key) { ArrayList() }.add(action)
      }
      val first = AtomicBoolean(true)
      toolbarActions.forEach { _, actions ->
        if (!first.compareAndSet(true, false)) {
          toolBar.items.add(Separator(Orientation.HORIZONTAL))
        }
        for (action in actions) {
          toolBar.items.add(toolButton(action))
        }
      }
    }

    fun toolButton(action: FxAction): ButtonBase {
      val subItems = action.subItems
      val control = if (subItems == null) {
        val selected = action.selected
        if (selected == null) {
          Button()
        } else {
          ToggleButton().apply { selectedProperty().bind(selected) }
        }
      } else {
        MenuButton().apply { fillMenuItems(items, subItems) }
      }
      control.isFocusTraversable = false
      control.graphicProperty().bind(iconBinding(action.icon, TOOLBAR_ICON_SIZE))
      control.disableProperty().bind(action.disabled)
      control.setOnAction { action.handler.get()?.handle(it) }
      val text = action.text.get()
      if (text != null) {
        control.tooltip = Tooltip().apply { textProperty().bind(action.text) }
      }
      return control
    }
  }
}
