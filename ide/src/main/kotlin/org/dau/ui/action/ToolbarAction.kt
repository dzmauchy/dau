package org.dau.ui.action

import org.springframework.beans.factory.annotation.Qualifier
import kotlin.annotation.AnnotationTarget.*

@Qualifier
@Target(ANNOTATION_CLASS, FUNCTION, CLASS, VALUE_PARAMETER)
annotation class ToolbarAction
