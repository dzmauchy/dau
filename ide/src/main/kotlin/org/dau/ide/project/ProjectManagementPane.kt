package org.dau.ide.project

import javafx.scene.control.Accordion
import javafx.scene.control.TitledPane
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class ProjectManagementPane(@Qualifier("management") panes: Array<TitledPane>) : Accordion(*panes) {
  init {
    expandedPane = panes[0]
  }
}
