package org.dau.ide.project.management;

import javafx.scene.control.TitledPane;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.kordamp.ikonli.zondicons.Zondicons;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Qualifier("management")
public class LibrariesManagementPane extends TitledPane {

  public LibrariesManagementPane() {
    textProperty().bind(Localization.INSTANCE.binding("Libraries"));
    setGraphic(IconFactory.icon(Zondicons.LIBRARY, 16));
  }
}
