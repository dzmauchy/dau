package org.dau.ide.main

import javafx.collections.FXCollections.observableSet
import javafx.collections.SetChangeListener
import javafx.collections.SetChangeListener.Change
import org.dau.ui.schematic.model.FxProject
import org.dau.ui.schematic.model.FxSchema
import org.springframework.stereotype.Component
import java.nio.file.Files

@Component
class MainProjects(private val directories: MainDirectories) {

  private val schemaListeners = HashMap<FxProject, SetChangeListener<FxSchema>>()
  val projects = observableSet<FxProject>().apply { addListener(SetChangeListener { onChangeProjects(it) }) }

  private fun onChangeProjects(change: Change<out FxProject>) {
    if (change.wasAdded()) {
      val project = change.elementAdded
      val l = SetChangeListener<FxSchema> { onChangeSchema(project, it) }
      project.schemas.addListener(l)
      schemaListeners[project] = l
    }
    if (change.wasRemoved()) {
      val project = change.elementRemoved
      val listener = schemaListeners.remove(project)
      project.schemas.removeListener(listener)
    }
  }

  private fun onChangeSchema(project: FxProject, change: Change<out FxSchema>) {
    val dir = directories.homeDir.resolve(project.id)
    if (change.wasRemoved()) {
      val schema = change.elementRemoved
      val file = dir.resolve(schema.toFileName())
      if (Files.isRegularFile(file)) {
        check(file.toFile().delete()) { "Could not delete $file" }
      }
    }
  }
}
