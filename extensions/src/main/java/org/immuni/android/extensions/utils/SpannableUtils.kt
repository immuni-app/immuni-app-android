package org.immuni.android.extensions.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView

/**
 * Change color and weight of a specific part of a [TextView].
 *
 * @param startPlaceholder the starting placeholder (eg. in My name
 * is {Marco} the starting placeholder is "{").
 * @param endPlaceholder (eg. "}").
 * @param color the color of the selected string.
 * @param bold if the selected string should be bold.
 * @return a [Spannable] to be used by a [TextView].
 */
fun String.color(startPlaceholder: String, endPlaceholder: String, color: Int, bold: Boolean = false): Spannable {
    val textWithoutPlaceholders = this.replace(startPlaceholder, "")
        .replace(endPlaceholder, "")
    val start = this.indexOf(startPlaceholder)
    val end = this.indexOf(endPlaceholder)
    val spannable = SpannableString(textWithoutPlaceholders)
    spannable.setSpan(
        ForegroundColorSpan(color),
        start,
        end - 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    val boldStyle = StyleSpan(Typeface.BOLD)
    spannable.setSpan(
        boldStyle,
        start,
        end - 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
}
