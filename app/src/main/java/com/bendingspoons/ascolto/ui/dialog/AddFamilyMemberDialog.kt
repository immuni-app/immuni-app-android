package com.bendingspoons.ascolto.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bendingspoons.ascolto.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddFamilyMemberDialog: BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.family_member_add_dialog, container, false)
    }
}