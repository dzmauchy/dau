package org.dau.ide.main;

import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener.Change;
import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import org.dau.di.Ctx;
import org.dau.ide.project.ProjectConf;
import org.dau.ide.project.ProjectTab;
import org.dau.ui.schematic.model.FxProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

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
        var newCtx = new Ctx(ctx, project.getName().get());
        var ih = (ChangeListener<String>) (o, ov, nv) -> newCtx.setDisplayName(nv);
        project.getName().addListener(ih);
        newCtx.registerBean(ProjectConf.class, () -> new ProjectConf(project, directories));
        newCtx.addApplicationListener((ContextClosedEvent ev) -> project.getName().removeListener(ih));
        newCtx.refresh();
        newCtx.start();
      }
      if (c.wasRemoved()) {
        var project = c.getElementRemoved();
        getTabs().removeIf(tab -> tab instanceof ProjectTab t && t.project == project);
      }
    });
  }
}
