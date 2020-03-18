package com.bendingspoons.ascolto.ui.log.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.ui.log.LogViewModel
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

abstract class FormContentFragment(@LayoutRes val layout: Int) : Fragment(layout) {
    private lateinit var viewModel: LogViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        nextButton.isEnabled = false

        viewModel.formModel.observe(viewLifecycleOwner, Observer { info ->
            onFormModelUpdate(info)
        })

        nextButton.setOnClickListener {
            viewModel.onNextTap()
        }

        prevButton.setOnClickListener {
            viewModel.onPrevTap()
        }
    }

    protected abstract val nextButton: Button
    protected abstract val prevButton: ImageView
    protected abstract val question: TextView
    protected abstract val description: TextView

    abstract fun onFormModelUpdate(model: FormModel)

    abstract fun validate(): Boolean

    fun updateFormModel(model: FormModel) {
        viewModel.updateFormModel(model)
    }

    fun formModel(): FormModel? {
        return viewModel.formModel()
    }

    fun updateEditText(editText: EditText, text: String) {
        if(editText.text.toString() != text) editText.setText(text)
        editText.setSelection((editText.length()).coerceAtLeast(0))
    }
}
