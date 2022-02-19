package org.dau.classpath;

import javafx.util.Pair;
import org.dau.runtime.Block;
import org.dau.runtime.Factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.net.URLClassLoader;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.sort;

public final class Executables {

  public static final Comparator<Package> PACKAGE_COMPARATOR = Comparator
    .comparing(Package::getName);

  public static final Comparator<Executable> EXECUTABLE_COMPARATOR = Comparator
    .<Executable, String>comparing(e -> e.getDeclaringClass().getName())
    .thenComparing(Executable::getName);

  private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = Comparator
    .<Constructor<?>>comparingInt(Constructor::getParameterCount)
    .reversed();

  private final ConcurrentSkipListMap<Package, ConcurrentSkipListSet<Executable>> map =
    new ConcurrentSkipListMap<>(PACKAGE_COMPARATOR);

  private final ConcurrentHashMap<BlockKey, Executable> index = new ConcurrentHashMap<>(32, 0.25f);

  private final ConcurrentSkipListSet<String> notFound = new ConcurrentSkipListSet<>();

  Executables() {}

  public Executables(Class<?>... classes) {
    for (var cl : classes) {
      tryProcess(cl);
    }
  }

  void tryProcess(Class<?> cl) {
    if (cl.isAnnotationPresent(Factory.class)) {
      for (var m : cl.getMethods()) {
        if (m.isAnnotationPresent(Block.class)) {
          map
            .computeIfAbsent(cl.getPackage(), k -> new ConcurrentSkipListSet<>(EXECUTABLE_COMPARATOR))
            .add(m);
          index.put(new BlockKey(m.getDeclaringClass().getName(), m.getName()), m);
        }
      }
    }
    if (cl.isAnnotationPresent(Block.class)) {
      var constructors = cl.getConstructors();
      sort(constructors, CONSTRUCTOR_COMPARATOR);
      for (var c : constructors) {
        map
          .computeIfAbsent(cl.getPackage(), k -> new ConcurrentSkipListSet<>(EXECUTABLE_COMPARATOR))
          .add(c);
        index.put(new BlockKey(c.getDeclaringClass().getName(), c.getName()), c);
        return;
      }
    }
  }

  void tryProcess(URLClassLoader classLoader, String name) {
    try {
      var cl = Class.forName(name, false, classLoader);
      tryProcess(cl);
    } catch (ClassNotFoundException ignore) {
      notFound.add(name);
    }
  }

  public Stream<String> notFound() {
    return notFound.stream();
  }

  public void forEachNotFound(Consumer<String> consumer) {
    notFound.forEach(consumer);
  }

  public Stream<Pair<Package, Supplier<Stream<Executable>>>> executables() {
    return map.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue()::stream));
  }

  public void forEach(BiConsumer<Package, Consumer<Consumer<Executable>>> consumer) {
    map.forEach((p, s) -> consumer.accept(p, s::forEach));
  }

  public boolean hasNotFound() {
    return !notFound.isEmpty();
  }

  public Executable getExecutable(String className, String name) {
    return index.get(new BlockKey(className, name));
  }

  public Optional<Executable> executable(String className, String name) {
    return Optional.ofNullable(getExecutable(className, name));
  }

  public Executable getExecutable(BlockKey key) {
    return index.get(key);
  }

  public Optional<Executable> executable(BlockKey key) {
    return Optional.ofNullable(getExecutable(key));
  }

  public int size() {
    return index.size();
  }
}
