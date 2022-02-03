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

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.ministerodellasalute.immuni.GreenCertificateDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.loading
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.util.ProgressDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlinx.android.synthetic.main.generate_green_certificate.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel

class GenerateGreenCertificate : Fragment(R.layout.generate_green_certificate),
    ConfirmationDialogListener {

    companion object {
        var NAVIGATE_UP = false
        const val ALERT_CONFIRM_SAVE = 212
    }

    private lateinit var viewModel: GreenCertificateViewModel
    val builder = MaterialDatePicker.Builder.datePicker()
    private lateinit var materialDatePicker: MaterialDatePicker<Long>
    private lateinit var userManager: UserManager

    override fun onResume() {
        super.onResume()
        if (NAVIGATE_UP) {
            NAVIGATE_UP = false
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel()
        userManager = get()

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            toolbarSeparator?.alpha = ratio
        })

        navigationIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        generateGC.setOnClickListener {
            viewModel.genera(
                typeToken = listTypeToken.text.toString(),
                token = codeInput.text.toString(),
                healthInsurance = healthInsuranceCardInput.text.toString(),
                expiredHealthIDDate = expiredDateHealthIDDateInput.text.toString()
            )
        }

        knowMore.setOnClickListener {
            val action = GreenCertificateDirections.actionKnowMoreGreenCertificateDialog()
            findNavController().navigate(action)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            activity?.loading(it, ProgressDialogFragment(), Bundle().apply {
                putString(
                    ProgressDialogFragment.MESSAGE,
                    getString(R.string.get_dgc_loading)
                )
            })
        }

        viewModel.navigateToSuccessPage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { _ ->
                val action = GreenCertificateDirections.actionGenerateGreenCertificateSuccess()
                findNavController().navigate(action)
            }
        }

        viewModel.alertError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { listLabel ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(listLabel[0])
                    .setMessage(listLabel[1])
                    .setPositiveButton(getString(R.string.button_dialog_confirm)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        }

        setInput()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setInput() {
        val regexAlphaNum = "^[A-Z0-9]*$".toRegex()

        val typeTokenList = resources.getStringArray(R.array.type_token)
        val adapter = ArrayAdapter(requireContext(), R.layout.exposed_list_item, typeTokenList)
        listTypeToken.setAdapter(adapter)
        listTypeToken.setDropDownBackgroundDrawable(
            resources.getDrawable(
                R.drawable.dropdown_rounded,
                null
            )
        )

        listTypeToken.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                codeInput.text?.clear()
                codeInputLayout.isEnabled = true
                codeInputLayout.prefixText = when (position) {
                    0 -> {
                        codeLabel.text = getString(R.string.authcode_title)
                        codeInput.setHint(R.string.code_placeholder)
                        ""
                    }

                    1 -> {
                        codeLabel.text = getString(R.string.nrfe_title)
                        codeInput.setHint(R.string.code_placeholder)
                        ""
                    }
                    2 -> {
                        codeLabel.text = getString(R.string.cun_title)
                        codeInput.setHint(R.string.code_placeholder)
                        getString(R.string.const_cun)
                    }
                    3 -> {
                        codeLabel.text = getString(R.string.nucg_title)
                        codeInput.setHint(R.string.code_placeholder)
                        getString(R.string.const_nucg)
                    }
                    4 -> {
                        codeLabel.text = getString(R.string.cuev_title)
                        codeInput.setHint(R.string.code_placeholder)
                        getString(R.string.const_cuev)
                    }
                    else -> ""
                }

                val lengthFilter = when (position) {
                    0 -> LengthFilter(12)
                    1 -> LengthFilter(17)
                    2 -> LengthFilter(10)
                    3 -> LengthFilter(10)
                    4 -> LengthFilter(10)
                    else -> LengthFilter(0)
                }
                codeInput.filters = arrayOf(InputFilter.AllCaps(), lengthFilter)
            }

        container.setOnClickListener {
            codeInput.clearFocus()
            healthInsuranceCardInput.clearFocus()
            expiredDateHealthIDDateInput.clearFocus()
        }
        codeInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_normal)?.let {
                        ColorStateList.valueOf(it)
                    })
                codeInput.hint = ""
                healthInsuranceCardInput.clearFocus()
                expiredDateHealthIDDateInput.clearFocus()
            } else {
                codeInput.hint = getString(R.string.code_placeholder)
            }
        }

        codeInput.filters += InputFilter.AllCaps()
        codeInput.filters += LengthFilter(0)

        codeInput.addTextChangedListener(
            object : TextWatcher {
                private var beforeText = ""

                override fun afterTextChanged(s: Editable) {
                    if (!s.toString().matches(regexAlphaNum) && "" != s.toString()) {
                        codeInput.setText(beforeText)
                        codeInput.setSelection(codeInput.text.toString().length)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    beforeText = s.toString()
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }
            }
        )

        healthInsuranceCardInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_normal)?.let {
                        ColorStateList.valueOf(it)
                    })
                codeInput.clearFocus()
                expiredDateHealthIDDateInput.clearFocus()
            }
        }
        healthInsuranceCardInput.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (healthInsuranceCardInput.text?.isNotBlank()!!) {
                        healthInsuranceCardInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                    } else {
                        healthInsuranceCardInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }
        )

        setDatePicker()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setDatePicker() {
        expiredDateHealthIDDateInput.inputType = InputType.TYPE_NULL
        expiredDateHealthIDDateInputLayout.setEndIconOnClickListener {
            expiredDateHealthIDDateInput.text?.clear()
        }
        expiredDateHealthIDDateInput.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.colorPrimary)?.let {
                        ColorStateList.valueOf(it)
                    })
                codeInput.clearFocus()
                healthInsuranceCardInput.clearFocus()
                openDatePicker()
                return@OnTouchListener true
            }
            false
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun openDatePicker() {
        builder.setTheme(R.style.Widget_AppTheme_MaterialDatePicker)
        materialDatePicker = builder.build()
        materialDatePicker.show(requireActivity().supportFragmentManager, "DATE PICKER")
        materialDatePicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            val format = SimpleDateFormat("dd/MM/yyyy")
            expiredDateHealthIDDateInput.setText(format.format(date))
        }
        materialDatePicker.addOnDismissListener {
            expiredDateHealthIDDateInputLayout.setStartIconTintList(
                context?.getColor(R.color.grey_normal)?.let {
                    ColorStateList.valueOf(it)
                })
        }
    }

    override fun onDialogNegative(requestCode: Int) {
        // Do nothing, user does not want to exit
    }

    override fun onDialogPositive(requestCode: Int) {
        if (requestCode == ALERT_CONFIRM_SAVE) {
            expiredDateHealthIDDateInput.text?.clear()
            if (!expiredDateHealthIDDateInput.isEnabled) {
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_light)?.let {
                        ColorStateList.valueOf(it)
                    })
            } else {
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_normal)?.let {
                        ColorStateList.valueOf(it)
                    })
            }
        }
    }
}
