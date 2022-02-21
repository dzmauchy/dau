package org.dau.util;

import org.w3c.dom.Element;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Xmls {

  private Xmls() {}

  public static Stream<Element> elementsByTag(Element node, String tag) {
    var byTag = node.getElementsByTagName(tag);
    return IntStream.range(0, byTag.getLength())
      .mapToObj(byTag::item)
      .filter(Element.class::isInstance)
      .map(Element.class::cast);
  }

  public static Stream<Element> elementsByTag(Element node, String baseTag, String tag) {
    return elementsByTag(node, baseTag).flatMap(e -> elementsByTag(e, tag));
  }
}
