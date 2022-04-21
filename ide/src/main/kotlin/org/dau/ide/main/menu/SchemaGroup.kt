package org.dau.ide.main.menu

import org.dau.ide.action.MenuBarGroup

@MenuBarGroup(name = "Schema", priority = 2)
@Target(AnnotationTarget.FUNCTION)
annotation class SchemaGroup
