package org.dau.ide.action;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionGroup {
  String name();
  int priority();
}
