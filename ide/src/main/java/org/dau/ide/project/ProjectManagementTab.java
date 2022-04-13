package org.dau.ide.project;

import javafx.scene.control.Tab;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.kordamp.ikonli.material.Material;
import org.springframework.stereotype.Component;

@Component
public class ProjectManagementTab extends Tab {

  public ProjectManagementTab(ProjectManagementPane settingsPane) {
    setGraphic(IconFactory.icon(Material.SETTINGS, 20));
    textProperty().bind(Localization.INSTANCE.binding("Management"));
    setClosable(false);
    setContent(settingsPane);
  }
}
