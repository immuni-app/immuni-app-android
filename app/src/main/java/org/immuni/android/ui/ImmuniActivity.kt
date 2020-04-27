package org.immuni.android.ui

import androidx.appcompat.app.AppCompatActivity
import org.immuni.android.managers.PermissionsManager
import org.koin.android.ext.android.inject

/**
 * This is the base class of all the activities.
 *
 */
open class ImmuniActivity : AppCompatActivity() {

    private val permissionsManager: PermissionsManager by inject()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }
}