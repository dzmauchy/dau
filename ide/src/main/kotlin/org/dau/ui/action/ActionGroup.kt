package org.dau.ui.action

import org.springframework.beans.factory.annotation.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION)
annotation class ActionGroup(val name: String, val priority: Int = 0)
