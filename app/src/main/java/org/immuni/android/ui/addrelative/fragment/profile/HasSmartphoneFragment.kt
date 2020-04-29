package org.immuni.android.ui.addrelative.fragment.profile

import android.os.Bundle
import android.view.View
import org.immuni.android.R
import org.immuni.android.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.add_relative_already_registered_fragment.*
import org.immuni.android.ui.addrelative.RelativeInfo

class HasSmartphoneFragment : RelativeContentFragment(R.layout.add_relative_has_smartphone_fragment) {
    override val nextButton: View
        get() = next
    override val prevButton: View
        get() = back

    override fun onResume() {
        super.onResume()
        this.view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yes.setOnClickListener {
            validate(true)
        }

        no.setOnClickListener {
            validate(true)
        }

        nextButton.setOnClickListener {
            if (yes.isChecked) {
                viewModel.onNextTap(CanAddInfoHimselfFragment::class.java)
            } else {
                viewModel.onNextTap(AlreadyRegisteredFragment::class.java)
            }
        }
    }

    override fun onUserInfoUpdate(userInfo: RelativeInfo) {
        updateUI(userInfo.hasSmartphone)
        validate(false)
    }

    private fun validate(updateModel: Boolean = true): Boolean {
        val valid = yes.isChecked || no.isChecked
        nextButton.isEnabled = valid
        if(valid && updateModel) updateModel(when {
            yes.isChecked -> true
            else -> false
        })
        return valid
    }

    private fun updateModel(answer: Boolean) {
        viewModel.userInfo()?.let {
            viewModel.updateUserInfo(it.copy(hasSmartphone = answer))
        }
    }

    private fun updateUI(answer: Boolean?) {
        when(answer) {
            true -> {
                yes.isChecked = true
                no.isChecked = false
            }
            false -> {
                yes.isChecked = false
                no.isChecked = true
            }
            else -> {
                yes.isChecked = false
                no.isChecked = false
            }
        }
    }
}
