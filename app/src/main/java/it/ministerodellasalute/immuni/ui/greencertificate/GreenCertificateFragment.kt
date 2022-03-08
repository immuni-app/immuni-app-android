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
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import it.ministerodellasalute.immuni.logic.user.models.User
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import it.ministerodellasalute.immuni.util.ImageUtils
import kotlinx.android.synthetic.main.green_certificate.*
import kotlinx.android.synthetic.main.green_certificate_tab.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.math.abs

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
        const val INFORMATION_ORDER = 202
        const val REPLACE_DGC = 203
        const val ALERT_ADD = 204
        const val ALERT_REMOVE = 205
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(
            resources.getColor(
                R.color.background_darker,
                null
            )
        )
        val argument = navArgs<GreenCertificateFragmentArgs>()

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

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentView =
                    (viewpager[0] as RecyclerView).layoutManager?.findViewByPosition(position)
                currentView?.post {
                    val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                        currentView.width,
                        View.MeasureSpec.EXACTLY
                    )
                    val hMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    currentView.measure(wMeasureSpec, hMeasureSpec)

                    if (viewpager.layoutParams.height != currentView.measuredHeight) {
                        viewpager.layoutParams = (viewpager.layoutParams).also { lp ->
                            lp.height = currentView.measuredHeight
                        }
                    }
                }
            }
        }

        greenPassAdapter = GreenPassAdapter(
            context = requireContext(),
            fragment = this@GreenCertificateFragment,
            viewModel = viewModel,
            userManager = userManager
        )
        greenPassAdapter.data = userManager.user.value?.greenPass!!.asReversed()

        with(viewpager) {
            adapter = greenPassAdapter
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        if (arguments?.containsKey("greenCertificateSelected") == true) {
            if (viewpager != null) {
                viewpager.post {
                    viewpager.setCurrentItem(
                        findCurrentItemFromUid(uid = argument.value.greenCertificateSelected),
                        true
                    )
                    arguments!!.remove("greenCertificateSelected")
                }
            }
        }


        TabLayoutMediator(tabLayoutDot, viewpager) { tab, position ->
            // Some implementation
        }.attach()

        navigationIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        informationOrder()
    }

    override fun onDialogPositive(requestCode: Int, argument: String?) {
        when (requestCode) {
            DELETE_QR -> {
                val user = userManager.user
                if (user.value?.greenPass!!.asReversed()[positionToDelete!!].addedHomeDgc) {
                    userManager.setShowDGCHome(show = false)
                }
                user.value?.greenPass!!.asReversed().removeAt(positionToDelete!!)
                userManager.save(
                    User(
                        region = user.value?.region!!,
                        province = user.value?.province!!,
                        greenPass = user.value?.greenPass!!
                    )
                )
                greenPassAdapter.notifyItemRemoved(positionToDelete!!)
                greenPassAdapter.notifyItemRangeChanged(
                    positionToDelete!!,
                    greenPassAdapter.itemCount
                )
                positionToDelete = null
                if (user.value?.greenPass!!.isEmpty())
                    findNavController().popBackStack()
            }
            INFORMATION_ORDER -> {
                userManager.setShowModalDGC(show = false)
            }
            GO_TO_SETTINGS -> {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            ALERT_ADD -> {
                val user = userManager.user
                val listDgcUpdated =
                    editDGC(uid = argument!!, removeCurrent = false, removeOther = false)
                userManager.setShowDGCHome(show = true)
                userManager.save(
                    User(
                        region = user.value?.region!!,
                        province = user.value?.province!!,
                        greenPass = listDgcUpdated
                    )
                )
                greenPassAdapter.notifyDataSetChanged()
            }
            ALERT_REMOVE -> {
                val user = userManager.user
                val listDgcUpdated =
                    editDGC(uid = argument!!, removeCurrent = true, removeOther = false)
                userManager.setShowDGCHome(show = false)
                userManager.save(
                    User(
                        region = user.value?.region!!,
                        province = user.value?.province!!,
                        greenPass = listDgcUpdated
                    )
                )
                greenPassAdapter.notifyDataSetChanged()
            }
            REPLACE_DGC -> {
                val user = userManager.user
                val listDgcUpdated =
                    editDGC(uid = argument!!, removeCurrent = false, removeOther = true)
                userManager.setShowDGCHome(show = true)
                userManager.save(
                    User(
                        region = user.value?.region!!,
                        province = user.value?.province!!,
                        greenPass = listDgcUpdated
                    )
                )
                greenPassAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDialogNegative(requestCode: Int) {
        // Pass
    }

    private fun informationOrder() {
        if (userManager.showModalDGC.value && userManager.user.value!!.greenPass.size > 1) {
            openConfirmationDialog(
                positiveButton = getString(R.string.green_certificate_modal_order_button),
                negativeButton = null,
                message = getString(R.string.green_certificate_modal_order_message),
                title = getString(R.string.green_certificate_modal_order_title),
                cancelable = false,
                requestCode = INFORMATION_ORDER
            )
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

    private fun findCurrentItemFromUid(uid: String?): Int {
        if (uid.isNullOrBlank()) {
            return 0
        }
        var i = 0;
        var indexSelected = 0
        loop@ for (greenPass in userManager.user.value?.greenPass!!.asReversed()) {
            when (true) {
                greenPass.data?.vaccinations != null -> {
                    if (uid === greenPass.data?.vaccinations?.get(0)?.certificateIdentifier) {
                        indexSelected = i
                        break@loop
                    }
                    i++
                }
                greenPass.data?.tests != null -> {
                    if (uid === greenPass.data?.tests?.get(0)?.certificateIdentifier) {
                        indexSelected = i
                        break@loop
                    }
                    i++
                }
                greenPass.data?.recoveryStatements != null -> {
                    if (uid === greenPass.data?.recoveryStatements?.get(0)?.certificateIdentifier) {
                        indexSelected = i
                        break@loop
                    }
                    i++
                }
                greenPass.data?.exemptions != null -> {
                    if (uid === greenPass.data?.exemptions?.get(0)?.certificateIdentifier) {
                        indexSelected = i
                        break@loop
                    }
                    i++
                }
            }
        }
        return indexSelected
    }

    private fun editDGC(
        uid: String,
        removeCurrent: Boolean,
        removeOther: Boolean
    ): MutableList<GreenCertificateUser> {
        val listDGC = userManager.user.value?.greenPass!!
        loop@ for (greenPass in listDGC) {
            when (true) {
                greenPass.data?.vaccinations != null -> {
                    if (uid === greenPass.data?.vaccinations?.get(0)?.certificateIdentifier) {
                        greenPass.addedHomeDgc = !removeCurrent
                        if (!removeOther) {
                            break@loop
                        }
                    } else {
                        if (removeOther) {
                            greenPass.addedHomeDgc = false
                        }
                    }
                }
                greenPass.data?.tests != null -> {
                    if (uid === greenPass.data?.tests?.get(0)?.certificateIdentifier) {
                        greenPass.addedHomeDgc = !removeCurrent
                        if (!removeOther) {
                            break@loop
                        }
                    } else {
                        if (removeOther) {
                            greenPass.addedHomeDgc = false
                        }
                    }
                }
                greenPass.data?.recoveryStatements != null -> {
                    if (uid === greenPass.data?.recoveryStatements?.get(0)?.certificateIdentifier) {
                        greenPass.addedHomeDgc = !removeCurrent
                        if (!removeOther) {
                            break@loop
                        }
                    } else {
                        if (removeOther) {
                            greenPass.addedHomeDgc = false
                        }
                    }
                }
                greenPass.data?.exemptions != null -> {
                    if (uid === greenPass.data?.exemptions?.get(0)?.certificateIdentifier) {
                        greenPass.addedHomeDgc = !removeCurrent
                        if (!removeOther) {
                            break@loop
                        }
                    } else {
                        if (removeOther) {
                            greenPass.addedHomeDgc = false
                        }
                    }
                }
            }
        }
        return listDGC
    }
}
