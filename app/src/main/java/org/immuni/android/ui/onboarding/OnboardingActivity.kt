package org.immuni.android.ui.onboarding

import android.os.Bundle
import android.os.PersistableBundle
import androidx.navigation.findNavController
import org.immuni.android.ui.ImmuniActivity
import org.immuni.android.R
import org.koin.androidx.viewmodel.ext.android.getStateViewModel

class OnboardingActivity : ImmuniActivity() {

    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getStateViewModel()

        setContentView(R.layout.onboarding_activity)
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.onboarding)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
    }
}
