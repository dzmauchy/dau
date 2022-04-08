package org.dau.classpath;

public record Dependency(String group, String name, String version) {
  @Override
  public String toString() {
    return group + ":" + name + ":" + version;
  }
}
