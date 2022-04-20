package org.dau.ide.main

import org.springframework.beans.factory.annotation.Qualifier
import kotlin.annotation.AnnotationTarget.*

@Qualifier("mainUI")
@Target(ANNOTATION_CLASS, FUNCTION, CLASS, VALUE_PARAMETER)
annotation class MainQualifier
