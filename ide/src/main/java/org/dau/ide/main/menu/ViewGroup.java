package org.dau.ide.main.menu;

import org.dau.ide.action.MenuBarGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@MenuBarGroup(name = "View", priority = 2)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewGroup {
}
