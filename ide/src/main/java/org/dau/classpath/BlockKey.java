package org.dau.classpath;

public record BlockKey(String className, String name) {
  @Override
  public String toString() {
    if (name.equals(className)) {
      return className + ".init";
    } else {
      return className + "." + name;
    }
  }
}
