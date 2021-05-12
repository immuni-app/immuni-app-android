package it.ministerodellasalute.immuni.ui.greencertificate

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.loading
import it.ministerodellasalute.immuni.extensions.utils.byAdding
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.util.ProgressDialogFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.generate_green_certificate.*
import kotlinx.android.synthetic.main.generate_green_certificate.container
import kotlinx.android.synthetic.main.generate_green_certificate.cunInput
import kotlinx.android.synthetic.main.generate_green_certificate.healthInsuranceCardInput
import kotlinx.android.synthetic.main.generate_green_certificate.navigationIcon
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

        navigationIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        generateGC.setOnClickListener {
            viewModel.genera(
                cun = cunInput.text.toString(),
                health_insurance_card = healthInsuranceCardInput.text.toString(),
                symptom_onset_date = expiredDateHealthIDDateInput.text.toString()
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
                    getString(R.string.upload_data_verify_loading)
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

        container.setOnClickListener {
            cunInput.clearFocus()
            healthInsuranceCardInput.clearFocus()
            expiredDateHealthIDDateInput.clearFocus()
        }
        cunInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_normal)?.let {
                        ColorStateList.valueOf(it)
                    })
                cunInput.hint = ""
                healthInsuranceCardInput.clearFocus()
                expiredDateHealthIDDateInput.clearFocus()
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
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
                    context?.getColor(R.color.grey_normal)?.let {
                        ColorStateList.valueOf(it)
                    })
                cunInput.clearFocus()
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
            expiredDateHealthIDDateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
            expiredDateHealthIDDateInput.text?.clear()
        }
        expiredDateHealthIDDateInput.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                expiredDateHealthIDDateInputLayout.setStartIconTintList(
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
        val endYear = Calendar.getInstance()
        endYear.set(endYear.get(Calendar.YEAR), 11, 31)
        val minDate = Date().byAdding(days = -1).time
        val maxDate = Date(endYear.timeInMillis).byAdding(year = 11).time
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
            expiredDateHealthIDDateInput.setText(format.format(date))
            expiredDateHealthIDDateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
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

@Parcelize
internal class RangeValidator(
    private var minDate: Long = 0,
    private var maxDate: Long = 0
) : CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {
        return !(minDate > date || maxDate < date)
    }
}