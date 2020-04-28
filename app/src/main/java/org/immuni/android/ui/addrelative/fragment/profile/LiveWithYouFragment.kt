package org.immuni.android.ui.addrelative.fragment.profile

import android.os.Bundle
import android.view.View
import org.immuni.android.R
import com.bendingspoons.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.add_relative_live_with_you_fragment.*
import org.immuni.android.ui.addrelative.RelativeInfo

class LiveWithYouFragment : RelativeContentFragment(R.layout.add_relative_live_with_you_fragment) {
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
            viewModel.onAddRelativeComplete()
        }
    }

    override fun onUserInfoUpdate(userInfo: RelativeInfo) {
        updateUI(userInfo)
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
            viewModel.updateUserInfo(it.copy(sameHouse = answer))
        }
    }

    private fun updateUI(userInfo: RelativeInfo) {
        var name = userInfo.nickname?.humanReadable(requireContext(), userInfo.gender!!) ?: requireContext().getString(R.string.this_person)
        question.text = String.format(requireContext().getString(R.string.live_with_you_title), name)
        description.text = String.format(requireContext().getString(R.string.live_with_you_message), name)

        when(userInfo.sameHouse) {
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
