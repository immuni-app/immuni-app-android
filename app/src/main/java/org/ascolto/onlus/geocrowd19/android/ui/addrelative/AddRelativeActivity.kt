package org.ascolto.onlus.geocrowd19.android.ui.addrelative

import android.os.Bundle
import android.os.PersistableBundle
import androidx.navigation.findNavController
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R
import org.koin.androidx.viewmodel.ext.android.getStateViewModel

class AddRelativeActivity : AscoltoActivity() {

    private lateinit var viewModel: AddRelativeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getStateViewModel()

        setContentView(R.layout.add_relative_activity)
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.add_relative)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
    }
}
