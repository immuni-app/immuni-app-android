package org.immuni.android.ui.home.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import kotlinx.android.synthetic.main.home_diary_dialog.*
import org.immuni.android.R
import org.immuni.android.ui.dialog.FullScreenDialogDarkFragment

class HomeDiaryDialogFragment : FullScreenDialogDarkFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_diary_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        button.setOnClickListener {
        }
    }
}
