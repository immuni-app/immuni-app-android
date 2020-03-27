package org.ascolto.onlus.geocrowd19.android.ui.log.fragment

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
import org.ascolto.onlus.geocrowd19.android.ui.log.LogViewModel
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

abstract class FormContentFragment(@LayoutRes val layout: Int) : Fragment(layout) {
    lateinit var viewModel: LogViewModel
    protected var position: Int = 0
    protected var questionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position") ?: 0
        questionId = arguments?.getString("questionId") ?: "0"
    }

    override fun onResume() {
        super.onResume()
        view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        nextButton.isEnabled = false
        prevButton.isEnabled = viewModel.canGoBack()

        viewModel.formModel.observe(viewLifecycleOwner, Observer { form ->
            onFormModelUpdate(form)
        })

        nextButton.setOnClickListener {
            viewModel.onNextTap(questionId)
        }

        prevButton.setOnClickListener {
            viewModel.onPrevTap(questionId)
        }
    }

    protected abstract val nextButton: Button
    protected abstract val prevButton: ImageView
    protected abstract val questionText: TextView
    protected abstract val descriptionText: TextView

    abstract fun onFormModelUpdate(model: FormModel)

    abstract fun validate(save: Boolean = true): Boolean

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
