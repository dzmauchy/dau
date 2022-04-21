package org.dau.ide.schema

import org.springframework.context.annotation.Bean

@Bean
@SchemaQualifier
@Target(AnnotationTarget.FUNCTION)
annotation class SchemaBean
