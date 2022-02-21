package org.dau.runtime.runner;

import java.net.URL;
import java.util.jar.JarInputStream;

public final class Runner {

  private Runner() {}

  public static void main(String... args) throws Exception {
    if (args.length == 0) {
      return;
    }
    var url = new URL(args[0]);
    var deployment = deployment(url);
    var classLoader = Thread.currentThread().getContextClassLoader();
    deployment.run(url, classLoader, Runner::onClose);
  }

  public static Deployment deployment(URL url) throws Exception {
    try (var jis = new JarInputStream(url.openStream(), true)) {
      for (var e = jis.getNextJarEntry(); e != null; e = jis.getNextJarEntry()) {
        if (e.getName().equals("META-INF/dau/deployment.xml")) {
          return DeploymentParser.parse(jis);
        }
      }
    }
    throw new IllegalStateException("No deployment descriptor found");
  }

  private static void onClose(Runnable runnable) {
    Runtime.getRuntime().addShutdownHook(new Thread(runnable));
  }
}
