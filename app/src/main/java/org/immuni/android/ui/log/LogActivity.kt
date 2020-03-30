package org.immuni.android.ui.log

import android.os.Bundle
import androidx.navigation.findNavController
import org.immuni.android.AscoltoActivity
import org.immuni.android.R
import org.koin.androidx.viewmodel.ext.android.getStateViewModel

class LogActivity : AscoltoActivity() {

    private lateinit var viewModel: LogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getStateViewModel()
        setContentView(R.layout.log_activity)
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.log)
    }
}
