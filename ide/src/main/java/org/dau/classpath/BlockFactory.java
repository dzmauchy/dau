package org.dau.classpath;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterators;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

public final class BlockFactory {

  private static final String CLASS_SUFFIX = ".class";

  private BlockFactory() {}

  private static void findExecutables(URLClassLoader classLoader, Executables map, URL url) throws Exception {
    if (url.getPath().endsWith(".jar")) {
      try (var jis = new JarInputStream(url.openStream(), true)) {
        for (var je = jis.getNextJarEntry(); je != null; je = jis.getNextJarEntry()) {
          if (je.isDirectory()) {
            continue;
          }
          if (!je.getName().endsWith(CLASS_SUFFIX)) {
            continue;
          }
          var fileName = je.getName();
          var fileNameWithoutExtension = fileName.substring(0, fileName.length() - CLASS_SUFFIX.length());
          var className = fileNameWithoutExtension.replace('/', '.');
          map.tryProcess(classLoader, className);
        }
      }
    } else {
      var base = Path.of(url.toURI());
      var ds = Files.walk(base)
        .filter(Files::isRegularFile)
        .filter(f -> f.getFileName().toString().endsWith(CLASS_SUFFIX));
      try (ds) {
        ds.forEach(f -> {
          var relativePath = base.relativize(f);
          var fileName = IntStream.range(0, relativePath.getNameCount())
            .mapToObj(relativePath::getName)
            .map(Path::toString)
            .collect(Collectors.joining("."));
          var className = fileName.substring(0, fileName.length() - CLASS_SUFFIX.length());
          map.tryProcess(classLoader, className);
        });
      }
    }
  }

  public static Executables findExecutables(URLClassLoader classLoader) {
    var map = new Executables();
    var urls = classLoader.getURLs();
    var spl = Spliterators.<URL>spliterator(urls, IMMUTABLE | NONNULL | DISTINCT);
    StreamSupport.stream(spl, true).forEach(url -> {
      try {
        findExecutables(classLoader, map, url);
      } catch (Throwable e) {
        throw new IllegalStateException(e);
      }
    });
    return map;
  }
}
