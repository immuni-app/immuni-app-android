/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.debugmenu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.ministerodellasalute.immuni.debugmenu.DebugMenu
import it.ministerodellasalute.immuni.debugmenu.DebugMenuItem
import it.ministerodellasalute.immuni.debugmenu.R
import it.ministerodellasalute.immuni.debugmenu.item.*
import it.ministerodellasalute.immuni.extensions.utils.DeviceUtils

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
            // debugging items
            if (secretMenuConfig.isDevelopmentDevice()) {
                addAll(secretMenuConfig.debuggingItems())
                add(ClearAppDataItem())
                add(ForceQuitDataItem())
                add(CrashItem())
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("How can I help you? ${DeviceUtils.appVersionName(applicationContext)} (${DeviceUtils.appVersionCode(applicationContext)})")
            .setNegativeButton("Cancel") { _, _ -> close() }
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
        } catch (e: Exception) { e.printStackTrace() }
    }
}
