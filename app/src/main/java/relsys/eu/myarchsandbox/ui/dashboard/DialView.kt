package relsys.eu.myarchsandbox.ui.dashboard

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.widget.RelativeLayout
import android.widget.TextView
import relsys.eu.myarchsandbox.R


class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    val DEBUG_TAG = "Gestures"

    private lateinit var valuesLayout : RelativeLayout
    private var mLastTouchX : Float
    private var mActivePointerId = INVALID_POINTER_ID
    // current angle on which the disk is turned
    private var currentAngle : Float
    private val angles = linkedMapOf<Float, TextView>()
    private var maxClockwiseAngle = 0f
    private var maxCounterClockwiseAngle = 0f
    // index of current value in angles array
    private var valueKey : Float
    private var value = 3

    init {
        valueKey = 0f
        currentAngle = 0f
        mLastTouchX = 0f

        post {
            val minAttr = Math.min(width, height)

            // add inner layout with values that will be rotated
            valuesLayout = RelativeLayout(context, attrs, defStyle)
            val layoutParams = LayoutParams(minAttr, minAttr)
            layoutParams.addRule(CENTER_HORIZONTAL)
            valuesLayout.layoutParams = layoutParams

            val background = ShapeDrawable()

            // Specify the shape of ShapeDrawable
            background.shape = OvalShape()


            // Specify the border color of shape
            background.paint.color = Color.TRANSPARENT

            // Set the border width
//            background.paint.strokeWidth = 4f

            // Specify the style is a Stroke
            background.paint.style = Paint.Style.FILL
            valuesLayout.background = background
            addView(valuesLayout)

            valuesLayout.post {
                val radius = minAttr / 2
                val cx = valuesLayout.width / 2
                val cy = valuesLayout.height / 2

                for (i in 1..10) {
                    val textView = buildTextView("${i}X")
                    valuesLayout.addView(textView)
                    val angleDegrees = 36 * (i-value)
                    val key = 0 - angleDegrees.toFloat()
                    angles[key] = textView
                    if (i == 1) {
                        maxClockwiseAngle = key
                    } else if (i == 10) {
                        maxCounterClockwiseAngle = key
                    }
                    val angle = Math.toRadians((angleDegrees - 90).toDouble())
                    textView.x = (cx + Math.cos(angle) * (radius - textView.measuredHeight / 2) - textView.measuredWidth / 2).toFloat()
                    textView.y = (cy + Math.sin(angle) * (radius - textView.measuredHeight / 2) - textView.measuredHeight / 2).toFloat()
                    textView.rotation = angleDegrees.toFloat()
                }
            }
        }
    }

    private fun buildTextView(value : String) : TextView {
        val textView = TextView(context)
        textView.text = value
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.dialValuesFontSize))
        textView.measure(0, 0)

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        textView.layoutParams = layoutParams
        return textView
    }

    private fun doAnimate(angleParam : Float) {

        var angle = angleParam

        if (angle < 0) {
            // we cannot rotate counterclockwise anymore
            if (currentAngle == maxCounterClockwiseAngle) {
                return
            }
            if (currentAngle + angle < maxCounterClockwiseAngle) {
                angle = maxCounterClockwiseAngle - currentAngle
            }
        } else {
            // we cannot rotate clockwise anymore
            if (currentAngle == maxClockwiseAngle) {
                return
            }
            if (currentAngle + angle > maxClockwiseAngle) {
                angle = maxClockwiseAngle - currentAngle
            }
        }

        currentAngle += angle

        val animations = arrayListOf<Animator>()

        val rotationAnimator = ObjectAnimator.ofFloat(valuesLayout, "rotation", valuesLayout.rotation,
            valuesLayout.rotation + angle)
        rotationAnimator.duration = 0
        animations.add(rotationAnimator)

        val newValueKey = Math.round(currentAngle / 36).toFloat() * 36
        if (newValueKey != valueKey) {
            val newKeyAnimatorX = ObjectAnimator.ofFloat(angles[newValueKey], "scaleX", 1f, 1.5f)
            newKeyAnimatorX.duration = 50
            animations.add(newKeyAnimatorX)

            val newKeyAnimatorY = ObjectAnimator.ofFloat(angles[newValueKey], "scaleY", 1f, 1.5f)
            newKeyAnimatorY.duration = 50
            animations.add(newKeyAnimatorY)

            val valueKeyAnimatorX = ObjectAnimator.ofFloat(angles[valueKey], "scaleX", 1.5f, 1f)
            valueKeyAnimatorX.duration = 50
            animations.add(valueKeyAnimatorX)

            val valueKeyAnimatorY = ObjectAnimator.ofFloat(angles[valueKey], "scaleY", 1.5f, 1f)
            valueKeyAnimatorY.duration = 50
            animations.add(valueKeyAnimatorY)

            Log.d(DEBUG_TAG, "current angle = $currentAngle, valueKey = $valueKey, newValueKey = $newValueKey")
            valueKey = newValueKey
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animations)
        animatorSet.start()
    }

    fun doAnimateScale(textView: TextView) {
        textView.animate().scaleXBy(2.0f).start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action: Int = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also {pointerIndex ->
                    mLastTouchX = event.getX(pointerIndex)
                }

                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val (x: kotlin.Float, y: kotlin.Float) = event.findPointerIndex(mActivePointerId).let { pointerIndex ->
                    event.getX(pointerIndex) to event.getY(pointerIndex)
                }

                val dx = x - mLastTouchX

                mLastTouchX = x

                val screenWidth = context.resources.displayMetrics.widthPixels

                val angle = 180 * dx / screenWidth

                doAnimate(angle)
            }
            MotionEvent.ACTION_UP -> {
                val remainder = currentAngle % 36
                val angle = if (Math.abs(remainder) >= 18) {
                    if (remainder > 0) {
                        36 - remainder
                    } else {
                        0 - (36 - Math.abs(remainder))
                    }
                } else {
                    0 - remainder
                }
                doAnimate(angle)

                Log.d(DEBUG_TAG, "total angle = $currentAngle; remainder = $remainder; angle = $angle; result = ${currentAngle + angle}")
            }
        }
        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val mPaint = Paint()

        val minAttr = Math.min(width, height)
        val cx = width / 2
        val cy = height / 2

        val outerRadius = minAttr / 2 - resources.getDimensionPixelSize(R.dimen.dialValuesFontSize) * 2

        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), outerRadius.toFloat(), mPaint)

        mPaint.reset()

        mPaint.color = Color.BLACK
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 3F
        mPaint.isAntiAlias = true

        val scaleMarksRadius = outerRadius - resources.getDimensionPixelSize(R.dimen.dialScaleMarksPadding)
        val scaleMarksSmallRadius = scaleMarksRadius - resources.getDimensionPixelSize(R.dimen.dialScaleMarksSmallSize)
        val scaleMarksBigRadius = scaleMarksRadius - resources.getDimensionPixelSize(R.dimen.dialScaleMarksBigSize)

        val angleBetweenScaleMarks = (360 / 10).toFloat() / 8
        var currentAngle = 0F
        for (i in 0 until 80) {
            Log.d(DEBUG_TAG, currentAngle.toString())
            val angle = Math.toRadians(currentAngle.toDouble() - 90)
            val x1 = cx + Math.cos(angle) * scaleMarksRadius.toFloat()
            val y1 = cy + Math.sin(angle) * scaleMarksRadius.toFloat()

            val x2y2 = if (i % 8 == 0) {
                // big scale mark
                cx + Math.cos(angle) * scaleMarksBigRadius.toFloat() to
                        cy + Math.sin(angle) * scaleMarksBigRadius.toFloat()
            } else {
                cx + Math.cos(angle) * scaleMarksSmallRadius.toFloat() to
                        cy + Math.sin(angle) * scaleMarksSmallRadius.toFloat()
            }
            canvas.drawLine(x1.toFloat(), y1.toFloat(), x2y2.first.toFloat(), x2y2.second.toFloat(), mPaint)

            currentAngle += angleBetweenScaleMarks
        }

//        mPaint.reset()
//        mPaint.color = Color.BLACK
//        mPaint.style = Paint.Style.STROKE
//        mPaint.strokeWidth = 3F
//        mPaint.isAntiAlias = true
//
//        canvas.drawLine((width / 2).toFloat(), 0f, (width / 2).toFloat(),  height.toFloat(), mPaint)
//        canvas.drawLine(0f, (height / 2).toFloat(),  width.toFloat(), (height / 2).toFloat(), mPaint)
    }
}