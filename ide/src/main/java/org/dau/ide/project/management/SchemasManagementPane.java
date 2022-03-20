package org.dau.ide.project.management;

import javafx.scene.control.TitledPane;
import org.dau.ide.l10n.Localization;
import org.dau.ui.icons.IconFactory;
import org.kordamp.ikonli.ionicons4.Ionicons4IOS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Qualifier("management")
public class SchemasManagementPane extends TitledPane {

  public SchemasManagementPane() {
    textProperty().bind(Localization.binding("Schemas"));
    setGraphic(IconFactory.icon(Ionicons4IOS.LIST, 16));
  }
}
