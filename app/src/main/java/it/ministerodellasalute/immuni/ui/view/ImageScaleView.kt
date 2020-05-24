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

package it.ministerodellasalute.immuni.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.view.ImageScaleView.MatrixCropType

/**
 * Allow ImageView cropping with different alignments.
 *
 */
class ImageScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    private var mMatrixType = MatrixCropType.TOP_CENTER // default

    private enum class MatrixCropType(private val mValue: Int) {
        TOP_CENTER(0),
        BOTTOM_CENTER(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3),
        TOP_LEFT(4),
        TOP_RIGHT(5);

        companion object {
            fun fromValue(value: Int): MatrixCropType {
                for (matrixCropType in values()) {
                    if (matrixCropType.mValue == value) {
                        return matrixCropType
                    }
                }
                // default
                return TOP_CENTER
            }
        }
    }

    override fun setFrame(
        frameLeft: Int,
        frameTop: Int,
        frameRight: Int,
        frameBottom: Int
    ): Boolean {
        val drawable = drawable
        if (drawable != null) {
            val frameWidth = frameRight - frameLeft.toFloat()
            val frameHeight = frameBottom - frameTop.toFloat()
            val originalImageWidth = getDrawable().intrinsicWidth.toFloat()
            val originalImageHeight = getDrawable().intrinsicHeight.toFloat()
            var usedScaleFactor = 1f
            if (frameWidth > originalImageWidth || frameHeight > originalImageHeight) {
                val fitHorizontallyScaleFactor = frameWidth / originalImageWidth
                val fitVerticallyScaleFactor = frameHeight / originalImageHeight
                usedScaleFactor =
                    Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor)
            }
            val newImageWidth = originalImageWidth * usedScaleFactor
            val newImageHeight = originalImageHeight * usedScaleFactor
            val matrix = imageMatrix
            matrix.setScale(usedScaleFactor, usedScaleFactor, 0f, 0f)
            when (mMatrixType) {
                MatrixCropType.TOP_CENTER -> matrix.postTranslate(
                    (frameWidth - newImageWidth) / 2,
                    0f
                )
                MatrixCropType.TOP_RIGHT -> matrix.postTranslate(
                    0f,
                    0f
                )
                MatrixCropType.TOP_LEFT -> matrix.postTranslate(
                    frameWidth - newImageWidth,
                    0f
                )
                MatrixCropType.BOTTOM_CENTER -> matrix.postTranslate(
                    (frameWidth - newImageWidth) / 2,
                    frameHeight - newImageHeight
                )
                MatrixCropType.BOTTOM_LEFT -> matrix.postTranslate(
                    frameWidth - newImageWidth,
                    frameHeight - newImageHeight
                )
                MatrixCropType.BOTTOM_RIGHT -> matrix.postTranslate(
                    0f,
                    frameHeight - newImageHeight
                )
            }
            imageMatrix = matrix
        }
        return super.setFrame(frameLeft, frameTop, frameRight, frameBottom)
    }

    init {
        // get attributes
        if (attrs != null) {
            val a =
                context.theme.obtainStyledAttributes(attrs, R.styleable.ImageScaleView, 0, 0)
            mMatrixType = try {
                MatrixCropType.fromValue(
                    a.getInteger(
                        R.styleable.ImageScaleView_matrixType,
                        0
                    )
                )
            } finally {
                a.recycle()
            }
        }
    }
}
