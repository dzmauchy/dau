package org.dau.ui.fx

import javafx.application.Platform
import java.lang.ref.Cleaner
import java.lang.ref.Cleaner.Cleanable

object FxCleaner {

    private val CLEANER = Cleaner.create()

    @JvmStatic
    fun clean(ref: Any?, runnable: Runnable?): Cleanable {
        return CLEANER.register(ref) { Platform.runLater(runnable) }
    }
}