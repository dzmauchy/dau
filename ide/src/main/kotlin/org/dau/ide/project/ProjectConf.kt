package org.dau.ide.project

import org.dau.ide.main.MainDirectories
import org.dau.ui.schematic.model.FxProject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path

@Configuration(proxyBeanMethods = false)
@ComponentScan
open class ProjectConf(@param:Autowired(required = false) val project: FxProject, directories: MainDirectories) {

  val directory: Path = try {
    directories.homeDir.resolve(project.id).also(Files::createDirectories)
  } catch (e: IOException) {
    throw UncheckedIOException(e)
  }

  @Bean
  open fun project(): FxProject {
    return project
  }
}
