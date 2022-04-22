package org.dau.ide.main.menu

import org.dau.ui.action.MenuBarGroup

@MenuBarGroup(name = "Project", priority = 1)
@Target(AnnotationTarget.FUNCTION)
annotation class ProjectGroup
