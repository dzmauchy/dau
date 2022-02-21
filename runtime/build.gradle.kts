plugins {
  `java-library`
  id("org.springframework.boot").version(springBootVersion)
}

tasks.getByName("jar", org.gradle.jvm.tasks.Jar::class) {
  manifest {
    attributes(
      "Main-Class" to "org.dau.runtime.runner.Runner",
      "Automatic-Module-Name" to "dau.runtime"
    )
  }
}

dependencies {
  api(group = "org.slf4j", name = "slf4j-api", version = slf4jVersion)
  api(group = "org.apache.logging.log4j", name = "log4j-api", version = log4jVersion)

  implementation(group = "org.apache.logging.log4j", name = "log4j-1.2-api", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-to-slf4j", version = log4jVersion)
}