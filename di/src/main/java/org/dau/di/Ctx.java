package org.dau.di;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

import java.util.Base64;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.lang.System.identityHashCode;
import static java.nio.ByteBuffer.allocate;
import static java.util.Objects.requireNonNull;

public final class Ctx extends AnnotationConfigApplicationContext {

  private static final Logger LOGGER = Logger.getLogger("contexts");
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  private final ConcurrentHashMap<String, Ctx> children = new ConcurrentHashMap<>(16, 0.5f);
  private final LinkedList<Object> roots = new LinkedList<>();

  public Ctx(Ctx parent, String name) {
    super.setId(ENCODER.encodeToString(allocate(4).putInt(0, identityHashCode(this)).array()));
    setParent(parent);
    setDisplayName(name);
    var bf = getDefaultListableBeanFactory();
    bf.setAllowCircularReferences(false);
    bf.setAllowBeanDefinitionOverriding(false);
    if (parent != null) {
      parent.children.compute(getId(), (k, old) -> {
        if (old == null) {
          return this;
        } else {
          throw new IllegalStateException("Duplicated context in " + parent + ": " + this + "/" + old);
        }
      });
    }
  }

  public Ctx(String name) {
    this(null, name);
  }

  @Override
  protected void initApplicationEventMulticaster() {
    var bf = getDefaultListableBeanFactory();
    bf.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, new SimpleApplicationEventMulticaster() {

      private boolean shouldSkip(ApplicationEvent event) {
        if (event instanceof ApplicationContextEvent e) {
          if (e.getApplicationContext() != Ctx.this) {
            return true;
          }
        }
        if (event instanceof PayloadApplicationEvent<?> e) {
          var p = e.getPayload();
          if (p instanceof UIEvent<?> && e.getSource() instanceof Ctx c && c != Ctx.this) {
            return true;
          }
        }
        LOGGER.info(event::toString);
        return false;
      }

      @Override
      public void multicastEvent(@NonNull ApplicationEvent event) {
        if (shouldSkip(event)) {
          return;
        }
        super.multicastEvent(event);
        if (event instanceof PayloadApplicationEvent<?> e) {
          var p = e.getPayload();
          if (p instanceof UIEvent<?>) {
            children.forEach((k, v) -> v.publishEvent(new PayloadApplicationEvent<>(v, p)));
          }
        }
      }

      @Override
      public void multicastEvent(@NonNull ApplicationEvent event, ResolvableType eventType) {
        if (shouldSkip(event)) {
          return;
        }
        super.multicastEvent(event, eventType);
        if (event instanceof PayloadApplicationEvent<?> e) {
          var p = e.getPayload();
          if (p instanceof UIEvent<?>) {
            children.forEach((k, v) -> v.publishEvent(new PayloadApplicationEvent<>(v, p), eventType));
          }
        }
      }
    });
    super.initApplicationEventMulticaster();
  }

  @Override
  protected void doClose() {
    var p = getParent();
    if (p != null) {
      var pc = (Ctx) p;
      pc.children.remove(requireNonNull(getId()));
    }
    try {
      children.entrySet().removeIf(e -> {
        e.getValue().close();
        return true;
      });
    } finally {
      super.doClose();
    }
  }

  private String getPath() {
    if (!(getParent() instanceof Ctx e)) {
      return getId();
    } else {
      return e.getPath() + "/" + getId();
    }
  }

  public void addRoot(Object root) {
    roots.add(root);
  }

  @Override
  public void setId(@NonNull String id) {
    throw new UnsupportedOperationException("Ctx id is immutable");
  }

  @NonNull
  @Override
  public String toString() {
    return "[" + getPath() + "] " + getDisplayName();
  }
}
