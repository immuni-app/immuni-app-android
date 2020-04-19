package org.immuni.android.ui.forceupdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.immuni.android.R
import com.bendingspoons.base.extensions.setDarkStatusBar

class ForceUpdateActivity : AppCompatActivity() {

    companion object {
        var isOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOpen = true
        setContentView(R.layout.force_update_activity)
        setDarkStatusBar(resources.getColor(R.color.colorPrimary))
    }

    override fun onDestroy() {
        super.onDestroy()
        isOpen = false
    }
}
