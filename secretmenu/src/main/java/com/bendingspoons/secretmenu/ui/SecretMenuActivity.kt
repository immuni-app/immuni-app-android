package com.bendingspoons.secretmenu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.secretmenu.R
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.secretmenu.item.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SecretMenuActivity : AppCompatActivity() {

    private val secretMenuConfig = SecretMenu.instance.config

    override fun onPause() {
        super.onPause()
        close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.secret_menu_activity)

        val items = mutableListOf(
            AppVersionItem(),
            DeviceInfoItem()
        ).apply {
            // public items
            addAll(secretMenuConfig.publicItems())
            // debugging items
            if (secretMenuConfig.isDevelopmentDevice()) {
                add(AllIdsItem())
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
