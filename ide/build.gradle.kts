import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  java
  kotlin("jvm").version(kotlinVersion)
  id("org.springframework.boot").version(springBootVersion)
}

tasks.getByName("jar", Jar::class) {
  manifest {
    attributes(
      "Main-Class" to "org.dau.ide.IdeLauncher",
      "Add-Exports" to "javafx.controls/com.sun.javafx.css",
      "Automatic-Module-Name" to "dau.ide"
    )
  }
}

tasks.getByName<BootJar>("bootJar") {
  layered {
    isEnabled = true
  }
}

tasks.getByName<BootRun>("bootRun") {
  isOptimizedLaunch = false
}

tasks.register<Exec>("standaloneRun") {
  group = "application"
  executable = "java"
  workingDir = projectDir.resolve("build").resolve("libs")
  args("-jar", "${project.name}-${project.version}.jar")
}

tasks.withType<KotlinCompile> {

  targetCompatibility = javaVersion.toString()
  sourceCompatibility = javaVersion.toString()

  kotlinOptions {
    jvmTarget = javaVersion.toString()
    javaParameters = true
  }
}

dependencies {
  implementation(group = "org.eclipse.jdt", name = "org.eclipse.jdt.core", version = "3.29.0")

  implementation(group = "org.kordamp.ikonli", name = "ikonli-javafx", version = ikonliVersion)
  for (lib in ikonliLibs) {
    implementation(group = "org.kordamp.ikonli", name = "ikonli-$lib-pack", version = ikonliVersion)
  }
}