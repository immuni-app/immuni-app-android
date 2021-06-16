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

package it.ministerodellasalute.immuni.extensions.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.WindowManager
import java.lang.Math.pow
import java.lang.Math.sqrt

/**
 * Screen utility methods.
 */
object ScreenUtils {

    private var SW = -1
    private var SH = -1

    /**
     * Get screen width in pixels.
     */
    fun getScreenWidth(context: Context): Int {
        if (SW != -1) return SW
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        SW = width
        return width
    }

    /**
     * Get screen height in pixels.
     */
    fun getScreenHeight(context: Context): Int {
        if (SH != -1) return SH
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y
        SH = height
        return height
    }

    /**
     * Get screen diagonal size in inches.
     */
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

    /**
     * Convert density independent unit to pixels.
     */
    fun convertDpToPixels(context: Context, dp: Int): Int {
        val r = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics).toInt()
    }

    /**
     * Convert pixels to inches.
     */
    fun convertPixelsToInches(context: Context, pixels: Int): Double {
        val r = context.resources
        return pixels.toDouble() / r.displayMetrics.xdpi
    }

    /**
     * Convert pixels to density independent unit.
     */
    fun convertPixelsToDp(context: Context, pixels: Float): Float {
        val r: Resources = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            pixels,
            r.displayMetrics
        )
    }
}
