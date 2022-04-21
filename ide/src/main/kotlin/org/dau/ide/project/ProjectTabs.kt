package org.dau.ide.project

import javafx.collections.SetChangeListener
import javafx.scene.control.TabPane
import org.dau.ide.schema.SchemaTab
import org.dau.ui.schematic.model.FxProject
import org.dau.ui.schematic.model.FxSchema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ProjectTabs(settingsTab: ProjectManagementTab) : TabPane(settingsTab) {

  @Autowired
  fun initSchemas(project: FxProject) {
    project.schemas.addListener { c: SetChangeListener.Change<out FxSchema> ->
      if (c.wasRemoved()) {
        val schema = c.elementRemoved
        tabs.removeIf { it is SchemaTab && it.schema === schema }
      }
    }
  }
}
