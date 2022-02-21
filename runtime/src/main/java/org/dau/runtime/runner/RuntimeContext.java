package org.dau.runtime.runner;

import java.util.concurrent.ConcurrentHashMap;

public final class RuntimeContext {

  private final ConcurrentHashMap<String, Object> sharedMap = new ConcurrentHashMap<>(128);

  public void setShared(String name, Object value) {
    sharedMap.put(name, value);
  }

  public Object getShared(String name) {
    return sharedMap.get(name);
  }
}
