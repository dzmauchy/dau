package org.dau.runtime.runner;

public record Library(String group, String name, String version, String classifier) {
  public Library(String group, String name, String version) {
    this(group, name, version, "");
  }
}