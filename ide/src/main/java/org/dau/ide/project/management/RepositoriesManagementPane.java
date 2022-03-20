package org.dau.ide.project.management;

import javafx.scene.control.TitledPane;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.kordamp.ikonli.remixicon.RemixiconAL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Qualifier("management")
public class RepositoriesManagementPane extends TitledPane {

  public RepositoriesManagementPane() {
    textProperty().bind(Localization.binding("Repositories"));
    setGraphic(IconFactory.icon(RemixiconAL.GIT_REPOSITORY_FILL, 16));
  }
}
