package org.dau.di;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Prototype
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrototypeComponent {

  @AliasFor(annotation = Component.class, attribute = "value")
  String name() default "";
}
