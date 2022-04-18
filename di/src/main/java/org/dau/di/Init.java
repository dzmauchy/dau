package org.dau.di;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Comparator.comparingInt;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public final class Init {

  private static final ConcurrentHashMap<Class<?>, HashMap<Method, Integer>> ORDER = new ConcurrentHashMap<>(8);
  private static final ConcurrentHashMap<Object, TreeMap<Method, Runnable>> TASKS = new ConcurrentHashMap<>(8);

  private final Method member;

  public Init(InjectionPoint injectionPoint) {
    member = (Method) injectionPoint.getMember();
  }

  public void schedule(Object self, Runnable task) {
    final var cl = self.getClass();
    final var om = ORDER.computeIfAbsent(cl, c -> {
      final var list = Arrays.stream(c.getMethods())
        .filter(m -> m.isAnnotationPresent(Autowired.class))
        .filter(m -> Arrays.stream(m.getParameterTypes()).anyMatch(t -> t == Init.class))
        .toList();
      final var map = new HashMap<Method, Integer>(list.size());
      for (int i = 0; i < list.size(); i++) {
        map.put(list.get(i), i + 1);
      }
      return map;
    });
    final var q = TASKS.computeIfAbsent(self, s -> new TreeMap<>(comparingInt(om::get)));
    q.put(member, task);
    final var m = ORDER.get(cl);
    final int order = m.get(member);
    if (order == m.size()) {
      TASKS.remove(self);
      q.forEach((mt, t) -> {
        try {
          t.run();
        } catch (RuntimeException e) {
          throw new IllegalStateException("Unable to run " + mt + " of " + self, e);
        }
      });
    }
  }
}
