package org.dau.ide.project;

import org.dau.ide.main.MainDirectories;
import org.dau.ui.schematic.fx.model.FxProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration(proxyBeanMethods = false)
@ComponentScan
public class ProjectConf {

  public final FxProject project;
  public final Path directory;

  public ProjectConf(@Autowired(required = false) FxProject project, MainDirectories directories) {
    this.project = project;
    try {
      this.directory = directories.homeDir.resolve(project.id);
      Files.createDirectories(directory);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Bean
  public FxProject project() {
    return project;
  }
}
