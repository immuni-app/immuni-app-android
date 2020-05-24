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

package it.ministerodellasalute.immuni.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.forceupdate.ForceUpdateManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.ui.forceupdate.ForceUpdateActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * This is the base class of all the activities.
 *
 */
abstract class ImmuniActivity : AppCompatActivity() {

    private val exposureManager: ExposureManager by inject()
    private val forceUpdateManager: ForceUpdateManager by inject()
    private val configurationSettingsManager: ConfigurationSettingsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (this !is ForceUpdateActivity) {
            forceUpdateManager.shouldShowForceUpdate.asLiveData().observe(this, Observer {
                showForceUpdate()
            })
        }
    }

    private fun showForceUpdate() {
        val intent = Intent(this, ForceUpdateActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor =
                ContextCompat.getColor(applicationContext, R.color.background)
        }
    }

    /**
     * Close the activity if an app force update
     * or a Google Play Services update is required.
     * Except for [ForceUpdateActivity] itself.
     */
    override fun onResume() {
        super.onResume()
        if (this !is ForceUpdateActivity) {
            lifecycleScope.launch {
                configurationSettingsManager.fetchSettings()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        exposureManager.onRequestPermissionsResult(this, requestCode, resultCode, data)
    }
}
