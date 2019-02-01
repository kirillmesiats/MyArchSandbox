package relsys.eu.myarchsandbox.ui.dashboard

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

    private val mPaint = Paint()
    private val mRectF = RectF()
    private lateinit var valuesLayout: RelativeLayout
    private var mLastTouchX = 0f
    private var mActivePointerId = INVALID_POINTER_ID
    // current angle on which the disk is turned
    private var currentAngle = 0f
    private val angles = ArrayList<Float>()
    private val labels = ArrayList<TextView>()
    //    private var maxClockwiseAngle = 0f
//    private var maxCounterClockwiseAngle = 0f
    // index of current value in angles array
//    private var valueKey : Float
    private var currentValue = 0

    init {
        //        valueKey = 0f
//        mLastTouchX = 0f

        clipChildren = false
        clipToPadding = false

        post {
            val minAttr = Math.min(width, height)

            // add inner layout with values that will be rotated
            valuesLayout = RelativeLayout(context, attrs, defStyle)
            val layoutParams = LayoutParams(minAttr, minAttr)
            layoutParams.addRule(CENTER_HORIZONTAL)
            valuesLayout.layoutParams = layoutParams
            valuesLayout.pivotX = (width / 2).toFloat()
            valuesLayout.pivotY = height.toFloat()
            valuesLayout.clipChildren = false
            valuesLayout.clipToPadding = false

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
                val cy = valuesLayout.height

                for (i in 0..9) {
                    val textView = buildTextView("${i + 1}X")
                    valuesLayout.addView(textView)
                    val angleDegrees = 36 * i
                    angles.add(0 - angleDegrees.toFloat())
                    labels.add(textView)
                    val angle = Math.toRadians((angleDegrees - 90).toDouble())
                    textView.x =
                            (cx + Math.cos(angle) * (radius - textView.measuredHeight / 2) - textView.measuredWidth / 2).toFloat()
                    textView.y =
                            (cy + Math.sin(angle) * (radius - textView.measuredHeight / 2) - textView.measuredHeight / 2).toFloat()
                    textView.rotation = angleDegrees.toFloat()
                }
            }
        }
    }

    private fun buildTextView(value: String): TextView {
        val textView = TextView(context)
        textView.text = value
        textView.setTextColor(Color.WHITE)
        if (labels.size == 0) {
            textView.alpha = 1f
            textView.scaleX = 1.5f
            textView.scaleY = 1.5f
        } else {
            textView.alpha = 0.5f
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.dialValuesFontSize))
        textView.measure(0, 0)

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        textView.layoutParams = layoutParams
        return textView
    }

    private fun doAnimate(angleParam: Float) {

        var angle = angleParam

        if (angle < 0) {
            // we cannot rotate counterclockwise anymore
            if (currentAngle == angles[angles.size - 1]) {
                return
            }
            if (currentAngle + angle < angles[angles.size - 1]) {
                angle = angles[angles.size - 1] - currentAngle
            }
        } else {
            // we cannot rotate clockwise anymore
            if (currentAngle == angles[0]) {
                return
            }
            if (currentAngle + angle > angles[0]) {
                angle = angles[0] - currentAngle
            }
        }

        currentAngle += angle

        val animations = arrayListOf<Animator>()

        val rotationAnimator = ObjectAnimator.ofFloat(
            valuesLayout, "rotation", valuesLayout.rotation,
            valuesLayout.rotation + angle
        )
        rotationAnimator.duration = 0
        animations.add(rotationAnimator)

        val newValueKey = Math.abs(Math.round(currentAngle / 36))
        if (newValueKey != currentValue) {
            val newKeyAnimatorX = ObjectAnimator.ofFloat(labels[newValueKey], "scaleX", 1f, 1.5f)
            newKeyAnimatorX.duration = 50
            animations.add(newKeyAnimatorX)

            val newKeyAnimatorY = ObjectAnimator.ofFloat(labels[newValueKey], "scaleY", 1f, 1.5f)
            newKeyAnimatorY.duration = 50
            animations.add(newKeyAnimatorY)

            val newKeyAlpha = ObjectAnimator.ofFloat(labels[newValueKey], "alpha", 0.5f, 1f)
            newKeyAlpha.duration = 50
            animations.add(newKeyAlpha)

            val valueKeyAnimatorX = ObjectAnimator.ofFloat(labels[currentValue], "scaleX", 1.5f, 1f)
            valueKeyAnimatorX.duration = 50
            animations.add(valueKeyAnimatorX)

            val valueKeyAnimatorY = ObjectAnimator.ofFloat(labels[currentValue], "scaleY", 1.5f, 1f)
            valueKeyAnimatorY.duration = 50
            animations.add(valueKeyAnimatorY)

            val valueKeyAlpha = ObjectAnimator.ofFloat(labels[currentValue], "alpha", 1f, 0.5f)
            valueKeyAlpha.duration = 50
            animations.add(valueKeyAlpha)

            currentValue = newValueKey
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animations)
        animatorSet.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action: Int = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also { pointerIndex ->
                    mLastTouchX = event.getX(pointerIndex)
                }

                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.findPointerIndex(mActivePointerId).let { pointerIndex -> event.getX(pointerIndex) }

                val dx = x - mLastTouchX

                mLastTouchX = x

                val screenWidth = context.resources.displayMetrics.widthPixels

                val angle = 180 * dx / screenWidth

                doAnimate(angle)
            }
            MotionEvent.ACTION_UP -> {
                val angleUntil = Math.round(currentAngle / 36).toFloat() * 36
                doAnimate(angleUntil - currentAngle)
            }
        }
        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val minAttr = Math.min(width, height)
        val cx = width / 2
        val cy = height

        val outerRadius = (minAttr / 2 - resources.getDimensionPixelSize(R.dimen.dialValuesFontSize) * 2).toFloat()

        mPaint.reset()
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
        //canvas.drawCircle(cx.toFloat(), cy.toFloat(), outerRadius.toFloat(), mPaint)

        mRectF.set(cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius)
        canvas.drawArc(mRectF, 0f, -180f, true, mPaint)

        mPaint.reset()

        mPaint.color = Color.parseColor("#8B74A1")
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 3F
        mPaint.isAntiAlias = true

        val scaleMarksRadius = outerRadius - resources.getDimensionPixelSize(R.dimen.dialScaleMarksPadding)
        val scaleMarksSmallRadius = scaleMarksRadius - resources.getDimensionPixelSize(R.dimen.dialScaleMarksSmallSize)
        val scaleMarksBigRadius = scaleMarksRadius - resources.getDimensionPixelSize(R.dimen.dialScaleMarksBigSize)

        val angleBetweenScaleMarks = (360 / 10).toFloat() / 8
        var currentAngle = -90F
        for (i in 0 until 80) {
            if (currentAngle <= 0 || currentAngle >= 180) {
                val angle = Math.toRadians(currentAngle.toDouble())
                val x1 = cx + Math.cos(angle) * scaleMarksRadius
                val y1 = cy + Math.sin(angle) * scaleMarksRadius

                val x2y2 = if (i % 8 == 0) {
                    // big scale mark
                    cx + Math.cos(angle) * scaleMarksBigRadius to
                            cy + Math.sin(angle) * scaleMarksBigRadius
                } else {
                    cx + Math.cos(angle) * scaleMarksSmallRadius to
                            cy + Math.sin(angle) * scaleMarksSmallRadius
                }
                canvas.drawLine(x1.toFloat(), y1.toFloat(), x2y2.first.toFloat(), x2y2.second.toFloat(), mPaint)
            }

            currentAngle += angleBetweenScaleMarks
        }

        mPaint.reset()
        mPaint.color = Color.parseColor("#0D502E73")
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true

        val innerRadius = (outerRadius - resources.getDimensionPixelSize(R.dimen.dialInnerCirclePadding)).toFloat()
        // canvas.drawCircle(cx.toFloat(), cy.toFloat(), innerRadius, mPaint)
        mRectF.set(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius)
        canvas.drawArc(mRectF, 0f, -180f, true, mPaint)

//        canvas.drawRect(0f, (height / 2).toFloat(), width.toFloat(), height.toFloat(), mPaint)

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