package org.dau.runtime.runner;

public interface DeploymentDestroyer extends Runnable {

  void destroy() throws Exception;

  @Override
  default void run() {
    try {
      destroy();
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
