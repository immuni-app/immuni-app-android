package org.ascolto.onlus.geocrowd19.android.ui.force_update

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.ascolto.onlus.geocrowd19.android.R
import com.bendingspoons.base.extensions.setDarkStatusBar

class ForceUpdateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.force_update_activity)
        setDarkStatusBar(resources.getColor(R.color.colorPrimary))
    }
}
