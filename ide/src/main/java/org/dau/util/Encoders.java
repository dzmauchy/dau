package org.dau.util;

import java.nio.ByteBuffer;
import java.util.Base64;

public final class Encoders {

  private Encoders() {}

  public static final Base64.Encoder ID_ENCODER = Base64.getUrlEncoder().withoutPadding();

  public static String generateId(Object o) {
    int timestamp = (int) (System.currentTimeMillis() / 1000L);
    int hash = System.identityHashCode(o);
    var bytes = ByteBuffer.allocate(8).putInt(0, timestamp).putInt(4, hash).array();
    return ID_ENCODER.encodeToString(bytes);
  }
}
