package org.ascolto.onlus.geocrowd19.android.ui.uploadData

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.ascolto.onlus.geocrowd19.android.R
import org.koin.androidx.viewmodel.ext.android.getViewModel

class InsertUploadDataCodeFragment : Fragment(R.layout.insert_upload_data_code_fragment) {
    private lateinit var viewModel: InsertUploadDataCodeViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = getViewModel()
        // TODO: Use the ViewModel
    }

}
