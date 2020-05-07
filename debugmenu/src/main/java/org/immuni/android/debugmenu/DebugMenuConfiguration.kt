package org.immuni.android.debugmenu

/**
 * This is the debug menu configuration
 * the app injects into the [DebugMenu].
 */
interface DebugMenuConfiguration {

    val isDevelopmentDevice: () -> Boolean

    // available to anyone
    fun publicItems(): List<DebugMenuItem>

    // available only during debug
    fun debuggingItems(): List<DebugMenuItem>
}
