package org.dau.ide.main;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.scene.control.Tab;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.dau.ide.action.ActionGroup;
import org.dau.ide.action.FxAction;
import org.dau.ide.l10n.Localization;
import org.dau.ide.main.dialogs.ProjectsDialog;
import org.dau.ide.main.menu.ProjectGroup;
import org.dau.ide.main.menu.ViewGroup;
import org.dau.ide.project.ProjectTab;
import org.dau.ui.schematic.fx.model.FxProject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

import static javafx.beans.binding.Bindings.createBooleanBinding;

@Component
public class MainActions {

  @MainBean
  @ViewGroup
  public FxAction selectThemeAction() {
    return new FxAction("Select theme").on(ev -> {

    });
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "project")
  public FxAction createProjectAction(MainProjects projects) {
    return new FxAction("icons/project.png", "Create a new project")
      .on(() -> projects.projects.add(new FxProject()));
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "project-load")
  public FxAction loadProject(ObjectFactory<ProjectsDialog> dialog, MainProjects projects, MainProjectTabs tabs) {
    return new FxAction("icons/load.png", "Load a project")
      .on(() -> dialog.getObject().showAndWait().ifPresent(paths -> {
        for (var path : paths) {
          projects.projects.add(FxProject.load(path));
        }
        if (!paths.isEmpty()) {
          tabs.getSelectionModel().selectLast();
        }
      }));
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "project-load")
  public FxAction importProject(MainDirectories directories, @MainQualifier Stage stage, MainProjects projects) {
    return new FxAction("icons/import.png", "Import a project")
      .on(() -> {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(directories.homeDir.toFile());
        directoryChooser.titleProperty().bind(Localization.binding("Choose a directory to import"));
        var dir = directoryChooser.showDialog(stage);
        if (dir != null) {
          projects.projects.add(FxProject.load(dir.toPath()));
        }
      });
  }

  @MainBean
  @ProjectGroup
  @ActionGroup(name = "projects")
  public FxAction projectListAction(MainProjects projects, MainProjectTabs tabs) {
    var list = FXCollections.<FxAction>observableArrayList();
    var tabSelectListener = (ChangeListener<Tab>) (o, ov, nv) -> {
      if (nv instanceof ProjectTab t) {
        for (var a : list) {
          if (a.linkedObject == t.project) {
            a.withSelected(p -> p.set(true));
          } else {
            a.withSelected(p -> p.set(false));
          }
        }
      }
    };
    tabs.getSelectionModel().selectedItemProperty().addListener(tabSelectListener);
    projects.projects.addListener((SetChangeListener<FxProject>) c -> {
      if (c.wasRemoved()) {
        list.removeIf(a -> a.linkedObject == c.getElementRemoved());
      }
      if (c.wasAdded()) {
        var p = c.getElementAdded();
        var sel = new SimpleBooleanProperty();
        sel.addListener((o, ov, nv) -> {
          if (nv) {
            tabs.getTabs().forEach(tab -> {
              if (tab instanceof ProjectTab t && t.project == p) {
                tabs.getSelectionModel().selectedItemProperty().removeListener(tabSelectListener);
                tabs.getSelectionModel().select(tab);
                tabs.getSelectionModel().selectedItemProperty().addListener(tabSelectListener);
              }
            });
          }
        });
        var a = new FxAction()
          .icon(new SimpleStringProperty("icons/project.png"))
          .text(p.name)
          .selected(sel)
          .linkedObject(p);
        list.add(a);
        list.sort((a1, a2) -> {
          var p1 = (FxProject) a1.linkedObject;
          var p2 = (FxProject) a2.linkedObject;
          return p1.name.get().compareTo(p2.name.get());
        });
      }
    });
    return new FxAction("icons/projects.png", "Projects").subItems(list);
  }
}
