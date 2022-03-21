package org.dau.di;

import java.util.function.Consumer;

public final class B<T> {

  private final T v;

  public B(T value) {
    this.v = value;
  }

  public B<T> on(Consumer<T> consumer) {
    consumer.accept(v);
    return this;
  }

  public T build() {
    return v;
  }

  public T v() {
    return v;
  }

  @SafeVarargs
  public static <T> T with(T value, Consumer<T>... consumers) {
    for (var c : consumers) {
      c.accept(value);
    }
    return value;
  }
}
