package org.immuni.android.debugmenu

import android.content.Context

/**
 * An abstract item in the debug menu feature list.
 *
 * @param title the visible name of the debug menu feature.
 * @param action a block executed when the item is clicked.
 */
abstract class DebugMenuItem(
    val title: String,
    val action: (context: Context, config: DebugMenuConfiguration) -> Unit
)
