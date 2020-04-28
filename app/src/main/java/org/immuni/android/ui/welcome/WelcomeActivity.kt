package org.immuni.android.ui.welcome

import android.os.Bundle
import android.os.PersistableBundle
import androidx.navigation.findNavController
import org.immuni.android.ui.ImmuniActivity
import org.immuni.android.R

class WelcomeActivity : ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_activity)
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.welcome)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
    }
}
