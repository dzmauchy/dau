package org.dau.ide.project.management

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.SetChangeListener
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.util.converter.DefaultStringConverter
import org.dau.di.Init
import org.dau.ide.l10n.Localization
import org.dau.ide.project.ProjectSchemas
import org.dau.ui.icons.IconFactory
import org.dau.ui.schematic.model.FxProject
import org.dau.ui.schematic.model.FxSchema
import org.dau.ui.utils.TableColumnBuilder
import org.kordamp.ikonli.ionicons4.Ionicons4IOS
import org.kordamp.ikonli.material.Material
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
@Qualifier("management")
@Suppress("UNUSED_PARAMETER")
class SchemasManagementPane(
    private val project: FxProject,
    private val projectSchemas: ProjectSchemas
) : TitledPane() {

  private val schemaSetChangeListener = SetChangeListener<FxSchema> { this.onUpdateSchemas(it) }
  private val schemas: ObservableList<FxSchema>
  private val table: TableView<FxSchema>
  private val toolBar = ToolBar()

  init {
    textProperty().bind(Localization.binding("Schemas"))
    graphic = IconFactory.icon(Ionicons4IOS.LIST, 16)
    this.project.schemas.addListener(schemaSetChangeListener)
    this.schemas = FXCollections.observableArrayList(project.schemas)
    this.table = TableView(schemas)
    this.table.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
    this.table.columns.add(TableColumnBuilder<FxSchema, String>("Id")
      .width(128)
      .value { SimpleStringProperty(it?.value?.id) }
      .build()
    )
    this.table.columns.add(TableColumnBuilder<FxSchema, String>("Name")
      .width(400)
      .value { it?.value?.name }
      .with { it?.setSortType(TableColumn.SortType.ASCENDING) }
      .with { it?.setEditable(true) }
      .cell { TextFieldTableCell(DefaultStringConverter()) }
      .build()
    )
    this.table.columns.add(TableColumnBuilder<FxSchema, Number>("Blocks")
      .width(128)
      .value { it?.value?.blockCount() }
      .with { it?.setEditable(false) }
      .cell {
        val cell = TextFieldTableCell<FxSchema, Number>()
        cell.alignment = Pos.BASELINE_RIGHT
        cell.isEditable = false
        cell
      }
      .build()
    )
    this.table.columns.add(TableColumnBuilder<FxSchema, HBox>("Actions")
      .width(400)
      .value { f ->
        val box = HBox(
          Button(null, IconFactory.icon(Material.OPEN_IN_NEW, 16)).apply {
            setOnAction { projectSchemas.addSchema(f!!.value) }
          }
        )
        box.alignment = Pos.CENTER
        SimpleObjectProperty(box)
      }
      .build()
    )
    this.table.isEditable = true
    this.table.sortOrder.add(table.columns[1])
    content = BorderPane(table, toolBar, null, null, null)
  }

  private fun onUpdateSchemas(change: SetChangeListener.Change<out FxSchema>) {
    if (change.wasRemoved()) {
      val e = change.elementRemoved
      schemas.removeIf { it.id == e.id }
    }
    if (change.wasAdded()) {
      val e = change.elementAdded
      schemas.add(e)
    }
  }

  @EventListener
  fun onClose(ev: ContextClosedEvent) {
    project.schemas.removeListener(schemaSetChangeListener)
  }

  @Autowired
  fun initToolbar(init: Init) = init.schedule(this) {
    toolBar.items.addAll(
      Button(null, IconFactory.icon(Ionicons4IOS.ADD, 20)).apply {
        setOnAction {
          val schema = FxSchema()
          schema.name.set("New schema")
          project.schemas.add(schema)
        }
      },
      Button(null, IconFactory.icon(Ionicons4IOS.REMOVE, 20)).apply {
        setOnAction {
          val item = table.selectionModel.selectedItem
          project.schemas.remove(item)
        }
        disableProperty().bind(table.selectionModel.selectedItemProperty().isNull)
      },
      Separator(Orientation.VERTICAL),
      Button(null, IconFactory.icon(Material.SORT, 20)).apply {
        setOnAction { table.sort() }
      },
      Separator(Orientation.VERTICAL),
      Button(null, IconFactory.icon(Material.OPEN_IN_NEW, 20)).apply {
        setOnAction { projectSchemas.addSchema(table.selectionModel.selectedItem) }
        disableProperty().bind(table.selectionModel.selectedItemProperty().isNull)
      }
    )
  }
}
