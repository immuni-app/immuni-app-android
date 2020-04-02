package org.immuni.android.ui.ble

import android.os.Bundle
import org.immuni.android.ImmuniActivity
import org.immuni.android.R

class BleDebugActivity : ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ble_debug_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, BleDebugFragment.newInstance())
                .commitNow()
        }
    }
}
