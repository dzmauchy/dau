package org.dau.ide.main

import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

@Component
class MainDirectories {

  val homeDir = Path.of(System.getProperty("user.home"), "dau").also(Files::createDirectories)
}
