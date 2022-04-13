package org.dau.ui.utils

import org.w3c.dom.Element
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.reflect.cast

object Xmls {

  @JvmStatic
  fun elementsByTag(node: Element, tag: String): Stream<Element> {
    val byTag = node.getElementsByTagName(tag)
    return IntStream.range(0, byTag.length)
      .mapToObj(byTag::item)
      .filter(Element::class::isInstance)
      .map(Element::class::cast)
  }

  @JvmStatic
  fun elementsByTag(node: Element, baseTag: String, tag: String): Stream<Element> {
    return elementsByTag(node, baseTag).flatMap { elementsByTag(it, tag) }
  }
}