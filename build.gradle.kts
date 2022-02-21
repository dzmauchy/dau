group = "org.dau"
version = "1.0-SNAPSHOT"

subprojects {
  apply(plugin = "java")
  apply(plugin = "java-library")

  group = parent?.group!!
  version = parent?.version!!

  repositories {
    mavenCentral()
  }

  configurations.all {
    resolutionStrategy.eachDependency {
      if (requested.group == "org.apache.commons" && requested.name == "commons-lang3") {
        useVersion("3.12.0")
      }
      if (requested.group == "org.apache.httpcomponents" && requested.name == "httpcore") {
        useVersion("4.4.15")
      }
    }
  }

  configure<JavaPluginExtension> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
  }

  tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
  }

  dependencies {
    "implementation"(group = "org.slf4j", name = "slf4j-jdk14", version = slf4jVersion)
    "testImplementation"(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    "testRuntimeOnly"(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)
  }

  tasks.getByName<Test>("test") {
    useJUnitPlatform()
  }

  when (name) {
    "base" -> {
      dependencies {
        "compileOnly"(project(":runtime"))
      }
    }
    "ide" -> {
      dependencies {
        "implementation"(project(":di"))
        "implementation"(project(":runtime"))

        for (lib in javafxLibs) {
          "implementation"(group = "org.openjfx", name = lib, version = javafxVersion)
          "implementation"(group = "org.openjfx", name = lib, version = javafxVersion, classifier = javafxClassifier)
        }
        "testImplementation"(group = "org.springframework", name = "spring-test", version = springVersion)
      }
    }
  }
}