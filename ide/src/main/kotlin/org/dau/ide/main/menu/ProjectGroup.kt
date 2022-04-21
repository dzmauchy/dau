package org.dau.ide.main.menu

import org.dau.ide.action.MenuBarGroup

@MenuBarGroup(name = "Project", priority = 1)
@Target(AnnotationTarget.FUNCTION)
annotation class ProjectGroup
