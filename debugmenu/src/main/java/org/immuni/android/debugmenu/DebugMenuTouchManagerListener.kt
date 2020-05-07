package org.immuni.android.debugmenu

/**
 * Listens to [DebugMenuTouchManager] events.
 */
interface DebugMenuTouchManagerListener {
    /**
     * Invoked when the [DebugMenuTouchManager] decides the debug menu must be opened.
     */
    fun onActivateSecretMenu()
}