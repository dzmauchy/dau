package org.dau.ide.main

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.SetChangeListener
import javafx.scene.control.Tab
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import org.dau.ide.action.ActionGroup
import org.dau.ide.action.FxAction
import org.dau.ide.l10n.Localization
import org.dau.ide.main.dialogs.ProjectsDialog
import org.dau.ide.main.menu.ProjectGroup
import org.dau.ide.main.menu.ViewGroup
import org.dau.ide.project.ProjectTab
import org.dau.ui.schematic.model.FxProject
import org.springframework.beans.factory.ObjectFactory
import org.springframework.stereotype.Component

@Component
class MainActions {

  @MainBean
  @ViewGroup
  fun selectThemeAction(): FxAction {
    return FxAction("Select theme").on {

    }
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "project")
  fun createProjectAction(projects: MainProjects): FxAction {
    return FxAction("icons/project.png", "Create a new project").on { projects.projects.add(FxProject()) }
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "project-load")
  fun loadProject(dialog: ObjectFactory<ProjectsDialog>, projects: MainProjects, tabs: MainProjectTabs): FxAction {
    return FxAction("icons/load.png", "Load a project").on {
      dialog.getObject().showAndWait().ifPresent { paths ->
        for (path in paths) {
          projects.projects.add(FxProject.load(path))
        }
        if (!paths.isEmpty()) {
          tabs.selectionModel.selectLast()
        }
      }
    }
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "project-load")
  fun importProject(directories: MainDirectories, @MainQualifier stage: Stage, projects: MainProjects): FxAction {
    return FxAction("icons/import.png", "Import a project").on {
      val directoryChooser = DirectoryChooser()
      directoryChooser.initialDirectory = directories.homeDir.toFile()
      directoryChooser.titleProperty().bind(Localization.binding("Choose a directory to import"))
      val dir = directoryChooser.showDialog(stage)
      if (dir != null) {
        projects.projects.add(FxProject.load(dir.toPath()))
      }
    }
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "projects")
  fun projectListAction(projects: MainProjects, tabs: MainProjectTabs): FxAction {
    val list = FXCollections.observableArrayList<FxAction>()
    val tabSelectListener = ChangeListener<Tab> { _, _, nv ->
      if (nv is ProjectTab) {
        for (a in list) {
          if (a.linkedObject === nv.project) {
            a.withSelected { p -> p.set(true) }
          } else {
            a.withSelected { p -> p.set(false) }
          }
        }
      }
    }
    tabs.selectionModel.selectedItemProperty().addListener(tabSelectListener)
    projects.projects.addListener(SetChangeListener { c ->
      if (c.wasRemoved()) {
        list.removeIf { a -> a.linkedObject === c.elementRemoved }
      }
      if (c.wasAdded()) {
        val p = c.elementAdded
        val sel = SimpleBooleanProperty()
        sel.addListener { _, _, nv ->
          if (nv!!) {
            tabs.tabs.forEach { tab ->
              if (tab is ProjectTab && tab.project === p) {
                tabs.selectionModel.selectedItemProperty().removeListener(tabSelectListener)
                tabs.selectionModel.select(tab)
                tabs.selectionModel.selectedItemProperty().addListener(tabSelectListener)
              }
            }
          }
        }
        val a = FxAction()
          .icon(SimpleStringProperty("icons/project.png"))
          .text(p.name)
          .selected(sel)
          .linkedObject(p)
        list.add(a)
        list.sortWith { a1, a2 ->
          val p1 = a1.linkedObject as FxProject
          val p2 = a2.linkedObject as FxProject
          p1.name.get().compareTo(p2.name.get())
        }
      }
    })
    return FxAction("icons/projects.png", "Projects").subItems(list)
  }
}
