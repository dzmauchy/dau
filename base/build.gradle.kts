plugins {
  `maven-publish`
}

java {
  withJavadocJar()
  withSourcesJar()
}

tasks.withType<Javadoc> {
  options {
    quiet()
    when (val o = this) {
      is StandardJavadocDocletOptions -> {
        o.noComment()
        o.addStringOption("Xdoclint:none", "-quiet")
      }
    }
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      groupId = "org.dauch"
      artifactId = "dauch-base"
      version = "0.1"

      from(components["java"])

      pom {
        name.set("dauch-base")
        description.set("Base library")
        url.set("org.dauch")
        licenses {
          license {
            name.set("AGPLv3")
            url.set("https://www.gnu.org/licenses/agpl-3.0.en.html")
          }
        }
        developers {
          developer {
            id.set("dzauchy")
            name.set("Dzmiter Auchynnikau")
            email.set("dzmiter.auchynnikau@gmail.com")
          }
        }
        scm {
          connection.set("scm:git:https://github.com/dzmauchy/dau.git")
          developerConnection.set("scm:git:https://github.com/dzmauchy/dau.git")
          url.set("https://github.com/dzmauchy/dau")
        }
      }
    }
  }
}