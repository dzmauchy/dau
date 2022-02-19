package org.dau.ui.schematic.fx.model;

import org.dau.ui.schematic.fx.model.FxBlock.Input;
import org.dau.ui.schematic.fx.model.FxBlock.Output;

public record FxBlockConnection(Output out, Input in) {

  @Override
  public String toString() {
    return "Conn(" + out + "->" + in + ")";
  }
}
