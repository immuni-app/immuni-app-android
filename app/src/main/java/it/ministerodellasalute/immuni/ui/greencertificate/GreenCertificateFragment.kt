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

package it.ministerodellasalute.immuni.ui.greencertificate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.ministerodellasalute.immuni.GreenCertificateDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.User
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import it.ministerodellasalute.immuni.util.ImageUtils
import kotlin.math.abs
import kotlinx.android.synthetic.main.green_certificate.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel

class GreenCertificateFragment : Fragment(R.layout.green_certificate), ConfirmationDialogListener {

    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback
    private lateinit var userManager: UserManager

    private lateinit var greenPassBase64: String
    private lateinit var filename: String
    var positionToDelete: Int? = null

    lateinit var greenPassAdapter: GreenPassAdapter
    private lateinit var viewModel: GreenCertificateViewModel

    companion object {
        const val DELETE_QR = 200
        const val GO_TO_SETTINGS = 201
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(
            resources.getColor(
                R.color.background_darker,
                null
            )
        )

        userManager = get()
        viewModel = getViewModel()

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            toolbarSeparator?.alpha = ratio
        })

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
        }

        greenPassAdapter = GreenPassAdapter(
            context = requireContext(),
            fragment = this@GreenCertificateFragment,
            viewModel = viewModel
        )
        greenPassAdapter.data = userManager.user.value?.greenPass!!

        with(viewpager) {
            adapter = greenPassAdapter
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        TabLayoutMediator(tabLayoutDot, viewpager) { tab, position ->
            // Some implementation
        }.attach()

        navigationIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        getDGCButton.setOnClickListener {
            val action = GreenCertificateDirections.actionGenerateGC()
            findNavController().navigate(action)
        }
        setVisibilityLayout()
    }

    override fun onDialogPositive(requestCode: Int) {
        if (requestCode == DELETE_QR) {
            val user = userManager.user
            user.value?.greenPass!!.removeAt(positionToDelete!!)
            userManager.save(
                User(
                    region = user.value?.region!!,
                    province = user.value?.province!!,
                    greenPass = user.value?.greenPass!!
                )
            )
            greenPassAdapter.notifyItemRemoved(positionToDelete!!)
            greenPassAdapter.notifyItemRangeChanged(positionToDelete!!, greenPassAdapter.itemCount)
            setVisibilityLayout()
            positionToDelete = null
        } else {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    override fun onDialogNegative(requestCode: Int) {
        // Pass
    }

    private fun setVisibilityLayout() {
        if (userManager.user.value?.greenPass!!.isEmpty()) {
            noQrCodeLayout.visibility = View.VISIBLE
            qrCodeLayout.visibility = View.GONE
        } else {
            noQrCodeLayout.visibility = View.GONE
            qrCodeLayout.visibility = View.VISIBLE
        }
    }

    fun checkPermission(greenPassBase64: String, filename: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            downloadImage(greenPassBase64, filename)
        } else {
            this.greenPassBase64 = greenPassBase64
            this.filename = filename
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 200)
        }
    }

    private fun downloadImage(greenPassBase64: String, filename: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ImageUtils.downloadQ(
                context = requireContext(),
                bitmap = ImageUtils.convert(greenPassBase64),
                filename = filename
            )
        else
            ImageUtils.downloadLegacy(
                bitmap = ImageUtils.convert(greenPassBase64),
                filename = filename
            )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
            downloadImage(greenPassBase64, filename)
        } else {
            openConfirmationDialog(
                positiveButton = getString(R.string.permission_go_to_settings),
                negativeButton = getString(R.string.cancel),
                message = getString(R.string.permission_needed_permission_message),
                title = getString(R.string.permission_need_permission_title),
                cancelable = true,
                requestCode = GO_TO_SETTINGS
            )
        }
    }
}
