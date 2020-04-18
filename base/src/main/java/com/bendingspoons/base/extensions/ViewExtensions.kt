package com.bendingspoons.base.extensions

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun View.stopAnimation() {
    clearAnimation()
    this.animate().cancel()
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

fun TabLayout.setTint(color: Int, selectedColor: Int = color) {
    val states = arrayOf(
        intArrayOf(android.R.attr.state_selected), // selected
        intArrayOf(-android.R.attr.state_enabled), // not enabled
        intArrayOf(android.R.attr.state_enabled) // enabled
    )

    val colors = intArrayOf(selectedColor, color, color)

    val myList = ColorStateList(states, colors)
    this.tabIconTint = myList

    setTabTextColors(color, selectedColor)
}

fun SwitchCompat.setTint(checked: Int, unchecked: Int) {
    val states = arrayOf(
        intArrayOf(-android.R.attr.state_checked),
        intArrayOf(android.R.attr.state_checked))

    val alphaBlack50 = Color.argb(125, 0, 0, 0)
    val checkedAlpha = Color.argb(125, Color.red(checked), Color.green(checked), Color.blue(checked))

    val thumbColors = intArrayOf(unchecked, checked)
    val trackColors = intArrayOf(alphaBlack50, checkedAlpha)

    DrawableCompat.setTintList(
        DrawableCompat.wrap(this.thumbDrawable),
        ColorStateList(states, thumbColors)
    )
    DrawableCompat.setTintList(
        DrawableCompat.wrap(this.trackDrawable),
        ColorStateList(states, trackColors)
    )
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
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
    if(this.alpha == 1f) return
    this.clearAnimation()
    this.animate()
        .alpha(1.0f)
        .setListener(null)
        .duration = duration
}

fun View.animateHide(duration: Long = 500L) {
    if(this.alpha == 0f) return
    this.clearAnimation()
    this.animate()
        .alpha(0.0f)
        .setListener(null)
        .duration = duration
}

fun View.animateScale(scale: Float, duration: Long = 500L) {
    if(this.scaleX == scale && this.scaleY == scale) return
    this.clearAnimation()
    this.animate()
        .scaleX(scale)
        .scaleY(scale)
        .setListener(null)
        .duration = duration
}

fun View.animateTranslationY(translation: Float, duration: Long = 500L) {
    if(this.translationY == translation) return
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