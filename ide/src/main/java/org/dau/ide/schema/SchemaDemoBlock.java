package org.dau.ide.schema;

import org.dau.runtime.Block;

@Block("Demo block")
public class SchemaDemoBlock {

  public SchemaDemoBlock(String sync, String name, int code, double len, double angle, double speed) {
  }

  public double distance() {
    return 0d;
  }

  public double out() {
    return 0d;
  }

  public double delta() {
    return 0;
  }
}
