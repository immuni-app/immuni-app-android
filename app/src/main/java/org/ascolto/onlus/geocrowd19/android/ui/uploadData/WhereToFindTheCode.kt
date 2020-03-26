package org.ascolto.onlus.geocrowd19.android.ui.uploadData

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.*
import org.ascolto.onlus.geocrowd19.android.R
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class WhereToFindTheCodeFragment : Fragment(R.layout.insert_upload_data_code_fragment) {
    private lateinit var viewModel: UploadDataViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()

        close.setOnClickListener {
            close()
        }

        okButton.setOnClickListener {
            close()
        }
    }

    private fun close() {
        findNavController().popBackStack()
    }

}
