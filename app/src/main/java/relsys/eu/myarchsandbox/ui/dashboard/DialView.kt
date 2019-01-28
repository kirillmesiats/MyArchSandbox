package relsys.eu.myarchsandbox.ui.dashboard

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
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.widget.TextView


class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private lateinit var valuesLayout : RelativeLayout
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mActivePointerId = INVALID_POINTER_ID
    private var totalAngle = 0f

    init {
        post {
            val minAttr = Math.min(width, height)

            // add inner layout with values that will be rotated
            valuesLayout = RelativeLayout(context, attrs, defStyle)
            val layoutParams = LayoutParams(minAttr, minAttr/*LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT*/)
            layoutParams.addRule(CENTER_HORIZONTAL)
            valuesLayout.layoutParams = layoutParams

            val background = ShapeDrawable()

            // Specify the shape of ShapeDrawable
            background.shape = OvalShape()


            // Specify the border color of shape
            background.paint.color = Color.GREEN

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
                    val angleDegrees = 36 * (i-3)
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
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
        textView.measure(0, 0)

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        textView.layoutParams = layoutParams
//
//        val sd = ShapeDrawable()
//
//        // Specify the shape of ShapeDrawable
//        sd.shape = RectShape()
//
//        // Specify the border color of shape
//        sd.paint.color = Color.WHITE
//
//        // Set the border width
//        sd.paint.strokeWidth = 4f
//
//        // Specify the style is a Stroke
//        sd.paint.style = Paint.Style.STROKE
//        textView.background = sd

        return textView
    }

    fun doAnimate(angle : Float) {
        valuesLayout.rotation
        val animate = valuesLayout.animate()
        animate.duration = 0
        animate.interpolator = LinearInterpolator()
        animate.rotationBy(angle).start()
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        return if (mDetector.onTouchEvent(event)) {
//            true
//        } else {
//            return super.onTouchEvent(event)
//        }
//    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val DEBUG_TAG = "Gestures"
        val action: Int = event.actionMasked


        when (action) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also {pointerIndex ->
                    mLastTouchX = event.getX(pointerIndex)
                    mLastTouchY = event.getY(pointerIndex)
                }

                mActivePointerId = event.getPointerId(0)
                totalAngle = 0f

            }
            MotionEvent.ACTION_MOVE -> {
                val (x: kotlin.Float, y: kotlin.Float) = event.findPointerIndex(mActivePointerId).let { pointerIndex ->
                    event.getX(pointerIndex) to event.getY(pointerIndex)
                }

                var dx = x - mLastTouchX
                var dy = y - mLastTouchY

                mLastTouchX = x
                mLastTouchY = y

                val screenWidth = context.resources.displayMetrics.widthPixels

                val angle = 180 * dx / screenWidth
                totalAngle += angle
                println("screenWidth = $screenWidth; dx = $dx; angle = $angle")
                doAnimate(angle)
            }
            MotionEvent.ACTION_UP -> {
                val remainder = totalAngle % 36
                val angle = if (Math.abs(remainder) >= 18) {
                    if (remainder > 0) {
                        36 - Math.abs(remainder)
                    } else {
                        0 - (36 - Math.abs(remainder))
                    }
                } else {
                    0 - remainder
                }
                doAnimate(angle)
                Log.d(DEBUG_TAG, "total angle = $totalAngle; remainder = $remainder; angle = $angle; result = ${totalAngle + angle}")

                //Log.d(DEBUG_TAG, "Action was UP")

            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

//        val mPaint = Paint()
//
//        mPaint.color = Color.WHITE
//        mPaint.style = Paint.Style.STROKE
//        mPaint.strokeWidth = 4F
//        mPaint.isAntiAlias = true
//
//        val minAttr = Math.min(width, height)
//        val radius = minAttr / 2
//
//        println(width)
//        println(height)
//        println(radius)
//
//        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius.toFloat() /*+ mPadding - 10*/, mPaint)

    }
}