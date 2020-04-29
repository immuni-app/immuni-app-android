package org.immuni.android.ui.home.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.immuni.android.R
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.extensions.activity.setLightStatusBarFullscreen
import org.immuni.android.ids.IdsManager
import org.immuni.android.networking.Networking
import kotlinx.android.synthetic.main.data_handling_fragment.*
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class DataHandlingFragment : Fragment(R.layout.data_handling_fragment) {

    private lateinit var viewModel: HomeSharedViewModel
    val networking: Networking<ImmuniSettings, ImmuniMe> by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        privacyPolicyButton.setOnClickListener {
            viewModel.onPrivacyPolicyClick()
        }

        restoreDataButton.setOnClickListener {
            activity?.let {
                recoverDataEmail(it)
            }
        }

        delete.setOnClickListener {
            activity?.let {
                deleteDataEmail(it)
            }
        }

        back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun recoverDataEmail(activity: Activity) {
        val ctx = activity.applicationContext
        val email = networking.settings()?.recoverDataEmail

        val ids: IdsManager by inject()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_name))
            putExtra(
                Intent.EXTRA_TEXT,
                String.format("${ctx.getString(R.string.recover_data_email_message)}", ids.id.id)
            )
        }

        activity.startActivity(Intent.createChooser(intent, ctx.getString(R.string.choose_an_app_to_contact_us)))
    }

    fun deleteDataEmail(activity: Activity) {
        val ctx = activity.applicationContext
        val email = networking.settings()?.deleteDataEmail

        val ids: IdsManager by inject()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_name))
            putExtra(
                Intent.EXTRA_TEXT,
                String.format("${ctx.getString(R.string.delete_data_email_message)}", ids.id.id)
            )
        }

        activity.startActivity(Intent.createChooser(intent, ctx.getString(R.string.choose_an_app_to_contact_us)))
    }
}
