package org.immuni.android.debugmenu

import org.immuni.android.ids.IdsManager

/**
 * This is the debug menu configuration
 * the app injects into the [DebugMenu].
 */
interface DebugMenuConfiguration {

    val isDevelopmentDevice: () -> Boolean

    fun idsManager(): IdsManager

    // available to anyone
    fun publicItems(): List<DebugMenuItem>

    // available only during debug
    fun debuggingItems(): List<DebugMenuItem>
}
