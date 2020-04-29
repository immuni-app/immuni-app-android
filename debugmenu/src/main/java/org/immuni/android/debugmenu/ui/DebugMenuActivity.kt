package org.immuni.android.debugmenu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.debugmenu.DebugMenu
import org.immuni.android.debugmenu.item.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.immuni.android.debugmenu.R
import org.immuni.android.debugmenu.DebugMenuItem

/**
 * This activity show all the [DebugMenuItem] in a list and
 * allow the user to execute the relative action.
 */
class DebugMenuActivity : AppCompatActivity() {

    private val secretMenuConfig = DebugMenu.instance.config

    override fun onPause() {
        super.onPause()
        close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_menu_activity)

        val items = mutableListOf(
            AppVersionItem(),
            DeviceInfoItem()
        ).apply {
            // public items
            addAll(secretMenuConfig.publicItems())
            // debugging items
            if (secretMenuConfig.isDevelopmentDevice()) {
                addAll(secretMenuConfig.debuggingItems())
                add(ClearUserIdentityItem())
                add(ClearAppDataItem())
                add(ForceQuitDataItem())
                add(CrashItem())
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("How can I help you? ${DeviceUtils.appVersionName(applicationContext)} (${DeviceUtils.appVersionCode(applicationContext)})")
            .setNegativeButton("Cancel") { _, _ -> close()}
            .setOnCancelListener { close() }
            .setItems(
                items.map { it.title }.toTypedArray()
            ) { dialog, which ->
                items[which].action(applicationContext, secretMenuConfig)
                close()
            }
            .show()
    }

    private fun close() {
        try {
            this.finish()
        } catch (e: Exception) {e.printStackTrace()}
    }
}
