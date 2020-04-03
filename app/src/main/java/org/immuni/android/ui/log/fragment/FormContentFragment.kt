package org.immuni.android.ui.log.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bendingspoons.base.extensions.animateHide
import com.bendingspoons.base.extensions.animateShow
import org.immuni.android.ui.log.LogViewModel
import org.immuni.android.ui.log.model.FormModel
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.base.utils.ScreenUtils
import org.immuni.android.R
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
            validate(save = true)
            viewModel.onNextTap(questionId)
        }

        prevButton.setOnClickListener {
            viewModel.onPrevTap(questionId)
        }

        // on scrolling the top mask hide/show
        view.findViewById<NestedScrollView>(R.id.scrollView)?.setOnScrollChangeListener { v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            updateTopMask(scrollY)
        }

        updateTopMask(view.findViewById<NestedScrollView>(R.id.scrollView)?.scrollY ?: 0)
    }

    // on scroll the top mask raise its elevation generating a shadow effect while scrolling up

    fun updateTopMask(scrollY: Int) {
        val dp = ScreenUtils.convertDpToPixels(requireContext(), 8).toFloat()
        val elevation = resources.getDimension(R.dimen.top_scroll_mask_elevation)
        //this.view?.findViewById<View>(R.id.topMask)?.alpha = 1f//0f + scrollY/dp
        this.view?.findViewById<View>(R.id.topMask)?.elevation = (elevation * (0f + scrollY/dp).coerceIn(0f, 1f))
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
