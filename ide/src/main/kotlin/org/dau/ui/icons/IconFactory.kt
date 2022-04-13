package org.dau.ui.icons

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import java.util.concurrent.ConcurrentHashMap

object IconFactory {

  private val CACHE = ConcurrentHashMap<Key, Image?>(32)

  @JvmStatic
  @JvmOverloads
  fun image(path: String?, size: Int = 0): Image? = when (path) {
    null -> null
    else -> CACHE.computeIfAbsent(Key(path, size)) { k ->
      val cl = Thread.currentThread().contextClassLoader
      cl.getResource(k.path)?.let {
        if (k.size == 0)
          Image(it.toString(), false)
        else Image(it.toString(), k.size.toDouble(), k.size.toDouble(), true, true, false)
      }
    }
  }

  @JvmStatic
  @JvmOverloads
  fun icon(path: String?, size: Int = 0): ImageView? = image(path, size)?.let(::ImageView)

  @JvmStatic
  @JvmOverloads
  fun icon(icon: Ikon, size: Int = 0, color: Color = Color.LIGHTGRAY) = FontIcon(icon).apply {
    iconColor = color
    if (size > 0) iconSize = size
  }

  @JvmRecord
  private data class Key(val path: String, val size: Int)
}