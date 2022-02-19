package org.dau.base;

import org.dau.runtime.Block;
import org.dau.runtime.Factory;
import org.dau.runtime.Inlined;

import java.util.Collections;
import java.util.List;

@Factory("Common inlined functions")
public class Intrinsics {

  @Block("Repeater")
  @Inlined("${arg}")
  public static <T> T repeater(T arg) {
    return arg;
  }

  @Block("Empty list")
  @Inlined("java.util.Collections.<#{T}>emptyList()")
  public static <T> List<T> emptyList() {
    return Collections.emptyList();
  }

  @Block("Singleton list")
  @Inlined("java.util.Collections.singletonList(${arg})")
  public static <T> List<T> singletonList(T arg) {
    return Collections.singletonList(arg);
  }

  @Block("List of 2 elements")
  @Inlined("java.util.List.of(${arg1},${arg2})")
  public static <T> List<T> list2(T arg1, T arg2) {
    return List.of(arg1, arg2);
  }

  @Block("List of 3 elements")
  @Inlined("java.util.List.of(${arg1},${arg2},${arg3})")
  public static <T> List<T> list3(T arg1, T arg2, T arg3) {
    return List.of(arg1, arg2, arg3);
  }
}
