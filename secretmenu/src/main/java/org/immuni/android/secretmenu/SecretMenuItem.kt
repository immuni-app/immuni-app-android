package org.immuni.android.secretmenu

import android.content.Context
import org.immuni.android.secretmenu.SecretMenuConfiguration

/**
 * An abstract item in the secret menu feature list.
 *
 * @param title the visible name of the secret menu feature.
 * @param action a block executed when the item is clicked.
 */
abstract class SecretMenuItem(
    val title: String,
    val action: (context: Context, config: SecretMenuConfiguration) -> Unit
)
