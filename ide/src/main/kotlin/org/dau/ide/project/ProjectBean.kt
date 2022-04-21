package org.dau.ide.project

import org.springframework.context.annotation.Bean

@Bean
@ProjectQualifier
@Target(AnnotationTarget.FUNCTION)
annotation class ProjectBean
