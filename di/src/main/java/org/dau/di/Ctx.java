package org.dau.di;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public final class Ctx extends AnnotationConfigApplicationContext {

  private static final Logger LOGGER = Logger.getLogger("contexts");

  private final ConcurrentHashMap<String, Ctx> children = new ConcurrentHashMap<>(16, 0.5f);

  public Ctx(Ctx parent, String id, String name) {
    setParent(parent);
    setId(id);
    setDisplayName(name);
    var bf = getDefaultListableBeanFactory();
    bf.setAllowCircularReferences(false);
    bf.setAllowBeanDefinitionOverriding(false);
    if (parent != null) {
      parent.children.compute(id, (k, old) -> {
        if (old == null) {
          return this;
        } else {
          throw new IllegalStateException("Duplicated context in " + parent + ": " + this + "/" + old);
        }
      });
    }
  }

  public Ctx(String id, String name) {
    this(null, id, name);
  }

  @Override
  protected void initApplicationEventMulticaster() {
    var bf = getDefaultListableBeanFactory();
    bf.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, new SimpleApplicationEventMulticaster() {

      private boolean shouldSkip(ApplicationEvent event) {
        if (event instanceof ApplicationContextEvent e) {
          LOGGER.info(event::toString);
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

  @NonNull
  @Override
  public String toString() {
    return "[" + getPath() + "] " + getDisplayName();
  }
}
