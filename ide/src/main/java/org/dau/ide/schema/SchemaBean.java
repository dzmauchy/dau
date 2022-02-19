package org.dau.ide.schema;

import org.springframework.context.annotation.Bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Bean
@SchemaQualifier
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaBean {
}
