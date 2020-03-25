package org.ascolto.onlus.geocrowd19.android.ui.uploadData

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import org.ascolto.onlus.geocrowd19.android.R
import org.koin.androidx.viewmodel.ext.android.getViewModel

class UploadDataActivity : AppCompatActivity(R.layout.upload_data_activity) {
    private lateinit var viewModel: UploadDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getViewModel()

        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.upload_data)
    }
}
