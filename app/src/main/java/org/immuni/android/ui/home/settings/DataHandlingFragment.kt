package org.immuni.android.ui.home.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.immuni.android.R
import org.immuni.android.ui.home.HomeSharedViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.concierge.ConciergeManager
import kotlinx.android.synthetic.main.data_handling_fragment.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class DataHandlingFragment : Fragment(R.layout.data_handling_fragment) {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        privacyPolicyButton.setOnClickListener {
            viewModel.onPrivacyPolicyClick()
        }

        restoreDataButton.setOnClickListener {
            activity?.let {
                contactUs(it)
            }
        }

        delete.setOnClickListener {
            activity?.let {
                contactUs(it)
            }
        }
    }

    fun contactUs(activity: Activity) {
        val ctx = activity.applicationContext
        val email = "example@bendingspoons.com"

        val concierge: ConciergeManager by inject()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_name))
            putExtra(
                Intent.EXTRA_TEXT, "${ctx.getString(R.string.delete_data_email_message)}\n" +
                        "\n---------------------\n" +
                        concierge.backupPersistentId.id + "\n" +
                        "---------------------")
        }

        activity.startActivity(Intent.createChooser(intent, ctx.getString(R.string.choose_an_app_to_contact_us)))
    }
}
