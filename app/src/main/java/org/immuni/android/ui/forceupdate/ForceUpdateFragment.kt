package org.immuni.android.ui.forceupdate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bendingspoons.base.extensions.invisible
import com.bendingspoons.base.extensions.visible
import org.immuni.android.R
import kotlinx.android.synthetic.main.force_update_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.bendingspoons.base.extensions.loading
import org.immuni.android.util.ProgressDialogFragment
import org.koin.androidx.viewmodel.ext.android.getViewModel

class ForceUpdateFragment : Fragment(R.layout.force_update_fragment) {
    private lateinit var viewModel: ForceUpdateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // deny back press to force update
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it, ProgressDialogFragment())
        })

        viewModel.downloading.observe(viewLifecycleOwner, Observer {
            title.text = "Download in corso..."
            message.text = "Controlla l'avanzamento del download nella barra delle notifiche."
            //update.isEnabled = false
            //update.alpha = 0.3f
            retry.visible()
            retry.isEnabled = true
            update.invisible()
            update.isEnabled = false
        })

        update.setOnClickListener {
            startUpdate()
        }

        retry.setOnClickListener {
            resetUI()
        }
    }

    private fun resetUI() {
        retry.invisible()
        retry.isEnabled = false
        update.visible()
        update.isEnabled = true
        title.text = getString(R.string.app_update_title)
        message.text = getString(R.string.app_update_message)
    }

    private fun startUpdate() {
        context?.let { viewModel.goToPlayStoreAppDetails(this) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions.isNotEmpty() && permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startUpdate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 20999 && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch {
                runCatching {
                    delay(500)
                    startUpdate()
                }
            }

        }
    }
}
