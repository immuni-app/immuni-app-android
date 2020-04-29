package org.immuni.android.debugmenu

import org.immuni.android.ids.Ids

/**
 * This is the debug menu configuration
 * the app injects into the [DebugMenu].
 */
interface DebugMenuConfiguration {

    val isDevelopmentDevice: () -> Boolean

    fun ids(): Ids

    // available to anyone
    fun publicItems(): List<DebugMenuItem>

    // available only during debug
    fun debuggingItems(): List<DebugMenuItem>
}
