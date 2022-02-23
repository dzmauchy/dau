package org.dau.runtime.runner;

import org.w3c.dom.Element;

import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Xmls {

  private Xmls() {}

  static Stream<Element> elements(Element element, String tag) {
    var nodes = element.getElementsByTagName(tag);
    return IntStream.range(0, nodes.getLength())
      .mapToObj(nodes::item)
      .filter(Element.class::isInstance)
      .map(Element.class::cast);
  }

  static Stream<Element> elements(Element element, String tag, String subTag) {
    return elements(element, tag).flatMap(e -> elements(e, subTag));
  }
}
