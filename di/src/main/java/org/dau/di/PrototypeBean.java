package org.dau.di;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Bean
@Prototype
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrototypeBean {

  @AliasFor(annotation = Bean.class, attribute = "name")
  String[] name() default {};
}
