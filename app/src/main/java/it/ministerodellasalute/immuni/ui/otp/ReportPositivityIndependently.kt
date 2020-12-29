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

package it.ministerodellasalute.immuni.ui.otp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import it.ministerodellasalute.immuni.DataUploadDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.byAdding
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.report_positivity_cun.*
import java.text.SimpleDateFormat
import java.util.*


class ReportPositivityIndependently : Fragment(R.layout.report_positivity_cun) {

    val builder = MaterialDatePicker.Builder.datePicker()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        knowMore.setSafeOnClickListener {
            val action = DataUploadDirections.actionHowToUploadPositiveIndependently()
            findNavController().navigate(action)
        }

        setDatePicker()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setDatePicker() {
        symptomOnsetDateInput.inputType = InputType.TYPE_NULL
        symptomOnsetDateInput.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
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
        constraintsBuilder.setValidator(RangeValidator(minDate, maxDate))
        builder.setCalendarConstraints(constraintsBuilder.build())
        builder.setTheme(R.style.Widget_AppTheme_MaterialDatePicker)
        val materialDatePicker = builder.build()
        materialDatePicker.show(requireActivity().supportFragmentManager, "DATE PICKER")
        materialDatePicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            val format = SimpleDateFormat("dd/MM/yyyy")
            symptomOnsetDateInput.setText(format.format(date))
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
