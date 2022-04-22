package org.dau.ui.action

import org.springframework.beans.factory.annotation.Qualifier

@Qualifier
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class MenuBarGroup(val name: String, val priority: Int)
