package org.immuni.android.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.immuni.android.managers.ExposureNotificationManager
import org.koin.android.ext.android.inject

/**
 * This is the base class of all the activities.
 *
 */
open class ImmuniActivity : AppCompatActivity() {

    private val exposureNotificationManager: ExposureNotificationManager by inject()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        exposureNotificationManager.onRequestPermissionsResult(this, requestCode, resultCode, data)
    }
}
