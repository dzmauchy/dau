package org.dau.ide.schema

import org.dau.ui.action.FxAction
import org.dau.ui.action.ToolbarAction
import org.dau.ide.project.ProjectConf
import org.springframework.stereotype.Component

@Component
class SchemaActions {

  @SchemaBean
  @ToolbarAction
  fun saveAction(conf: SchemaConf, projectConf: ProjectConf): FxAction {
    return FxAction("icons/save.png", "Save")
      .on {
        val file = projectConf.directory.resolve(conf.schema.toFileName())
        conf.schema.save(file)
      }
  }
}
