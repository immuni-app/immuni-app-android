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

import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans

/**
 * Generic function that gives ability to build [SpannedString] and span placeholder
 * between with start and end characters.
 * [builderAction] gives you ability to customize placeholder.
 */
fun String.spanPlaceholder(
    startPlaceholder: Char = '{',
    endPlaceholder: Char = '}',
    builderAction: SpannableStringBuilder.(CharSequence) -> Unit
): SpannedString {
    val text = this
    val startIdx = this.indexOf(startPlaceholder)
    val endIdx = this.indexOf(endPlaceholder)
    return buildSpannedString {
        append(text.subSequence(0, startIdx))
        builderAction(text.subSequence(startIdx + 1, endIdx))
        if (endIdx < text.length - 1) {
            append(text.subSequence(endIdx + 1, text.length))
        }
    }
}

/**
 * Add click handler to spannable string
 */
inline fun SpannableStringBuilder.clickable(
    crossinline onClick: () -> Unit,
    builderAction: SpannableStringBuilder.() -> Unit
) = inSpans(object : ClickableSpan() {
    override fun onClick(p0: View) {
        onClick()
    }
}, builderAction = builderAction)

/**
 * Add click handler to spannable string
 */
inline fun SpannableStringBuilder.clickableWithoutUnderline(
    crossinline onClick: () -> Unit,
    builderAction: SpannableStringBuilder.() -> Unit
) = inSpans(object : ClickableSpan() {
    override fun onClick(p0: View) {
        onClick()
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = false
    }
}, builderAction = builderAction)

/**
 * Change color and weight of a specific part of a [TextView].
 *
 * @param startPlaceholder the starting placeholder (eg. in My name
 * is {Marco} the starting placeholder is "{").
 * @param endPlaceholder (eg. "}").
 * @param color the color of the selected string.
 * @param bold if the selected string should be bold.
 * @return a [SpannedString] to be used by a [TextView].
 */
fun String.color(
    startPlaceholder: Char,
    endPlaceholder: Char,
    @ColorInt color: Int,
    bold: Boolean = false
): SpannedString {
    return spanPlaceholder(startPlaceholder, endPlaceholder) {
        color(color) {
            if (bold) {
                bold {
                    append(it)
                }
            } else append(it)
        }
    }
}

fun String.coloredClickable(
    startPlaceholder: Char = '{',
    endPlaceholder: Char = '}',
    @ColorInt color: Int,
    bold: Boolean = false,
    onClick: () -> Unit
): SpannedString {
    return spanPlaceholder(startPlaceholder, endPlaceholder) {
        color(color) {
            clickable(onClick) {
                if (bold) {
                    bold {
                        append(it)
                    }
                } else append(it)
            }
        }
    }
}

/**
 * Highlights every word that matches `highlight` in this text with specified color.
 */
fun String.colorHighlight(
    highlight: String,
    @ColorInt color: Int
): SpannedString {
    val text = this
    if (highlight.isBlank()) return buildSpannedString { append(text) }

    // Find all start indices
    val highlightStartIndices = mutableListOf<Int>()
    while (true) {
        val startIndex = text.indexOf(
            string = highlight,
            startIndex = highlightStartIndices.lastOrNull()?.let { it + highlight.length } ?: 0,
            ignoreCase = true
        )
        // if we are getting lower index than we should, quit searching.
        if (startIndex < 0 || startIndex < highlightStartIndices.size) break
        highlightStartIndices.add(startIndex)
    }

    return buildSpannedString {
        var currStart = 0
        highlightStartIndices.forEach { highlightStartIdx ->
            // append before highlight
            append(text.subSequence(currStart, highlightStartIdx))
            val highlightEndIdx = highlightStartIdx + highlight.length
            // append highlight
            color(color) { append(text.subSequence(highlightStartIdx, highlightEndIdx)) }
            currStart = highlightEndIdx
        }
        // append last part
        append(text.subSequence(currStart, text.length))
    }
}

fun String.boldLinkSpan(
    startPlaceholderLink: Char = '{',
    endPlaceholderLink: Char = '}',
    @ColorInt colorLink: Int,
    linkUnderlined: Boolean = true,
    boldLink: Boolean = false,
    startPlaceholderBold: Char = '[',
    endPlaceholderBold: Char = ']',
    @ColorInt color: Int,
    bold: Boolean = false,
    onClick: () -> Unit
): SpannedString {
    val text = this
    val startIdx = this.indexOf(startPlaceholderBold)
    val endIdx = this.indexOf(endPlaceholderBold)
    val startIdxLink = this.indexOf(startPlaceholderLink)
    val endIdxLink = this.indexOf(endPlaceholderLink)
    if (startIdx < startIdxLink) {
        return buildSpannedString {
            append(text.subSequence(0, startIdx))
            color(color) {
                if (bold) {
                    bold {
                        append(text.subSequence(startIdx + 1, endIdx))
                    }
                } else append(text.subSequence(startIdx + 1, endIdx))
            }
            append(text.subSequence(endIdx + 1, startIdxLink))
            color(colorLink) {
                if (linkUnderlined) {
                    clickable(onClick) {
                        if (boldLink) {
                            bold {
                                append(text.subSequence(startIdxLink + 1, endIdxLink))
                            }
                        } else append(text.subSequence(startIdxLink + 1, endIdxLink))
                    }
                } else {
                    clickableWithoutUnderline(onClick) {
                        if (boldLink) {
                            bold {
                                append(text.subSequence(startIdxLink + 1, endIdxLink))
                            }
                        } else append(text.subSequence(startIdxLink + 1, endIdxLink))
                    }
                }
            }
            if (endIdxLink < text.length - 1) {
                append(text.subSequence(endIdxLink + 1, text.length))
            }
        }
    } else {
        return buildSpannedString {
            append(text.subSequence(0, startIdxLink))
            color(colorLink) {
                if (linkUnderlined) {
                    clickable(onClick) {
                        if (boldLink) {
                            bold {
                                append(text.subSequence(startIdxLink + 1, endIdxLink))
                            }
                        } else append(text.subSequence(startIdxLink + 1, endIdxLink))
                    }
                } else {
                    clickableWithoutUnderline(onClick) {
                        if (boldLink) {
                            bold {
                                append(text.subSequence(startIdxLink + 1, endIdxLink))
                            }
                        } else append(text.subSequence(startIdxLink + 1, endIdxLink))
                    }
                }
            }
            append(text.subSequence(endIdxLink + 1, startIdx))
            color(color) {
                if (bold) {
                    bold {
                        append(text.subSequence(startIdx + 1, endIdx))
                    }
                } else append(text.subSequence(startIdx + 1, endIdx))
            }
            if (endIdx < text.length - 1) {
                append(text.subSequence(endIdx + 1, text.length))
            }
        }
    }
}
