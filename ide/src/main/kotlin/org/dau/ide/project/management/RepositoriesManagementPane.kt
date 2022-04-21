package org.dau.ide.project.management

import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.SetChangeListener
import javafx.collections.SetChangeListener.Change
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import org.dau.di.Init
import org.dau.ide.l10n.Localization
import org.dau.ide.l10n.Localization.l
import org.dau.ui.icons.IconFactory
import org.dau.ui.schematic.model.FxProject
import org.kordamp.ikonli.ionicons4.Ionicons4IOS
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.metrizeicons.MetrizeIcons
import org.kordamp.ikonli.remixicon.RemixiconAL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.net.URI

@Component
@Order(2)
@Qualifier("management")
class RepositoriesManagementPane(private val project: FxProject) : TitledPane() {

  private val repositories: ObservableList<URI>
  private val changeListener = SetChangeListener<URI> { onUpdateRepositories(it) }
  private val listView: ListView<URI>
  private val toolBar = ToolBar()

  init {
    textProperty().bind(Localization.binding("Repositories"))
    graphic = IconFactory.icon(RemixiconAL.GIT_REPOSITORY_FILL, 16)
    this.repositories = FXCollections.observableArrayList(project.repositories)
    this.project.repositories.addListener(changeListener)
    this.listView = ListView(repositories)
    this.listView.setCellFactory {
      object : ListCell<URI>() {
        override fun updateItem(item: URI?, empty: Boolean) {
          super.updateItem(item, empty)
          if (item != null) {
            text = item.toString()
            graphic = IconFactory.icon(MetrizeIcons.MET_LINK_URL, 18)
          }
        }
      }
    }
    content = BorderPane(listView, toolBar, null, null, null)
  }

  @EventListener
  fun onClose(event: ContextClosedEvent) {
    assert(event.source != null)
    project.repositories.removeListener(changeListener)
  }

  private fun onUpdateRepositories(change: Change<out URI>) {
    if (change.wasRemoved()) {
      repositories.remove(change.elementRemoved)
    }
    if (change.wasAdded()) {
      repositories.add(change.elementAdded)
    }
  }

  @Autowired
  fun initToolbar(init: Init) = init.schedule(this) {
    toolBar.items.addAll(
      Button(null, IconFactory.icon(Ionicons4IOS.ADD_CIRCLE_OUTLINE, 20)).apply {
        tooltip = Tooltip("URL")
        setOnAction {
          val dlg = TextInputDialog()
          dlg.initModality(Modality.APPLICATION_MODAL)
          dlg.initOwner(scene.window)
          dlg.title = "URL"
          dlg.contentText = "URL: "
          dlg.dialogPane.prefWidth = 800.0
          dlg.headerTextProperty().bind(Localization.binding("Repository URL"))
          dlg.showAndWait().ifPresent { url -> project.repositories.add(URI.create(url)) }
        }
      },
      Separator(Orientation.VERTICAL),
      Button(null, IconFactory.icon(Ionicons4IOS.REMOVE, 20)).apply {
        tooltip = Tooltip().apply { textProperty().bind("Remove".l()) }
        disableProperty().bind(listView.selectionModel.selectedItemProperty().isNull)
        setOnAction { project.repositories.remove(listView.selectionModel.selectedItem) }
      },
      Button(null, IconFactory.icon(Material.CLEAR, 20)).apply {
        tooltip = Tooltip().apply { textProperty().bind("Clear".l()) }
        disableProperty().bind(Bindings.isEmpty(listView.items))
        setOnAction { project.repositories.clear() }
      }
    )
  }
}
