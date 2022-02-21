package org.dau.base;

import org.dau.runtime.Factory;
import org.dau.runtime.Inlined;

@Factory("Ports")
public class Ports {

  @Inlined("$runtimeContext.setShared(\"@{outBlock}\", ${arg})")
  public static void out(Object arg) {
  }

  @Inlined("$runtimeContext.getShared(\"inBlock\")")
  public static <T> T in() {
    return null;
  }
}
