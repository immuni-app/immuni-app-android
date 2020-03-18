package com.bendingspoons.ascolto

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject

@SuppressLint("Registered")
open class AscoltoActivity : AppCompatActivity() {

    //val secretMenu: SecretMenu by inject()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            //secretMenu.onTouchEvent(it)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun dispatchDialogTouchEvent(ev: MotionEvent?) {
        ev?.let {
            //secretMenu.onTouchEvent(it)
        }
    }
}