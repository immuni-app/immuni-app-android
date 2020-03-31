package org.immuni.android.ui.ble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.immuni.android.AscoltoActivity
import org.immuni.android.R

class BleDebugActivity : AscoltoActivity() {

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
