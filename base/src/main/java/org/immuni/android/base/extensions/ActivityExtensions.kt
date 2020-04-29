package org.immuni.android.base.extensions

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.base.R

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
fun FragmentActivity.loading(loading: Boolean, dialog: DialogFragment?) {
    val tag = "loading_dialog"
    if(loading) {
        val ft = supportFragmentManager.beginTransaction()

        val prev = supportFragmentManager.findFragmentByTag(tag)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

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

fun AppCompatActivity.transparentStatusBarWithNavigationBar() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    setStatusBarColor(android.R.color.transparent)
    // If KITKAT fallback to translucent status bar
    // Lower API version will have default system UI
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
    }
}

fun AppCompatActivity.setLightStatusBar(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setStatusBarColor(color)
    } else {
        setStatusBarColor(getThemeColor(R.attr.colorPrimaryDark))
    }
}

fun AppCompatActivity.setLightStatusBarFullscreen(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
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
    setStatusBarColor(color)
}

fun AppCompatActivity.setDarkStatusBarFullscreen(color: Int) {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    setStatusBarColor(color)
}

fun AppCompatActivity.immersiveMode() {
    if (Build.VERSION.SDK_INT >= 19) {
        val decorView = window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        decorView.setOnSystemUiVisibilityChangeListener {
            immersiveMode()
        }
    }
}

fun AppCompatActivity.transparentSystemUI() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}

fun AppCompatActivity.removeNavigationBar() {
    val decorView = window.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    decorView.systemUiVisibility = uiOptions
}


fun AppCompatActivity.setNavigationBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.navigationBarColor = color
    }
}

fun AppCompatActivity.setStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }
}

fun FragmentActivity.showAlert(title: String,
                               message: String,
                               positiveButton: String,
                               positiveButtonListener: DialogInterface.OnClickListener? = null,
                               negativeButton: String? = null,
                               negativeButtonListener: DialogInterface.OnClickListener? = null,
                               neutralButton: String? = null,
                               neutralButtonListener: DialogInterface.OnClickListener? = null): AlertDialog {
    val builder = MaterialAlertDialogBuilder(this).apply {
        setMessage(message)
        setTitle(title)

        // positive action
        if(positiveButtonListener != null) setPositiveButton(positiveButton, positiveButtonListener)
        else setPositiveButton(positiveButton) { dialog, id -> dialog.dismiss()}

        // negative action
        if(negativeButton != null && negativeButtonListener != null) setNegativeButton(negativeButton, negativeButtonListener)
        else if(negativeButton != null) setNegativeButton(negativeButton) { dialog, id -> dialog.dismiss()}

        // neutral action
        if(neutralButton != null && neutralButtonListener != null) setNeutralButton(neutralButton, neutralButtonListener)
        else if(neutralButton != null) setNeutralButton(neutralButton) { dialog, id -> dialog.dismiss()}
    }

    val dialog: AlertDialog = builder.create()
    dialog.show()
    return dialog
}

fun FragmentActivity.showEditAlert(title: String,
                                   message: String,
                                   hint: String? = "",
                                   positiveButton: String,
                                   positiveButtonListener: EditTextDialogInterface.OnClickListener? = null,
                                   negativeButton: String? = null,
                                   negativeButtonListener: DialogInterface.OnClickListener? = null,
                                   neutralButton: String? = null,
                                   neutralButtonListener: DialogInterface.OnClickListener? = null,
                                   cancelable: Boolean = true): Dialog {

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
            imm?.hideSoftInputFromWindow(editText.getWindowToken(), 0)
        }

        setView(view)

        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        // positive action
        if(positiveButtonListener != null) setPositiveButton(positiveButton) { dialog, id ->
            hideKeyboard()
            positiveButtonListener.onClick(dialog, id, editText.text.toString())
        }
        else setPositiveButton(positiveButton) { dialog, id ->
            hideKeyboard()
            dialog.dismiss()}

        // negative action
        if(negativeButton != null && negativeButtonListener != null) setNegativeButton(negativeButton, negativeButtonListener)
        else if(negativeButton != null) setNegativeButton(negativeButton) { dialog, id -> dialog.dismiss()}

        // neutral action
        if(neutralButton != null && neutralButtonListener != null) setNeutralButton(neutralButton, neutralButtonListener)
        else if(neutralButton != null) setNeutralButton(neutralButton) { dialog, id -> dialog.dismiss()}
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