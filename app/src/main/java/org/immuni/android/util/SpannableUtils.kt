package org.immuni.android.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

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