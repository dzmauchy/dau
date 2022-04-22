package org.dau.ide.main

import org.springframework.context.annotation.Bean

@MainQualifier
@Bean
@Target(AnnotationTarget.FUNCTION)
annotation class MainBean
