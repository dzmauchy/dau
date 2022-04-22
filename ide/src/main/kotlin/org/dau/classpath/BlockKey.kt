package org.dau.classpath

@JvmRecord
data class BlockKey(val className: String, val name: String) {
  override fun toString(): String {
    return if (name == className) "$className.init" else "$className.$name"
  }
}
