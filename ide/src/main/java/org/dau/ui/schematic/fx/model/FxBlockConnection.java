package org.dau.ui.schematic.fx.model;

import org.dau.ui.schematic.fx.model.FxBlock.Input;
import org.dau.ui.schematic.fx.model.FxBlock.Output;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public record FxBlockConnection(Output out, Input in) {

  public Element toXml(Document doc) {
    var el = doc.createElement("connection");
    el.setAttribute("out-block-id", Integer.toString(out.getBlock().id));
    el.setAttribute("out", out.id);
    el.setAttribute("in-block-id", Integer.toString(in.getBlock().id));
    el.setAttribute("in", in.id);
    return el;
  }

  @Override
  public String toString() {
    return "Conn(" + out + "->" + in + ")";
  }
}
