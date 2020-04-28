package org.immuni.android.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import org.immuni.android.R

class ImmuniProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val completedPaint = Paint()
    private val inProgressPaint = Paint()
    private val separatorPaint = Paint()

    private var lastTranslation = -10000f
    private var animatedProgress: Float = 0f
    private var animator: ValueAnimator? = null

    private var H = 0f
    private var W = 0f
    private var STEP_W = 0f
    private var steps = 1
    private var currentStep = 0
    private var percentage = 0.0f
    private var type = Type.STEPS

    private val progressBackgroundColor: Int
    private val progressCompletedColor: Int
    private val progressPendingColor: Int
    private val progressSeparatorColor: Int

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ImmuniProgressView,
            0, 0).apply {

            try {
                progressBackgroundColor = getColor(R.styleable.ImmuniProgressView_progressColorBackground, 0)
                progressCompletedColor = getColor(R.styleable.ImmuniProgressView_progressColorCompleted, 0)
                progressPendingColor = getColor(R.styleable.ImmuniProgressView_progressColorPending, 0)
                progressSeparatorColor = getColor(R.styleable.ImmuniProgressView_progressColorSeparator, 0)

                completedPaint.color = progressCompletedColor
                inProgressPaint.color = progressPendingColor
                separatorPaint.color = progressSeparatorColor

            } finally {
                recycle()
            }
        }
    }

    fun init() {


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(progressBackgroundColor)

        W = width.toFloat()
        H = height.toFloat()

        when(type) {
            Type.STEPS -> {
                STEP_W = W / steps

                canvas.save()
                val target = -W + (STEP_W * (currentStep))
                val translation = target - (target - lastTranslation)*animatedProgress
                lastTranslation = translation
                canvas.translate(translation, 0f)
                canvas.drawRoundRect(-STEP_W, 0f, W, H, 0f, 0f, completedPaint)
                canvas.drawRoundRect(STEP_W * (steps-1), 0f, W, H, 0f, 0f, inProgressPaint)
                if(currentStep > 1) canvas.drawRect(STEP_W * (steps-1), 0f, STEP_W * (steps-1) + H/2, H, separatorPaint)
                canvas.restore()
            }
            Type.TIMER -> {
                canvas.save()
                val target = -W * (1-percentage)
                val translation = target - (target - lastTranslation)*animatedProgress
                lastTranslation = translation
                canvas.translate(translation, 0f)
                //canvas.drawRoundRect(-STEP_W, 0f, W, H, H/2, H/2, completedPaint)
                canvas.drawRoundRect(-STEP_W, 0f, W, H, 0f, 0f, completedPaint)
                canvas.restore()
            }
        }
    }

    fun setStep(step: Int , totalSteps: Int) {

        if(type == Type.STEPS && currentStep == step && steps == totalSteps) return

        type = Type.STEPS
        currentStep = step
        steps = totalSteps
        animateProgress()
    }

    fun setPercentage(p: Float) {
        type = Type.TIMER
        percentage = p
        animateProgress()
    }

    fun animateProgress() {
        if(animator?.isRunning == true) animator?.cancel()
        animator = ValueAnimator.ofFloat(1f, 0f)
        animator?.apply {
            interpolator = AccelerateInterpolator()
            duration = 250
            addUpdateListener {
                animatedProgress = it.animatedValue as Float
                invalidate()
            }
        }
        animator?.start()
    }

    enum class Type {
        STEPS, TIMER
    }
}