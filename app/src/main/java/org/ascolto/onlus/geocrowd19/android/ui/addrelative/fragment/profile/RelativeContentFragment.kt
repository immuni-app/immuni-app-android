package org.ascolto.onlus.geocrowd19.android.ui.addrelative.fragment.profile

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingUserInfo
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.AddRelativeViewModel
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.RelativeInfo
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

        viewModel.partialUserInfo.observe(viewLifecycleOwner, Observer { info ->
            onUserInfoUpdate(info)
        })
    }

    protected abstract val nextButton: View

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
