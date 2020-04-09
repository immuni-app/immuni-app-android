package org.immuni.android.ui.ble.distance

import android.os.Bundle
import org.immuni.android.ImmuniActivity
import org.immuni.android.R

class BleDistanceDebugActivity : ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ble_distance_debug_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container,
                    BleDistanceDebugFragment.newInstance()
                )
                .commitNow()
        }
    }
}
