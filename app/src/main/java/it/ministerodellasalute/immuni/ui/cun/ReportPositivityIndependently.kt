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

package it.ministerodellasalute.immuni.ui.cun

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.ministerodellasalute.immuni.DataUploadDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.loading
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.utils.byAdding
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.util.ProgressDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.report_positivity_cun.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class ReportPositivityIndependently : Fragment(R.layout.report_positivity_cun) {

    companion object {
        var NAVIGATE_UP = false
    }

    private lateinit var viewModel: CunViewModel
    val builder = MaterialDatePicker.Builder.datePicker()
    private lateinit var materialDatePicker: MaterialDatePicker<Long>

    override fun onResume() {
        super.onResume()
        if (NAVIGATE_UP) {
            NAVIGATE_UP = false
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(
            resources.getColor(
                R.color.background_darker,
                null
            )
        )

        viewModel = getViewModel()

        setInput()

        navigationIcon.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        knowMore.setSafeOnClickListener {
            val action = DataUploadDirections.actionHowToUploadPositiveIndependently()
            findNavController().navigate(action)
        }

        verify.setSafeOnClickListener {
            var symptomOnsetDate: String = symptomOnsetDateInput.text.toString()
            if (symptomOnsetDateInput.text!!.isNotBlank()) {
                val dateSplitted = symptomOnsetDateInput.text!!.split("/")
                symptomOnsetDate = dateSplitted.reversed().joinToString("-")
            }

            viewModel.verifyIndependently(
                cun = cunInput.text.toString(),
                health_insurance_card = healthInsuranceCardInput.text.toString(),
                symptom_onset_date = symptomOnsetDate
            )
        }

        goTo.setSafeOnClickListener {
            val action = DataUploadDirections.actionUploadData(true)
            findNavController().navigate(action)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            activity?.loading(it, ProgressDialogFragment(), Bundle().apply {
                putString(
                    ProgressDialogFragment.MESSAGE,
                    getString(R.string.upload_data_verify_loading)
                )
            })
        }

        viewModel.navigateToUploadPage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { token ->
                val action =
                    DataUploadDirections.actionUploadActivity(
                        null,
                        CunToken.fromLogic(token),
                        true,
                        false
                    )
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
    }

    private fun setInput() {
        val regexAlphaNum = "^[A-Z0-9]*$".toRegex()

        container.setOnClickListener {
            cunInput.clearFocus()
            healthInsuranceCardInput.clearFocus()
            symptomOnsetDateInput.clearFocus()
        }
        cunInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                symptomOnsetDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_normal)?.let {
                        ColorStateList.valueOf(it)
                    })
                cunInput.hint = ""
                healthInsuranceCardInput.clearFocus()
                symptomOnsetDateInput.clearFocus()
            } else {
                cunInput.hint = getString(R.string.cun_placeholder)
            }
        }

        cunInput.filters += InputFilter.AllCaps()

        cunInput.addTextChangedListener(
            object : TextWatcher {
                private var beforeText = ""

                override fun afterTextChanged(s: Editable) {
                    if (!s.toString().matches(regexAlphaNum) && "" != s.toString()) {
                        cunInput.setText(beforeText)
                        cunInput.setSelection(cunInput.text.toString().length)
                    } else {
                        if (cunInput.text?.isNotBlank()!!) {
                            cunInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                        } else {
                            cunInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                        }
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
                symptomOnsetDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_normal)?.let {
                        ColorStateList.valueOf(it)
                    })
                cunInput.clearFocus()
                symptomOnsetDateInput.clearFocus()
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
        symptomOnsetDateInput.inputType = InputType.TYPE_NULL
        symptomOnsetDateInputLayout.setEndIconOnClickListener {
            symptomOnsetDateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
            symptomOnsetDateInput.text?.clear()
        }
        symptomOnsetDateInput.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                symptomOnsetDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.colorPrimary)?.let {
                        ColorStateList.valueOf(it)
                    })
                cunInput.clearFocus()
                healthInsuranceCardInput.clearFocus()
                openDatePicker()
                return@OnTouchListener true
            }
            false
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun openDatePicker() {
        val minDate = Date().byAdding(days = -14).time
        val maxDate = Date().byAdding().time
        val constraintsBuilder = CalendarConstraints.Builder()
        constraintsBuilder.setEnd(maxDate)
        constraintsBuilder.setStart(minDate)
        constraintsBuilder.setValidator(
            RangeValidator(
                minDate,
                maxDate
            )
        )
        builder.setCalendarConstraints(constraintsBuilder.build())
        builder.setTheme(R.style.Widget_AppTheme_MaterialDatePicker)
        materialDatePicker = builder.build()
        materialDatePicker.show(requireActivity().supportFragmentManager, "DATE PICKER")
        materialDatePicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            val format = SimpleDateFormat("dd/MM/yyyy")
            symptomOnsetDateInput.setText(format.format(date))
            symptomOnsetDateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        }
        materialDatePicker.addOnDismissListener {
            symptomOnsetDateInputLayout.setStartIconTintList(
                context?.getColor(R.color.grey_normal)?.let {
                    ColorStateList.valueOf(it)
                })
        }
    }
}

@Parcelize
internal class RangeValidator(
    private var minDate: Long = 0,
    private var maxDate: Long = 0
) : CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {
        return !(minDate > date || maxDate < date)
    }
}
