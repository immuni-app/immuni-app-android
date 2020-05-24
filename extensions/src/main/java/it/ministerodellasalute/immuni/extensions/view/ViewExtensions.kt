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

package it.ministerodellasalute.immuni.extensions.view

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Build
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GestureDetectorCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * View utility extensions.
 */

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.setOutlineSpotShadowColorCompat(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        outlineSpotShadowColor = color
    }
}

fun View.setTint(color: Int, selectedColor: Int = color) {
    val originalEnabled = this.isEnabled
    val originalActivated = this.isActivated

    val states = arrayOf(
        intArrayOf(android.R.attr.state_selected), // selected
        intArrayOf(-android.R.attr.state_enabled), // not enabled
        intArrayOf(android.R.attr.state_enabled) // enabled
    )

    val colors = intArrayOf(selectedColor, color, color)

    val myList = ColorStateList(states, colors)
    this.backgroundTintList = myList
    // do this to force button layout(bug?)
    this.isActivated = !originalActivated
    this.isActivated = originalActivated
    this.isEnabled = !originalEnabled
    this.isEnabled = originalEnabled
}

fun View.onLongPress(block: () -> Unit) {
    val tapGestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent?) {
                super.onLongPress(e)
                block()
            }
        })
    this.setOnTouchListener { v, event ->
        tapGestureDetector.onTouchEvent(event)
        false
    }
}

fun View.animateShow(duration: Long = 500L) {
    if (this.alpha == 1f) return
    this.clearAnimation()
    this.animate()
        .alpha(1.0f)
        .setListener(null)
        .duration = duration
}

fun View.animateHide(duration: Long = 500L) {
    if (this.alpha == 0f) return
    this.clearAnimation()
    this.animate()
        .alpha(0.0f)
        .setListener(null)
        .duration = duration
}

fun View.animateScale(scale: Float, duration: Long = 500L) {
    if (this.scaleX == scale && this.scaleY == scale) return
    this.clearAnimation()
    this.animate()
        .scaleX(scale)
        .scaleY(scale)
        .setListener(null)
        .duration = duration
}

fun View.animateTranslationY(translation: Float, duration: Long = 500L) {
    if (this.translationY == translation) return
    this.clearAnimation()
    this.animate()
        .translationY(translation)
        .setListener(null)
        .duration = duration
}

fun View.hideKeyboard() {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this@hideKeyboard.windowToken, 0)
}

fun View.showKeyboard() {
    GlobalScope.launch(Dispatchers.Main) {
        runCatching {
            delay(100)
            val imm: InputMethodManager =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            this@showKeyboard.requestFocus()
            imm.showSoftInput(this@showKeyboard, 0)
        }
    }
}

/**
 * @param listener returns true if event is handled, false if system should handle it.
 */
fun Dialog.setOnBackListener(listener: () -> Boolean) {
    setOnKeyListener { _, keyCode, keyEvent ->
        // Avoid double events (ACTION_UP/ACTION_DOWN)
        if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            listener()
        } else false
    }
}
