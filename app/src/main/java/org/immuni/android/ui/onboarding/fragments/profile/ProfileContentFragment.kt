package org.immuni.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import org.immuni.android.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

abstract class ProfileContentFragment(@LayoutRes val layout: Int) : Fragment(layout) {
    protected lateinit var viewModel: OnboardingViewModel
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

        viewModel.partialUserInfo.observe(viewLifecycleOwner, Observer { info ->
            onUserInfoUpdate(info)
        })

        nextButton.setOnClickListener {
            viewModel.onNextTap()
        }
    }

    protected abstract val nextButton: View

    abstract fun onUserInfoUpdate(userInfo: OnboardingUserInfo)

    fun updateUserInfo(userInfo: OnboardingUserInfo) {
        viewModel.updateUserInfo(userInfo)
    }

    fun userInfo(): OnboardingUserInfo? {
        return viewModel.userInfo()
    }

    fun updateEditText(editText: EditText, text: String) {
        if(editText.text.toString() != text) editText.setText(text)
        editText.setSelection((editText.length()).coerceAtLeast(0))
    }
}
