package org.ascolto.onlus.geocrowd19.android.ui.uploadData

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bendingspoons.base.extensions.invisible
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.base.extensions.visible
import kotlinx.android.synthetic.main.insert_upload_data_code_fragment.*
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.*
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.close
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.okButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.loading
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import kotlin.error

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
