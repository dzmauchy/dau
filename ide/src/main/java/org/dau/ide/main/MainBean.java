package org.dau.ide.main;

import org.springframework.context.annotation.Bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@MainQualifier
@Bean
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MainBean {
}
