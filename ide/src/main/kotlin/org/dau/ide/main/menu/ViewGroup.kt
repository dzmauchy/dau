package org.dau.ide.main.menu

import org.dau.ide.action.MenuBarGroup

@MenuBarGroup(name = "View", priority = 3)
@Target(AnnotationTarget.FUNCTION)
annotation class ViewGroup
