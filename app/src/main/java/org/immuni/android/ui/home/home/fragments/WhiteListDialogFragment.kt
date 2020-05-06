package org.immuni.android.ui.home.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.whitelist_dialog.*
import org.immuni.android.R
import org.immuni.android.managers.ExposureNotificationManager
import org.immuni.android.ui.dialog.FullScreenDialogDarkFragment

class WhiteListDialogFragment: FullScreenDialogDarkFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.whitelist_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openList.setOnClickListener {
            ExposureNotificationManager.startChangeBatteryOptimization(requireContext())
        }
    }
}