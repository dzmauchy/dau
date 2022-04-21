package org.dau.ide.project

import javafx.scene.control.Tab
import org.dau.ide.l10n.Localization
import org.dau.ui.icons.IconFactory
import org.kordamp.ikonli.material.Material
import org.springframework.stereotype.Component

@Component
class ProjectManagementTab(settingsPane: ProjectManagementPane) : Tab() {
  init {
    graphic = IconFactory.icon(Material.SETTINGS, 20)
    textProperty().bind(Localization.binding("Management"))
    isClosable = false
    content = settingsPane
  }
}
