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

  configure<JavaPluginExtension> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
  }

  tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
  }

  dependencies {
    "testImplementation"(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    "testRuntimeOnly"(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)
  }

  tasks.getByName<Test>("test") {
    useJUnitPlatform()
  }

  when (name) {
    "ide" -> {
      dependencies {
        "implementation"(project(":di"))
        for (lib in javafxLibs) {
          "implementation"(group = "org.openjfx", name = lib, version = javafxVersion)
          "implementation"(group = "org.openjfx", name = lib, version = javafxVersion, classifier = javafxClassifier)
        }
        "testImplementation"(group = "org.springframework", name = "spring-test", version = springVersion)
      }
    }
  }
}