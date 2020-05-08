package org.immuni.android.ui.setup

import android.content.Intent
import android.os.Bundle
import org.immuni.android.R
import org.immuni.android.extensions.activity.setLightStatusBar
import org.immuni.android.ui.ImmuniActivity

class SetupActivity : ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTaskRoot &&
            intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
            intent.action != null &&
            intent.action == Intent.ACTION_MAIN) {
            // TODO handle FCM push notifications data
            // to get FCM intent data do like below
            // val value = intent.extras?.getString("immuni_key")
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
