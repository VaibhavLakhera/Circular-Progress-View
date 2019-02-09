package com.vaibhavlakhera.circularprogressview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.InterpolatorRes
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import java.text.NumberFormat

class CircularProgressView : View {
    companion object {
        const val PROGRESS_TEXT_TYPE_PROGRESS = 0
        const val PROGRESS_TEXT_TYPE_PERCENT = 1

        private const val KEY_STATE = "KEY_STATE"

        private const val KEY_TOTAL_VALUE = "KEY_TOTAL_VALUE"
        private const val KEY_TOTAL_COLOR = "KEY_TOTAL_COLOR"
        private const val KEY_TOTAL_WIDTH = "KEY_TOTAL_WIDTH"

        private const val KEY_PROGRESS_VALUE = "KEY_PROGRESS_VALUE"
        private const val KEY_PROGRESS_COLOR = "KEY_PROGRESS_COLOR"
        private const val KEY_PROGRESS_WIDTH = "KEY_PROGRESS_WIDTH"
        private const val KEY_PROGRESS_ROUND_CAP = "KEY_PROGRESS_ROUND_CAP"
        private const val KEY_PROGRESS_INTERPOLATOR_RES_ID = "KEY_PROGRESS_INTERPOLATOR_RES_ID"

        private const val KEY_PROGRESS_TEXT_ENABLED = "KEY_PROGRESS_TEXT_ENABLED"
        private const val KEY_PROGRESS_TEXT_TYPE = "KEY_PROGRESS_TEXT_TYPE"
        private const val KEY_PROGRESS_TEXT_SIZE = "KEY_PROGRESS_TEXT_SIZE"
        private const val KEY_PROGRESS_TEXT_COLOR = "KEY_PROGRESS_TEXT_COLOR"

        private const val KEY_FILL_COLOR = "KEY_FILL_COLOR"
        private const val KEY_START_ANGLE = "KEY_START_ANGLE"
        private const val KEY_ANIMATE = "KEY_ANIMATE"
        private const val KEY_ANIMATE_DURATION = "KEY_ANIMATE_DURATION"
    }

    private val paintTotal = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintProgressText = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)

    private val percentFormat by lazy { NumberFormat.getPercentInstance() }

    private val circleBounds = RectF()
    private var centerX: Float = 0f
    private var centerY: Float = 0f

    private var totalValue: Int = 100
    private var totalColor: Int = 0
    private var totalWidth: Float = 16f

    private var progressValue: Int = 0
    private var progressColor: Int = 0
    private var progressWidth: Float = 16f
    private var progressRoundCap: Boolean = false

    private var progressTextEnabled: Boolean = false
    private var progressTextType: Int = PROGRESS_TEXT_TYPE_PROGRESS
    private var progressTextSize: Float = 0f
    private var progressTextColor: Int = 0
    private var progressInterpolatorResId = android.R.anim.linear_interpolator
    private var progressInterpolator: Interpolator = LinearInterpolator()

    private var fillColor: Int = 0

    private var startAngle: Float = 270f

    private var animate = false
    private var animateDuration: Long = 300     // In milliseconds
    private var progressAnimator: ValueAnimator? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView)

            totalValue = typedArray.getInt(R.styleable.CircularProgressView_totalValue, 100)
            totalColor = typedArray.getColor(R.styleable.CircularProgressView_totalColor, 0)
            totalWidth = typedArray.getDimensionPixelSize(R.styleable.CircularProgressView_totalWidth, 16).toFloat()

            progressValue = typedArray.getInt(R.styleable.CircularProgressView_progressValue, 0)
            progressColor = typedArray.getColor(R.styleable.CircularProgressView_progressColor, 0)
            progressWidth = typedArray.getDimensionPixelSize(R.styleable.CircularProgressView_progressWidth, 16).toFloat()
            progressRoundCap = typedArray.getBoolean(R.styleable.CircularProgressView_progressRoundCap, false)

            progressTextEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_progressTextEnabled, false)
            progressTextType = typedArray.getInt(R.styleable.CircularProgressView_progressTextType, PROGRESS_TEXT_TYPE_PROGRESS)
            progressTextSize = typedArray.getDimensionPixelSize(R.styleable.CircularProgressView_progressTextSize, 0).toFloat()
            progressTextColor = typedArray.getColor(R.styleable.CircularProgressView_progressTextColor, 0)

            // Set default to linear interpolator
            val interpolatorResId = typedArray.getResourceId(R.styleable.CircularProgressView_progressInterpolator, android.R.anim.linear_interpolator)
            progressInterpolatorResId = interpolatorResId
            progressInterpolator = AnimationUtils.loadInterpolator(context, interpolatorResId)

            fillColor = typedArray.getColor(R.styleable.CircularProgressView_fillColor, 0)

            startAngle = typedArray.getFloat(R.styleable.CircularProgressView_startAngle, 270f)
            animate = typedArray.getBoolean(R.styleable.CircularProgressView_animate, false)
            animateDuration = typedArray.getInt(R.styleable.CircularProgressView_animateDuration, 300).toLong()

            typedArray.recycle()
        }

        // Set the valid progress value
        progressValue = getValidProgressValue(progressValue)

        setupPaint()
    }

    private fun setupPaint() {
        paintTotal.style = Paint.Style.STROKE
        paintTotal.color = totalColor
        paintTotal.strokeWidth = totalWidth

        paintProgress.style = Paint.Style.STROKE
        paintProgress.color = progressColor
        paintProgress.strokeWidth = progressWidth
        paintProgress.strokeCap = if (progressRoundCap) Paint.Cap.ROUND else Paint.Cap.BUTT

        paintProgressText.textSize = progressTextSize
        paintProgressText.color = progressTextColor
        paintProgressText.textAlign = Paint.Align.CENTER

        paintFill.style = Paint.Style.FILL
        paintFill.color = fillColor
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        updateBounds()
        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()
    }

    private fun updateBounds() {
        /*
        * Padding will always be the max value out of progress width and total width
        * so that drawing will begin at the edge of the view.
        * */
        val padding = Math.max(progressWidth, totalWidth) / 2
        circleBounds.apply {
            left = padding
            top = padding
            right = width - padding
            bottom = height - padding
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw fill color circle if color exists
        if (fillColor != 0) {
            // Calculate padding for fill color to make sure it is always within the total progress circle
            val padding = if (totalWidth >= progressWidth) {
                totalWidth
            } else {
                (progressWidth / 2) + (totalWidth / 2)
            }
            val radius = centerX - padding + 1  // Adding 1 to fill the tiny gap that is left
            canvas.drawCircle(centerX, centerY, radius, paintFill)
        }

        // Draw the progress text if it is enabled
        if (progressTextEnabled) {
            val progressText: String = when (progressTextType) {
                PROGRESS_TEXT_TYPE_PROGRESS -> progressValue.toString()
                PROGRESS_TEXT_TYPE_PERCENT -> percentFormat.format(progressValue.toFloat() / totalValue.toFloat())
                else -> ""
            }

            val yPosition = centerY - ((paintProgressText.descent() + paintProgressText.ascent()) / 2)
            canvas.drawText(progressText, centerX, yPosition, paintProgressText)
        }

        // Draw total progress
        canvas.drawOval(circleBounds, paintTotal)

        // Current progress is calculated in degrees from total and progress values
        if (totalValue != 0 && progressValue != 0 && progressValue <= totalValue) {
            val progressSweepAngle = if (totalValue == progressValue) 360f else ((360f / totalValue) * progressValue)
            canvas.drawArc(circleBounds, startAngle, progressSweepAngle, false, paintProgress)
        }
    }

    /**
     * Filter out any invalid progress value
     * */
    private fun getValidProgressValue(input: Int): Int {
        return when {
            input < 0 -> 0
            input > totalValue -> totalValue
            else -> input
        }
    }

    /**
     * Set the value of total. By default it is 100.
     * */
    fun setTotal(total: Int) {
        this.totalValue = total

        // In case if total is less than the progress, then set the progress equal to the total.
        if (total < progressValue) {
            progressValue = total
        }
        invalidate()
    }

    fun getTotal(): Int = totalValue

    fun setTotalColor(color: Int) {
        this.totalColor = color
        paintTotal.color = this.totalColor
        invalidate()
    }

    fun setTotalColorRes(@ColorRes colorRes: Int) {
        this.totalColor = ContextCompat.getColor(context, colorRes)
        paintTotal.color = this.totalColor
        invalidate()
    }

    fun setTotalWidth(widthInDp: Float) {
        this.totalWidth = dpToPx(widthInDp)
        paintTotal.strokeWidth = this.totalWidth
        updateBounds()
        invalidate()
    }

    /**
     * Set the progress to the progress view.
     * @param progress Value of progress out of the value of total
     * */
    fun setProgress(progress: Int, animate: Boolean = this.animate) {
        val validProgress = getValidProgressValue(progress)

        if (animate) {
            // Cancel any on-going animation
            progressAnimator?.cancel()

            val animator = ValueAnimator.ofInt(this.progressValue, validProgress)
            animator.interpolator = progressInterpolator
            animator.duration = animateDuration
            animator.addUpdateListener {
                // To make sure progress is valid in case of interpolator like "anticipate overshoot"
                this.progressValue = getValidProgressValue(it.animatedValue as Int)
                invalidate()
            }
            animator.start()
            progressAnimator = animator
        } else {
            this.progressValue = validProgress
            invalidate()
        }
    }

    fun getProgress(): Int = progressValue

    fun setProgressColor(color: Int) {
        this.progressColor = color
        paintProgress.color = this.progressColor
        invalidate()
    }

    fun setProgressColorRes(@ColorRes colorRes: Int) {
        this.progressColor = ContextCompat.getColor(context, colorRes)
        paintProgress.color = this.progressColor
        invalidate()
    }

    fun setProgressWidth(widthInDp: Float) {
        this.progressWidth = dpToPx(widthInDp)
        paintProgress.strokeWidth = this.progressWidth
        updateBounds()
        invalidate()
    }

    fun setProgressRoundCap(roundCap: Boolean) {
        this.progressRoundCap = roundCap
        this.paintProgress.strokeCap = if (roundCap) Paint.Cap.ROUND else Paint.Cap.BUTT
        invalidate()
    }

    fun setProgressTextEnabled(enabled: Boolean) {
        this.progressTextEnabled = enabled
        invalidate()
    }

    /**
     * Set the progress text type.
     *
     * Must be either PROGRESS_TEXT_TYPE_PROGRESS or PROGRESS_TEXT_TYPE_PERCENT
     *
     * By default it is PROGRESS_TEXT_TYPE_PROGRESS and for any invalid type it will set it to default.
     * */
    fun setProgressTextType(type: Int) {
        this.progressTextType = when (type) {
            PROGRESS_TEXT_TYPE_PROGRESS -> type
            PROGRESS_TEXT_TYPE_PERCENT -> type
            else -> PROGRESS_TEXT_TYPE_PROGRESS
        }
        invalidate()
    }

    fun setProgressTextSize(sizeInSp: Float) {
        this.progressTextSize = spToPx(sizeInSp)
        paintProgressText.textSize = this.progressTextSize
        invalidate()
    }

    fun setProgressTextColor(color: Int) {
        this.progressTextColor = color
        paintProgressText.color = this.progressTextColor
        invalidate()
    }

    fun setProgressTextColorRes(@ColorRes colorRes: Int) {
        this.progressTextColor = ContextCompat.getColor(context, colorRes)
        paintProgressText.color = this.progressTextColor
        invalidate()
    }

    /**
     * @param interpolatorResId Must be one from android.R.interpolator.*
     * */
    fun setProgressInterpolator(@InterpolatorRes interpolatorResId: Int) {
        this.progressInterpolatorResId = interpolatorResId
        progressInterpolator = AnimationUtils.loadInterpolator(context, interpolatorResId)
    }

    fun setFillColor(color: Int) {
        this.fillColor = color
        paintFill.color = this.fillColor
        invalidate()
    }

    fun setFillColorRes(@ColorRes colorRes: Int) {
        this.fillColor = ContextCompat.getColor(context, colorRes)
        paintFill.color = this.fillColor
        invalidate()
    }

    /**
     * Set the start angle in degrees for the progress. By default it is 270 so that it starts from the top.
     * */
    fun setStartAngle(angle: Float) {
        this.startAngle = angle
        invalidate()
    }

    /**
     * Enable or disable the animation while updating the progress
     * */
    fun setAnimate(animate: Boolean) {
        this.animate = animate
    }

    /**
     * Set the animate duration in milliseconds
     * */
    fun setAnimateDuration(duration: Long) {
        this.animateDuration = duration
    }

    private fun dpToPx(dp: Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun spToPx(sp: Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // To ensure the view is always circular in shape
        setMeasuredDimension(measuredWidth, measuredWidth)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()

        // View's internal state
        bundle.putParcelable(KEY_STATE, super.onSaveInstanceState())

        // Keys for total
        bundle.putInt(KEY_TOTAL_VALUE, totalValue)
        bundle.putInt(KEY_TOTAL_COLOR, totalColor)
        bundle.putFloat(KEY_TOTAL_WIDTH, totalWidth)

        // Keys for progress
        bundle.putInt(KEY_PROGRESS_VALUE, progressValue)
        bundle.putInt(KEY_PROGRESS_COLOR, progressColor)
        bundle.putFloat(KEY_PROGRESS_WIDTH, progressWidth)
        bundle.putBoolean(KEY_PROGRESS_ROUND_CAP, progressRoundCap)
        bundle.putInt(KEY_PROGRESS_INTERPOLATOR_RES_ID, progressInterpolatorResId)

        // Keys for progress text
        bundle.putBoolean(KEY_PROGRESS_TEXT_ENABLED, progressTextEnabled)
        bundle.putInt(KEY_PROGRESS_TEXT_TYPE, progressTextType)
        bundle.putFloat(KEY_PROGRESS_TEXT_SIZE, progressTextSize)
        bundle.putInt(KEY_PROGRESS_TEXT_COLOR, progressTextColor)

        // Other keys
        bundle.putInt(KEY_FILL_COLOR, fillColor)
        bundle.putFloat(KEY_START_ANGLE, startAngle)
        bundle.putBoolean(KEY_ANIMATE, animate)
        bundle.putLong(KEY_ANIMATE_DURATION, animateDuration)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            // Restore the keys of the view that we saved in the onSaveInstanceState
            totalValue = state.getInt(KEY_TOTAL_VALUE)
            totalColor = state.getInt(KEY_TOTAL_COLOR)
            totalWidth = state.getFloat(KEY_TOTAL_WIDTH)

            progressValue = state.getInt(KEY_PROGRESS_VALUE)
            progressColor = state.getInt(KEY_PROGRESS_COLOR)
            progressWidth = state.getFloat(KEY_PROGRESS_WIDTH)
            progressRoundCap = state.getBoolean(KEY_PROGRESS_ROUND_CAP)
            progressInterpolatorResId = state.getInt(KEY_PROGRESS_INTERPOLATOR_RES_ID)

            progressTextEnabled = state.getBoolean(KEY_PROGRESS_TEXT_ENABLED)
            progressTextType = state.getInt(KEY_PROGRESS_TEXT_TYPE)
            progressTextSize = state.getFloat(KEY_PROGRESS_TEXT_SIZE)
            progressTextColor = state.getInt(KEY_PROGRESS_TEXT_COLOR)

            fillColor = state.getInt(KEY_FILL_COLOR)
            startAngle = state.getFloat(KEY_START_ANGLE)
            animate = state.getBoolean(KEY_ANIMATE)
            animateDuration = state.getLong(KEY_ANIMATE_DURATION)

            progressInterpolator = AnimationUtils.loadInterpolator(context, progressInterpolatorResId)
            setupPaint()

            // Restore the view's internal state
            super.onRestoreInstanceState(state.getParcelable(KEY_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressAnimator?.cancel()
    }
}