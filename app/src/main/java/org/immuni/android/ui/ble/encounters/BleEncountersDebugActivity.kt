package org.immuni.android.ui.ble.encounters

import android.os.Bundle
import org.immuni.android.ImmuniActivity
import org.immuni.android.R
import org.immuni.android.ui.ble.distance.BleDistanceDebugFragment

class BleEncountersDebugActivity : ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ble_encounters_debug_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container,
                    BleEncountersDebugFragment.newInstance()
                )
                .commitNow()
        }
    }
}
