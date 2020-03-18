package com.bendingspoons.ascolto.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.ascolto.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

abstract class ProfileContentFragment(@LayoutRes val layout: Int) : Fragment(layout) {
    protected lateinit var viewModel: OnboardingViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        nextButton.isEnabled = true

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
