package org.dau.ide.main.dialogs

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.dau.di.PrototypeComponent
import org.dau.ide.l10n.Localization
import org.dau.ide.main.MainDirectories
import org.dau.ide.main.MainQualifier
import org.dau.ui.schematic.model.FxProject
import org.dau.ui.utils.TableColumnBuilder
import java.nio.file.Files
import java.nio.file.Path

@PrototypeComponent
class ProjectsDialog(@MainQualifier stage: Stage, dirs: MainDirectories) : Dialog<ObservableList<Path>>() {

  private val table = TableView<Path>()

  init {
    initOwner(stage)
    initModality(Modality.WINDOW_MODAL)
    initStyle(StageStyle.UTILITY)
    dialogPane.buttonTypes.setAll(ButtonType.APPLY, ButtonType.CANCEL)
    initTable(dirs)
    dialogPane.content = table
    titleProperty().bind(Localization.binding("Projects"))
    setResultConverter { param ->
      when (param.buttonData) {
        ButtonBar.ButtonData.APPLY -> table.selectionModel.selectedItems;
        ButtonBar.ButtonData.CANCEL_CLOSE -> null;
        else -> throw IllegalStateException()
      }
    }
    isResizable = false
  }

  fun initTable(directories: MainDirectories) {
    table.selectionModel.selectionMode = SelectionMode.MULTIPLE
    table.columns.add(
      TableColumnBuilder<Path, String>("Id")
        .value { SimpleStringProperty(it.value.fileName.toString()) }
        .width(100).build())
    table.columns.add(
      TableColumnBuilder<Path, String>("Name")
        .value { SimpleStringProperty(FxProject.load(it.value).name.get()) }
        .width(300).build())
    Files.newDirectoryStream(directories.homeDir).use { ds ->
      for (path in ds) {
        if (FxProject.isProject(path)) {
          table.items.add(path)
        }
      }
    }
  }
}
