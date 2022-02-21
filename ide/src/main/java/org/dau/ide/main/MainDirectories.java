package org.dau.ide.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public final class MainDirectories {

  public final Path homeDir = Path.of(System.getProperty("user.home"), "dau");

  @Autowired
  public void init() throws Exception {
    Files.createDirectories(homeDir);
  }
}
