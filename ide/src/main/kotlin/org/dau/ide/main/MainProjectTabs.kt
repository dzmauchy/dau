package org.dau.ide.main

import javafx.beans.value.ChangeListener
import javafx.collections.SetChangeListener.Change
import javafx.geometry.Side
import javafx.scene.control.TabPane
import org.dau.di.Ctx
import org.dau.ide.project.ProjectConf
import org.dau.ide.project.ProjectTab
import org.dau.ui.schematic.model.FxProject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component
import java.util.function.Supplier

@Component
class MainProjectTabs : TabPane() {

  init {
    side = Side.BOTTOM
  }

  @Autowired
  fun initProjects(ctx: Ctx, projects: MainProjects, directories: MainDirectories) {
    projects.projects.addListener { c: Change<out FxProject> ->
      if (c.wasAdded()) {
        val project = c.elementAdded
        val newCtx = Ctx(ctx, project.name.get())
        val ih = ChangeListener<String> { _, _, nv -> newCtx.displayName = nv }
        project.name.addListener(ih)
        newCtx.registerBean(ProjectConf::class.java, Supplier { ProjectConf(project, directories) })
        newCtx.addApplicationListener(ApplicationListener { _: ContextClosedEvent -> project.name.removeListener(ih) })
        newCtx.refresh()
        newCtx.start()
      }
      if (c.wasRemoved()) {
        val project = c.elementRemoved
        tabs.removeIf { it is ProjectTab && it.project === project }
      }
    }
  }
}
