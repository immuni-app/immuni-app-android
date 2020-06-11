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

package it.ministerodellasalute.immuni.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmationDialogFragment : DialogFragment() {

    private val listener: ConfirmationDialogListener
        get() = targetFragment as ConfirmationDialogListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val args = requireArguments()

        this.isCancelable = args.getBoolean(ARG_CANCELABLE)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.getString(ARG_TITLE))
            .setCancelable(args.getBoolean(ARG_CANCELABLE))
            .setMessage(args.getString(ARG_MESSAGE))
            .setPositiveButton(args.getString(ARG_POSITIVE_BUTTON)) { _, _ ->
                listener.onDialogPositive(
                    targetRequestCode
                )
            }
            .setNegativeButton(args.getString(ARG_NEGATIVE_BUTTON)) { _, _ ->
                listener.onDialogNegative(
                    targetRequestCode
                )
            }
            .setOnCancelListener { }
            .create()
    }

    companion object {
        private const val ARG_POSITIVE_BUTTON = "ARG_POSITIVE_BUTTON"
        private const val ARG_NEGATIVE_BUTTON = "ARG_NEGATIVE_BUTTON"
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_CANCELABLE = "ARG_CANCELABLE"
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        private const val ARG_REQUEST_CODE = "ARG_REQUEST_CODE"

        fun createInstance(
            positiveButton: String,
            negativeButton: String? = null,
            message: String? = null,
            title: String,
            cancelable: Boolean,
            requestCode: Int
        ): ConfirmationDialogFragment {
            return ConfirmationDialogFragment().apply {
                arguments = bundleOf(
                    ARG_POSITIVE_BUTTON to positiveButton,
                    ARG_NEGATIVE_BUTTON to negativeButton,
                    ARG_MESSAGE to message,
                    ARG_TITLE to title,
                    ARG_CANCELABLE to cancelable,
                    ARG_REQUEST_CODE to requestCode
                )
            }
        }
    }
}

interface ConfirmationDialogListener {
    fun onDialogPositive(requestCode: Int)
    fun onDialogNegative(requestCode: Int)
}

/**
 * @param requestCode is useful to distinguish between multiple confirmation dialogs used in single
 * fragment.
 * This parameter gets back to the listener so that caller knows which dialog returned result.
 */
fun <T> T.openConfirmationDialog(
    positiveButton: String,
    negativeButton: String? = null,
    message: String? = null,
    title: String,
    cancelable: Boolean = true,
    requestCode: Int
) where T : Fragment, T : ConfirmationDialogListener {
    val fragment = ConfirmationDialogFragment.createInstance(
        positiveButton,
        negativeButton,
        message,
        title,
        cancelable,
        requestCode
    )
    fragment.setTargetFragment(this, requestCode)
    fragment.show(parentFragmentManager, "CONFIRMATION_DIALOG_$requestCode")
}
