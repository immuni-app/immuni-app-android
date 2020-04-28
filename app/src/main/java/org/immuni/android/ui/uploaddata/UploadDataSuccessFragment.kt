package org.immuni.android.ui.uploaddata

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.R
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class UploadDataSuccessFragment : Fragment(R.layout.upload_data_success_fragment) {
    private lateinit var viewModel: UploadDataViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        viewModel = getSharedViewModel()
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            close()
        }
    }

    private fun close() {
        activity?.finish()
    }
}
