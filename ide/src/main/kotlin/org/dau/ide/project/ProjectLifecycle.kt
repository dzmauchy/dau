package org.dau.ide.project

import org.dau.ide.main.MainDirectories
import org.dau.ide.main.MainProjects
import org.dau.ui.schematic.model.FxProject
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProjectLifecycle(
  private val projects: MainProjects,
  private val project: FxProject,
  private val directories: MainDirectories
) {

  @EventListener
  @Suppress("UNUSED_PARAMETER")
  fun onClose(ev: ContextClosedEvent) {
    try {
      project.save(directories.homeDir.resolve(project.id))
    } finally {
      projects.projects.remove(project)
    }
  }
}
