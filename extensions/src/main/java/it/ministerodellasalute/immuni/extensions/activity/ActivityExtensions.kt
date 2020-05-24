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

package it.ministerodellasalute.immuni.extensions.activity

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import it.ministerodellasalute.immuni.extensions.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Toast utility methods.
 */
fun toast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, length).show()
}

fun toast(context: Context, @StringRes title: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(
        context,
        context.resources.getString(title),
        length
    )
}

/**
 * Show a loading [DialogFragment].
 *
 * @param loading true if the dialog fragment should be shown, false otherwise.
 * @param dialog the dialog fragment we want to show during loading.
 */
fun FragmentActivity.loading(loading: Boolean, dialog: DialogFragment?, arguments: Bundle? = null) {
    val tag = "loading_dialog"
    if (loading) {
        val ft = supportFragmentManager.beginTransaction()

        val prev = supportFragmentManager.findFragmentByTag(tag)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        dialog?.arguments = arguments

        dialog?.show(ft, tag)
    } else {
        val ft = supportFragmentManager.beginTransaction()

        val prev = supportFragmentManager.findFragmentByTag(tag)
        if (prev != null) {
            (prev as? DialogFragment)?.dismiss()
            ft.remove(prev)
        }
    }
}

/**
 * Disable "Drag" for AppBarLayout
 * (i.e. User can't scroll appBarLayout by directly
 * touching appBarLayout - User can only scroll appBarLayout by only using scrollContent).
 */
fun AppBarLayout.disableDragging() {
    if (this.layoutParams != null) {
        val layoutParams = this.layoutParams as CoordinatorLayout.LayoutParams
        val appBarLayoutBehaviour = AppBarLayout.Behavior()
        appBarLayoutBehaviour.setDragCallback(object :
            AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
        layoutParams.behavior = appBarLayoutBehaviour
    }
}

fun AppCompatActivity.setLightStatusBar(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        setStatusBarColor(color)
    } else {
        setStatusBarColor(getThemeColor(R.attr.colorPrimaryDark))
    }
}

fun AppCompatActivity.setLightStatusBarFullscreen(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        setStatusBarColor(color)
    } else {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setStatusBarColor(getThemeColor(R.attr.colorPrimaryDark))
    }
}

fun AppCompatActivity.getThemeColor(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    val theme: Resources.Theme = applicationContext.theme
    theme.resolveAttribute(res, typedValue, true)
    return typedValue.data
}

fun AppCompatActivity.setDarkStatusBar(color: Int) {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    setStatusBarColor(color)
}

fun AppCompatActivity.setDarkStatusBarFullscreen(color: Int) {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    setStatusBarColor(color)
}

fun AppCompatActivity.setStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }
}

fun FragmentActivity.showAlert(
    title: String,
    message: String,
    positiveButton: String,
    positiveButtonListener: DialogInterface.OnClickListener? = null,
    negativeButton: String? = null,
    negativeButtonListener: DialogInterface.OnClickListener? = null,
    neutralButton: String? = null,
    neutralButtonListener: DialogInterface.OnClickListener? = null
): AlertDialog {
    val builder = MaterialAlertDialogBuilder(this).apply {
        setMessage(message)
        setTitle(title)

        // positive action
        if (positiveButtonListener != null) setPositiveButton(positiveButton, positiveButtonListener)
        else setPositiveButton(positiveButton) { dialog, id -> dialog.dismiss() }

        // negative action
        if (negativeButton != null && negativeButtonListener != null) setNegativeButton(negativeButton, negativeButtonListener)
        else if (negativeButton != null) setNegativeButton(negativeButton) { dialog, id -> dialog.dismiss() }

        // neutral action
        if (neutralButton != null && neutralButtonListener != null) setNeutralButton(neutralButton, neutralButtonListener)
        else if (neutralButton != null) setNeutralButton(neutralButton) { dialog, id -> dialog.dismiss() }
    }

    val dialog: AlertDialog = builder.create()
    dialog.show()
    return dialog
}

fun FragmentActivity.showEditAlert(
    title: String,
    message: String,
    hint: String? = "",
    positiveButton: String,
    positiveButtonListener: EditTextDialogInterface.OnClickListener? = null,
    negativeButton: String? = null,
    negativeButtonListener: DialogInterface.OnClickListener? = null,
    neutralButton: String? = null,
    neutralButtonListener: DialogInterface.OnClickListener? = null,
    cancelable: Boolean = true
): Dialog {

    val editText: TextInputEditText

    val builder = MaterialAlertDialogBuilder(this).apply {

        setCancelable(cancelable)
        setMessage(message)
        setTitle(title)

        val view = layoutInflater.inflate(R.layout.edit_text_alert_dialog, null)
        editText = view.findViewById(R.id.editText) as TextInputEditText
        editText.hint = hint

        fun hideKeyboard() {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(editText.windowToken, 0)
        }

        setView(view)

        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        // positive action
        if (positiveButtonListener != null) setPositiveButton(positiveButton) { dialog, id ->
            hideKeyboard()
            positiveButtonListener.onClick(dialog, id, editText.text.toString())
        }
        else setPositiveButton(positiveButton) { dialog, id ->
            hideKeyboard()
            dialog.dismiss() }

        // negative action
        if (negativeButton != null && negativeButtonListener != null) setNegativeButton(negativeButton, negativeButtonListener)
        else if (negativeButton != null) setNegativeButton(negativeButton) { dialog, id -> dialog.dismiss() }

        // neutral action
        if (neutralButton != null && neutralButtonListener != null) setNeutralButton(neutralButton, neutralButtonListener)
        else if (neutralButton != null) setNeutralButton(neutralButton) { dialog, id -> dialog.dismiss() }
    }

    val dialog: AlertDialog = builder.create()
    dialog.show()

    val imm = this.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    GlobalScope.launch {
        delay(500)
        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        imm?.showSoftInput(
            editText,
            InputMethodManager.SHOW_IMPLICIT)
    }

    return dialog
}
