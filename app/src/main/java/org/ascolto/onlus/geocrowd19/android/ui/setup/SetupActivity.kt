package org.ascolto.onlus.geocrowd19.android.ui.setup

import android.content.Intent
import android.os.Bundle
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R
import com.bendingspoons.base.extensions.setDarkStatusBar
import com.bendingspoons.base.extensions.setLightStatusBar

class SetupActivity : AscoltoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }

        setContentView(R.layout.setup_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SetupFragment.newInstance())
                .commitNow()
        }

        setLightStatusBar(resources.getColor(R.color.transparent))
    }

}
