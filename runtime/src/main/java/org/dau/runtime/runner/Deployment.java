package org.dau.runtime.runner;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.invoke.MethodType.methodType;

public record Deployment(String name, List<DeploymentUnit> units, List<Repository> repos, List<Library> libraries) {

  public void run(URL code, ClassLoader parent, Consumer<DeploymentDestroyer> beforeRun) {
    if (units.isEmpty()) {
      return;
    }
    var classLoader = classLoader(parent, code);
    beforeRun.accept(classLoader::close);
    var lookup = MethodHandles.lookup();
    var context = new RuntimeContext();
    for (var unit : units) {
      try {
        var cl = Class.forName(unit.entryPoint(), true, classLoader);
        var mh = lookup.findStatic(cl, "run", methodType(void.class, RuntimeContext.class));
        var thread = new Thread(() -> {
          try {
            mh.invokeExact(context);
          } catch (Throwable e) {
            throw new IllegalStateException("Unable to run unit " + unit.name(), e);
          }
        }, unit.name());
        thread.start();
      } catch (Throwable e) {
        throw new IllegalStateException("Unable to run unit " + unit.name(), e);
      }
    }
  }

  private URLClassLoader classLoader(ClassLoader parent, URL code) {
    var urls = new LinkedHashSet<URL>();
    for (var library : libraries) {
      var url = repos.stream()
        .flatMap(r -> r.existingURL(library).stream())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to locate artifact for " + library + " in " + repos));
      urls.add(url);
    }
    urls.add(code);
    return new URLClassLoader(name, urls.toArray(URL[]::new), parent);
  }
}
