/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.ui.certificate

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import kotlinx.android.synthetic.main.certificate_dgc.*
import org.koin.android.ext.android.get
import kotlin.math.abs

class CertificateDGCFragment : Fragment(R.layout.certificate_dgc), CertificateDGCClickListener {

    private lateinit var certificateDGCAdapter: CertificateDGCAdapter
    private lateinit var userManager: UserManager
    private lateinit var listGreenCertificate: MutableList<GreenCertificateUser>
    private lateinit var settingsManager: ConfigurationSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey("uid") == true) {
            val action = CertificateDGCFragmentDirections.actionGreenCertificateTab(arguments!!["uid"] as String)
            arguments!!.remove("uid")
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background_darker))

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())

            pageTitle?.alpha = 1 - ratio
            toolbarTitle?.alpha = ratio
            toolbarSeparator?.alpha = ratio
        })

        userManager = get()
        settingsManager = get()

        knowMore.setSafeOnClickListener {
            val action = CertificateDGCFragmentDirections.actionCertificateDgcKnowMore()
            findNavController().navigate(action)
        }

        listGreenCertificate = userManager.user.value?.greenPass!!.asReversed()

        if (listGreenCertificate.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            view_fully.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            view_fully.visibility = View.VISIBLE
        }

        certificateDGCAdapter = CertificateDGCAdapter(
            requireContext(),
            settingsManager,
            this
        )
        certificateRecycler.adapter = certificateDGCAdapter
        certificateDGCAdapter.data = listGreenCertificate

        getDGCButton.setOnClickListener {
            val action = CertificateDGCFragmentDirections.actionGreenCertificateNav()
            findNavController().navigate(action)
        }
    }

    override fun onClick(uid: String) {
        val action = CertificateDGCFragmentDirections.actionGreenCertificateTab(uid)
        findNavController().navigate(action)
    }

    fun openGenerateGreenPass() {
        val action = CertificateDGCFragmentDirections.actionGreenCertificateNav()
        findNavController().navigate(action)
        // close the previous dialog
        // important keep here, after navigating
        findNavController().popBackStack()
    }

}
