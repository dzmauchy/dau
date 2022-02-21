package org.dau.ide.project;

import javafx.scene.control.Tab;
import org.dau.di.Ctx;
import org.dau.ide.main.MainProjectTabs;
import org.dau.ui.icons.IconFactory;
import org.dau.ui.schematic.fx.model.FxProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectTab extends Tab {

  public final FxProject project;

  public ProjectTab(FxProject project, SchemaTabs tabPane, Ctx ctx) {
    this.project = project;
    setGraphic(IconFactory.icon("icons/project.png", 20));
    textProperty().bind(project.name);
    setContent(tabPane);
    setOnCloseRequest(ev -> ctx.close());
  }

  @Autowired
  public void initWith(MainProjectTabs tabs) {
    tabs.getTabs().add(this);
  }

  @EventListener
  public void onClose(ContextClosedEvent event) {
    var tabPane = getTabPane();
    if (tabPane != null) {
      tabPane.getTabs().remove(this);
    }
  }
}
