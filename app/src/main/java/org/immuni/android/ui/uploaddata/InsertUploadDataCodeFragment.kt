package org.immuni.android.ui.uploaddata

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.immuni.android.base.extensions.hideKeyboard
import org.immuni.android.base.extensions.invisible
import org.immuni.android.base.extensions.showKeyboard
import org.immuni.android.base.extensions.visible
import kotlinx.android.synthetic.main.insert_upload_data_code_fragment.*
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.close
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.okButton
import kotlinx.android.synthetic.main.where_to_find_the_code_fragment.title
import org.immuni.android.R
import org.immuni.android.base.extensions.loading
import org.immuni.android.util.ProgressDialogFragment
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class InsertUploadDataCodeFragment : Fragment(R.layout.insert_upload_data_code_fragment) {

    private lateinit var viewModel: UploadDataViewModel

    override fun onResume() {
        super.onResume()
        codeTextField.showKeyboard()
    }

    override fun onPause() {
        super.onPause()
        codeTextField.hideKeyboard()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = getSharedViewModel()

        okButton.isEnabled = false
        codeTextField.filters = arrayOf(InputFilter.AllCaps())

        close.setOnClickListener {
            close()
        }

        okButton.setOnClickListener {
            viewModel.exportData(codeTextField.text.toString())
        }

        title.setOnClickListener {
            val action = InsertUploadDataCodeFragmentDirections.actionGlobalInstruction()
            findNavController().navigate(action)
        }

        codeTextField.filters = mutableListOf<InputFilter>().apply {
            addAll(codeTextField.filters)
            add(LengthFilter(8))
        }.toTypedArray()
        codeTextField.doOnTextChanged { text, _, _, _ ->
            validateCode(text.toString())
            error.invisible()
        }

        // add info icon at the end of the page title
        val text = "${title.text}   "
        val spannable = SpannableString(text)
        spannable.setSpan(
            ImageSpan(requireContext(), R.drawable.ic_info_medium, DynamicDrawableSpan.ALIGN_BASELINE),
            text.length-1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        title.text = spannable

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                activity?.loading(it, ProgressDialogFragment())
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                error.visible()
            }
        })

        viewModel.success.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val action = UploadDataSuccessFragmentDirections.actionGlobalSuccess()
                findNavController().navigate(action)
            }
        })
    }

    private fun validateCode(code: String) {
        val isCodeValid = code.length == 8
        error.visibility = if (isCodeValid) View.VISIBLE else View.GONE
        okButton.isEnabled = isCodeValid
    }

    private fun close() {
        activity?.finish()
    }
}
