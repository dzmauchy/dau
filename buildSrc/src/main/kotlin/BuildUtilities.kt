import org.gradle.api.JavaVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

val junitVersion = "5.8.2"
val javafxVersion = "18"

val javafxLibs = listOf(
  "javafx-base",
  "javafx-controls",
  "javafx-media",
  "javafx-graphics"
)

val os = DefaultNativePlatform.getCurrentOperatingSystem()!!
val arch = DefaultNativePlatform.getCurrentArchitecture()!!

val javafxOs = when {
  os.isLinux -> "linux"
  os.isMacOsX -> "mac"
  os.isWindows -> "win"
  else -> throw IllegalStateException("Unknown os type: ${os.name}")
}

val javafxClassifier = when {
  arch.isArm -> "$javafxOs-aarch64"
  arch.isAmd64 -> javafxOs
  arch.isI386 -> "$javafxOs-x86"
  else -> throw IllegalStateException("Unsupported arch: ${arch.name} on ${os.name}")
}

val javaVersion = JavaVersion.VERSION_17
val kotlinVersion = "1.6.20"

val springBootVersion = "2.6.4"
val springVersion = "5.3.19"
val ikonliVersion = "12.3.0"
val log4jVersion = "2.17.1"
val slf4jVersion = "1.7.36"

val ikonliLibs = listOf(
  "materialdesign",
  "material",
  "fontawesome5",
  "weathericons",
  "themify",
  "lineawesome",
  "linecons",
  "metrizeicons",
  "simpleicons",
  "maki",
  "boxicons",
  "microns",
  "ionicons4",
  "bpmn",
  "elusive",
  "remixicon",
  "fluentui",
  "octicons",
  "zondicons",
  "dashicons",
  "prestashopicons",
  "foundation",
  "fileicons",
  "entypo",
  "hawcons",
  "medicons",
  "icomoon",
  "bootstrapicons",
  "material2"
)