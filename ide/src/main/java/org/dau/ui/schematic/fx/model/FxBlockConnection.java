package org.dau.ui.schematic.fx.model;

import org.dau.ui.schematic.fx.model.FxBlock.Input;
import org.dau.ui.schematic.fx.model.FxBlock.Output;
import org.dau.ui.schematic.layout.model.BlockConnection;

public record FxBlockConnection(Output out, Input in) implements BlockConnection {
  @Override
  public Output getOut() {
    return out;
  }

  @Override
  public Input getIn() {
    return in;
  }

  @Override
  public String toString() {
    return "Conn(" + out + "->" + in + ")";
  }
}
