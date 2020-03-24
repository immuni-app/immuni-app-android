package org.ascolto.onlus.geocrowd19.android.ui.addrelative.fragment.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import org.ascolto.onlus.geocrowd19.android.R
import com.bendingspoons.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.add_relative_already_registered_fragment.*
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.RelativeInfo

class CanAddInfoHimselfFragment : RelativeContentFragment(R.layout.add_relative_can_add_info_fragment) {
    override val nextButton: View
        get() = next

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
                val action = RelativeProfileFragmentDirections.actionInterrupt(
                    "title",
                    "message"
                )
                findNavController().navigate(action)
            } else {
                viewModel.onNextTap(AlreadyRegisteredFragment::class.java)
            }
        }
    }

    override fun onUserInfoUpdate(userInfo: RelativeInfo) {
        updateUI(userInfo.canAddInfoHimself)
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
            viewModel.updateUserInfo(it.copy(canAddInfoHimself = answer))
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
