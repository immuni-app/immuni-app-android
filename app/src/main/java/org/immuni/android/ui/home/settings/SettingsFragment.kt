package org.immuni.android.ui.home.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.settings_fragment.*
import org.immuni.android.R
import org.immuni.android.data.SettingsDataSource
import org.immuni.android.extensions.activity.setLightStatusBarFullscreen
import org.immuni.android.ui.home.HomeSharedViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class SettingsFragment : Fragment(R.layout.settings_fragment) {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        dataLoadButton.setOnClickListener {
            //viewModel.onTosClick()
        }

        dataRecoveryButton.setOnClickListener {
//            val action = SettingsFragmentDirections.actionGlobalDataHandling()
//            findNavController().navigate(action)
        }

        dataDeleteButton.setOnClickListener {

        }
    }

    fun contactUs(activity: Activity) {

        val settings: SettingsDataSource by inject()
        val email = settings.latestSettings()?.supportEmail

        val ctx = activity.applicationContext
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_name))
            putExtra(
                Intent.EXTRA_TEXT, ctx.getString(R.string.contact_us_email_message)
            )
        }

        activity.startActivity(
            Intent.createChooser(
                intent,
                ctx.getString(R.string.choose_an_app_to_contact_us)
            )
        )
    }
}
