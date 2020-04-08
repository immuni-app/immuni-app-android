package com.bendingspoons.base.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.WindowManager
import com.bendingspoons.base.extensions.round
import java.lang.Math.pow
import java.lang.Math.sqrt


object ScreenUtils {

    private var SW = -1
    private var SH = -1

    fun getScreenWidth(context: Context): Int {

        if (SW != -1) return SW

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        SW = width
        return width
    }

    fun getScreenSizeInInches(context: Context): Double {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val d = wm.defaultDisplay
        val metrics = DisplayMetrics()
        d.getMetrics(metrics)
        var widthPixels = metrics.widthPixels
        var heightPixels = metrics.heightPixels
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) try {
            widthPixels = Display::class.java.getMethod("getRawWidth").invoke(d) as Int
            heightPixels = Display::class.java.getMethod("getRawHeight").invoke(d) as Int
        } catch (ignored: Exception) { }
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) try {
            val realSize = Point()
            Display::class.java.getMethod("getRealSize", Point::class.java)
                .invoke(d, realSize)
            widthPixels = realSize.x
            heightPixels = realSize.y
        } catch (ignored: Exception) { }

        val diagonalInPixels = sqrt(pow(widthPixels.toDouble(), 2.0) + pow(heightPixels.toDouble(), 2.0))
        return convertPixelsToInches(context, diagonalInPixels.toInt()).round(1)
    }

    fun getScreenHeight(context: Context): Int {

        if (SH != -1) return SH

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        SH = height
        return height
    }

    fun convertDpToPixels(context: Context, dp: Int): Int {
        val r = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics).toInt()
    }

    fun convertPixelsToInches(context: Context, pixels: Int): Double {
        val r = context.resources
        return pixels.toDouble() / r.displayMetrics.xdpi
    }
}
