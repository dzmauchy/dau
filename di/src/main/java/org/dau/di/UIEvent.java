package org.dau.di;

import java.util.EventObject;

public abstract class UIEvent<S> extends EventObject {

  public UIEvent(S source) {
    super(source);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final S getSource() {
    return (S) super.getSource();
  }
}
