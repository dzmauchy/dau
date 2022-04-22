package org.dau.ui.action

@JvmRecord
data class ActionKey(val group: String, val priority: Int) : Comparable<ActionKey> {

  override fun compareTo(other: ActionKey): Int {
    val c = priority.compareTo(other.priority)
    if (c != 0) {
      return c;
    }
    return group.compareTo(other.group)
  }
}
