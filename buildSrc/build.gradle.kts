import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

val jvmVersion = "17"

tasks.withType<JavaCompile> {
  sourceCompatibility = jvmVersion
  targetCompatibility = jvmVersion
}

tasks.named("compileKotlin", KotlinCompile::class) {
  sourceCompatibility = jvmVersion
  targetCompatibility = jvmVersion
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  testImplementation(kotlin("test-junit5"))
}