package org.ascolto.onlus.geocrowd19.android.ui.uploadData

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.insert_upload_data_code_fragment.*
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.close
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.okButton
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.title
import org.ascolto.onlus.geocrowd19.android.R
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class InsertUploadDataCodeFragment : Fragment(R.layout.insert_upload_data_code_fragment) {
    private lateinit var viewModel: UploadDataViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = getSharedViewModel()

        okButton.isEnabled = false

        close.setOnClickListener {
            close()
        }

        okButton.setOnClickListener {
            // TODO: retrieve userId
            viewModel.exportData(userId, codeTextField.text.toString())
        }

        title.setOnClickListener {
            // TODO: Navigate to WhereToFindTheCodeFragment
        }

        codeTextField.doOnTextChanged { text, _, _, _ ->
            validateCode(text.toString())
        }
    }

    private fun validateCode(code: String) {
        val isCodeValid = true // TODO: insert code validation code
        error.visibility = if (isCodeValid) View.VISIBLE else View.GONE
        okButton.isEnabled = isCodeValid
    }

    private fun close() {
        activity?.finish()
    }
}
