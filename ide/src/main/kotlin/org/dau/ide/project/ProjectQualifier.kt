package org.dau.ide.project

import org.springframework.beans.factory.annotation.Qualifier
import kotlin.annotation.AnnotationTarget.*

@Qualifier("projectUI")
@Target(ANNOTATION_CLASS, FUNCTION, CLASS, VALUE_PARAMETER)
annotation class ProjectQualifier
