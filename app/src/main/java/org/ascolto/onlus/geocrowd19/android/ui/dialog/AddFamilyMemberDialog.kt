package org.ascolto.onlus.geocrowd19.android.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.toast
import kotlinx.android.synthetic.main.family_member_add_dialog.*

class AddFamilyMemberDialog: FullScreenBottomSheetDialogFragment()  {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.family_member_add_dialog, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        back.setOnClickListener {
            dismiss()
        }

        goAhead.setOnClickListener {
            dismiss()
        }

        addMember.setOnClickListener {
            toast("TODO go to add family member")
        }
    }
}