package org.immuni.android.extensions.view

import android.app.Activity
import android.view.GestureDetector
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