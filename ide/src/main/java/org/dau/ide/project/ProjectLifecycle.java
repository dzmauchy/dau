package org.dau.ide.project;

import org.dau.ide.main.MainDirectories;
import org.dau.ide.main.MainProjects;
import org.dau.ui.schematic.fx.model.FxProject;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public final class ProjectLifecycle {

  private final MainProjects projects;
  private final FxProject project;
  private final MainDirectories directories;

  public ProjectLifecycle(MainProjects projects, FxProject project, MainDirectories directories) {
    this.projects = projects;
    this.project = project;
    this.directories = directories;
  }

  @EventListener
  public void onClose(ContextClosedEvent event) {
    try {
      project.save(directories.homeDir.resolve(project.id));
    } finally {
      projects.projects.remove(project);
    }
  }
}
