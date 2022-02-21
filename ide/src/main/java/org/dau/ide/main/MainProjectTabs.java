package org.dau.ide.main;

import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener.Change;
import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import org.dau.di.Ctx;
import org.dau.ide.action.FxAction;
import org.dau.ide.project.ProjectConf;
import org.dau.ide.project.ProjectTab;
import org.dau.ui.schematic.fx.model.FxProject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import static javafx.application.Platform.runLater;

@Component
public final class MainProjectTabs extends TabPane {

  public MainProjectTabs() {
    setSide(Side.BOTTOM);
  }

  @Autowired
  public void initProjects(Ctx ctx, MainProjects projects, MainDirectories directories) {
    projects.projects.addListener((Change<? extends FxProject> c) -> {
      if (c.wasAdded()) {
        var project = c.getElementAdded();
        var newCtx = new Ctx(ctx, project.name.get());
        var ih = (ChangeListener<String>) (o, ov, nv) -> newCtx.setDisplayName(nv);
        project.name.addListener(ih);
        newCtx.registerBean(ProjectConf.class, () -> new ProjectConf(project, directories));
        newCtx.addApplicationListener((ContextClosedEvent ev) -> project.name.removeListener(ih));
        newCtx.refresh();
        newCtx.start();
      }
      if (c.wasRemoved()) {
        var project = c.getElementRemoved();
        getTabs().removeIf(tab -> tab instanceof ProjectTab t && t.project == project);
      }
    });
  }

  @Autowired
  public void initActions(@MainQualifier ObjectFactory<FxAction> projectListAction) {
    getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> runLater(() -> {
      var action = projectListAction.getObject();
      if (nv instanceof ProjectTab t) {
        for (var a : action.getSubItems()) {
          if (a.linkedObject == t.project) {
            a.withSelected(p -> p.set(true));
          } else {
            a.withSelected(p -> p.set(false));
          }
        }
      }
    }));
  }
}
