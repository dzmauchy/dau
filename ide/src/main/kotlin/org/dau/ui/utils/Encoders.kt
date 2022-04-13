package org.dau.ui.utils

import java.nio.ByteBuffer
import java.time.LocalDate
import java.util.*

object Encoders {

  val ID_ENCODER = Base64.getUrlEncoder().withoutPadding()

  @JvmStatic
  fun generateId(o: Any): String {
    val ldt = LocalDate.now()
    val timePart = String.format("%02d%02d%02d", ldt.year % 100, ldt.monthValue, ldt.dayOfMonth)
    val hash = System.identityHashCode(o)
    val bytes = ByteBuffer.allocate(4).putInt(0, hash).array()
    return timePart + "_" + ID_ENCODER.encodeToString(bytes)
  }
}