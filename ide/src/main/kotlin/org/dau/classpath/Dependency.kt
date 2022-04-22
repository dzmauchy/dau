package org.dau.classpath

@JvmRecord
data class Dependency(val group: String, val name: String, val version: String) {
  override fun toString(): String = "$group:$name:$version"
}
