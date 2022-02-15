package org.dau.ui.fx;

import javafx.application.Platform;

import java.lang.ref.Cleaner;

public final class FxCleaner {

  private static final Cleaner CLEANER = Cleaner.create();

  public static Cleaner.Cleanable clean(Object ref, Runnable runnable) {
    return CLEANER.register(ref, () -> Platform.runLater(runnable));
  }
}
