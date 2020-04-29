package org.immuni.android.ui.log.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.immuni.android.ui.log.LogViewModel
import org.immuni.android.ui.log.model.FormModel
import org.immuni.android.extensions.utils.ScreenUtils
import org.immuni.android.R
import org.immuni.android.extensions.activity.setLightStatusBarFullscreen
import org.immuni.android.extensions.view.gone
import org.immuni.android.extensions.view.hideKeyboard
import org.immuni.android.extensions.view.visible
import org.immuni.android.models.colorResource
import org.immuni.android.models.iconResource
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        nextButton.isEnabled = false
        prevButton.isEnabled = viewModel.canGoBack()

        viewModel.formModel.observe(viewLifecycleOwner, Observer { form ->
            onFormModelUpdate(form)
        })

        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if(user.isMain) {
                view.findViewById<ConstraintLayout>(R.id.userInfoCard)?.gone()
            } else {
                view.findViewById<ConstraintLayout>(R.id.userInfoCard)?.visible()
                val themeColor = ContextCompat.getColor(
                    requireContext(),
                    colorResource(
                        viewModel.deviceId,
                        viewModel.userIndex!!
                    )
                )
                // icon and color
                view.findViewById<ImageView>(R.id.userIcon)?.setImageResource(
                    user.gender.iconResource(viewModel.deviceId, viewModel.userIndex?:0)
                )
                view.findViewById<TextView>(R.id.questionFor)?.setTextColor(themeColor)
                view.findViewById<TextView>(R.id.questionForLabel)?.setTextColor(themeColor)
                // name
                view.findViewById<TextView>(R.id.questionFor)?.text = user.nickname?.humanReadable(requireContext(), user.gender)
            }
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

    override fun onResume() {
        super.onResume()
        view?.hideKeyboard()
        updateTopMask(this.view?.findViewById<NestedScrollView>(R.id.scrollView)?.scrollY ?: 0)
    }

    fun updateTopMask(scrollY: Int) {
        val dp = ScreenUtils.convertDpToPixels(requireContext(), 8).toFloat()
        this.view?.findViewById<View>(R.id.gradientTop)?.alpha = (0f + scrollY/dp).coerceIn(0f, 1f)
        val maxScrollUpCard = ScreenUtils.convertDpToPixels(requireContext(), 32).toFloat()
        this.view?.findViewById<View>(R.id.topMask)?.translationY = -(scrollY.toFloat().coerceAtMost(maxScrollUpCard))
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
