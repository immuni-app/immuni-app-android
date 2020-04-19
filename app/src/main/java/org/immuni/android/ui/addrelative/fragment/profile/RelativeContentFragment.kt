package org.immuni.android.ui.addrelative.fragment.profile

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bendingspoons.base.extensions.animateTranslationY
import com.bendingspoons.base.extensions.gone
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import org.immuni.android.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.base.utils.ScreenUtils
import org.immuni.android.R
import org.immuni.android.ui.addrelative.AddRelativeViewModel
import org.immuni.android.ui.addrelative.RelativeInfo
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

abstract class RelativeContentFragment(@LayoutRes val layout: Int) : Fragment(layout) {
    protected lateinit var viewModel: AddRelativeViewModel
    protected var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position") ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        nextButton.isEnabled = false

        prevButton.setOnClickListener {
            viewModel.onPrevTap()
        }

        viewModel.partialUserInfo.observe(viewLifecycleOwner, Observer { info ->
            onUserInfoUpdate(info)
        })

        // on scrolling the top mask hide/show
        view.findViewById<NestedScrollView>(R.id.scrollView)?.setOnScrollChangeListener { v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            updateTopMask(scrollY)
        }

        updateTopMask(view.findViewById<NestedScrollView>(R.id.scrollView)?.scrollY ?: 0)
    }

    override fun onResume() {
        super.onResume()
        updateTopMask(this.view?.findViewById<NestedScrollView>(R.id.scrollView)?.scrollY ?: 0, true)
    }

    fun updateTopMask(scrollY: Int, animate: Boolean = false) {
        val dp = ScreenUtils.convertDpToPixels(requireContext(), 8).toFloat()
        //val elevation = resources.getDimension(R.dimen.top_scroll_mask_elevation)
        //this.view?.findViewById<View>(R.id.topMask)?.alpha = 1f//0f + scrollY/dp
        //this.view?.findViewById<View>(R.id.topMask)?.elevation = (elevation * (0f + scrollY/dp).coerceIn(0f, 1f))

        this.view?.findViewById<View>(R.id.gradientTop)?.alpha = (0f + scrollY/dp).coerceIn(0f, 1f)

        val maxScrollUpCard = ScreenUtils.convertDpToPixels(requireContext(), 32).toFloat()
        val maxScrollUpProgressBar = ScreenUtils.convertDpToPixels(requireContext(), 20).toFloat()
        if(animate) {
            this.view?.findViewById<View>(R.id.topMask)?.animateTranslationY(-(scrollY.toFloat().coerceAtMost(maxScrollUpCard)), 250)
            activity?.findViewById<View>(R.id.progress)?.animateTranslationY(-(scrollY.toFloat().coerceAtMost(maxScrollUpProgressBar)), 250)
        } else {
            this.view?.findViewById<View>(R.id.topMask)?.translationY = -(scrollY.toFloat().coerceAtMost(maxScrollUpCard))
            activity?.findViewById<View>(R.id.progress)?.translationY = -(scrollY.toFloat().coerceAtMost(maxScrollUpProgressBar))
        }
    }

    protected abstract val nextButton: View
    protected abstract val prevButton: View

    abstract fun onUserInfoUpdate(userInfo: RelativeInfo)

    fun updateUserInfo(userInfo: RelativeInfo) {
        viewModel.updateUserInfo(userInfo)
    }

    fun userInfo(): RelativeInfo? {
        return viewModel.userInfo()
    }

    fun updateEditText(editText: EditText, text: String) {
        if(editText.text.toString() != text) editText.setText(text)
        editText.setSelection((editText.length()).coerceAtLeast(0))
    }
}
