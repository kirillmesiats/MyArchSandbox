package relsys.eu.myarchsandbox.ui.dashboard

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.SoundEffectConstants
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
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
    private lateinit var centerTitleLbl: TextView
    private lateinit var centerMsgLbl: TextView
    private var mLastTouchX = 0f
    private var mActivePointerId = INVALID_POINTER_ID
    // current angle on which the disk is turned
    private var currentAngle = 0f
    private val angles = ArrayList<Float>()
    private val labels = ArrayList<TextView>()
    private var currentValue = 0
    private val mode = Mode.ARC
    private val screenWidth = context.resources.displayMetrics.widthPixels
    private var cx = 0
    private var cy = 0
    private var scaleOuterRadius = 0f
    private var scaleInnerRadius = 0f
    private val mShadowPaint = Paint(0).apply {
        color = 0x101010
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        style = Paint.Style.FILL
    }

    init {
        clipChildren = false
        clipToPadding = false
        isSoundEffectsEnabled = true
        isHapticFeedbackEnabled = true

        post {
            val minAttr = if (mode == Mode.ARC) {
                width
            } else {
                Math.min(width, height)
            }

            cx = width / 2
            cy = if (mode == Mode.ARC) {
                height
            } else {
                height / 2
            }

            scaleOuterRadius =
                    (minAttr / 2 - resources.getDimensionPixelSize(R.dimen.dialValuesFontSize) * 1.5).toFloat()
            scaleInnerRadius = scaleOuterRadius - resources.getDimensionPixelSize(R.dimen.dialInnerCirclePadding)

            // add inner layout with values that will be rotated
            valuesLayout = RelativeLayout(context, attrs, defStyle)
            val layoutParams = LayoutParams(minAttr, minAttr)
            layoutParams.addRule(CENTER_HORIZONTAL)
            valuesLayout.layoutParams = layoutParams
            if (mode == Mode.ARC) {
                valuesLayout.pivotX = (width / 2).toFloat()
                valuesLayout.pivotY = height.toFloat()
            }
            valuesLayout.clipChildren = false
            valuesLayout.clipToPadding = false
            addView(valuesLayout)

            valuesLayout.post {
                val radius = minAttr / 2

                for (i in 0..9) {
                    val textView = buildValueTextView("${i + 1}X")
                    valuesLayout.addView(textView)
                    val angleDegrees = 36 * i
                    angles.add(0 - angleDegrees.toFloat())
                    labels.add(textView)
                    val angle = Math.toRadians((angleDegrees - 90).toDouble())

                    textView.x = (cx + Math.cos(angle) * radius - textView.measuredWidth / 2).toFloat()
                    textView.y = (cy + Math.sin(angle) * radius - textView.measuredHeight / 2).toFloat()
                    textView.rotation = angleDegrees.toFloat()
                }
            }
            addCenterTitleAndMsg(context)
        }
    }

    private fun addCenterTitleAndMsg(context: Context) {
        val gd = GradientDrawable()
        gd.setColor(-0xff0100) // Changes this drawbale to use a single color instead of a gradient
        gd.setStroke(1, -0x1000000)

        val squareSideHalf = (scaleInnerRadius / Math.sqrt(2.0)).toFloat()
        // lets add current value label and comment
        centerTitleLbl = AppCompatTextView(context)
//        centerTitleLbl.background = gd
        centerTitleLbl.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        centerTitleLbl.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans), Typeface.NORMAL)
        centerTitleLbl.setTextColor(Color.parseColor("#564467"))
        centerTitleLbl.layoutParams = LayoutParams(squareSideHalf.toInt() * 2, (squareSideHalf * 0.75f).toInt())
        centerTitleLbl.x = cx - squareSideHalf
        centerTitleLbl.y = cy - squareSideHalf * 1.25f
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            centerTitleLbl, 1, (resources.getDimensionPixelSize(R.dimen.dialValuesFontSize) * 1.33f).toInt(),
            1, TypedValue.COMPLEX_UNIT_PX
        )
        addView(centerTitleLbl)

        centerMsgLbl = AppCompatTextView(context)
//        centerMsgLbl.background = gd
        centerMsgLbl.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        centerMsgLbl.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans), Typeface.NORMAL)
        centerMsgLbl.setTextColor(Color.parseColor("#C4B8CC"))
        centerMsgLbl.layoutParams = LayoutParams(squareSideHalf.toInt() * 2, squareSideHalf.toInt() / 2)
        centerMsgLbl.x = centerTitleLbl.x
        centerMsgLbl.y = centerTitleLbl.y + centerTitleLbl.layoutParams.height
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            centerMsgLbl, 1, resources.getDimensionPixelSize(R.dimen.dialValuesFontSize) / 2,
            1, TypedValue.COMPLEX_UNIT_PX
        )
        addView(centerMsgLbl)
    }

    private fun buildValueTextView(value: String): TextView {
        val textView = TextView(context)
        textView.text = value
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimensionPixelSize(R.dimen.dialValuesFontSize).toFloat()
        )
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans), Typeface.NORMAL)
        textView.measure(0, 0)
        if (labels.size == 0) {
            textView.alpha = 1f
            textView.scaleX = 1.33f
            textView.scaleY = 1.33f
        } else {
            textView.alpha = 0.5f
        }

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
            val newKeyAnimatorX = ObjectAnimator.ofFloat(labels[newValueKey], "scaleX", 1f, 1.33f)
            newKeyAnimatorX.duration = 100
            animations.add(newKeyAnimatorX)

            val newKeyAnimatorY = ObjectAnimator.ofFloat(labels[newValueKey], "scaleY", 1f, 1.33f)
            newKeyAnimatorY.duration = 100
            animations.add(newKeyAnimatorY)

            val newKeyAlpha = ObjectAnimator.ofFloat(labels[newValueKey], "alpha", 0.5f, 1f)
            newKeyAlpha.duration = 100
            animations.add(newKeyAlpha)

            val valueKeyAnimatorX = ObjectAnimator.ofFloat(labels[currentValue], "scaleX", 1.33f, 1f)
            valueKeyAnimatorX.duration = 100
            animations.add(valueKeyAnimatorX)

            val valueKeyAnimatorY = ObjectAnimator.ofFloat(labels[currentValue], "scaleY", 1.33f, 1f)
            valueKeyAnimatorY.duration = 100
            animations.add(valueKeyAnimatorY)

            val valueKeyAlpha = ObjectAnimator.ofFloat(labels[currentValue], "alpha", 1f, 0.5f)
            valueKeyAlpha.duration = 100
            animations.add(valueKeyAlpha)

            currentValue = newValueKey
            playSoundEffect(SoundEffectConstants.CLICK)
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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
        mPaint.reset()
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true

        if (mode == Mode.ARC) {
            mRectF.set(cx - scaleOuterRadius, cy - scaleOuterRadius, cx + scaleOuterRadius, cy + scaleOuterRadius)
            canvas.drawArc(mRectF, 0f, -180f, true, mPaint)
        } else {
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), scaleOuterRadius, mPaint)
        }

        mPaint.reset()
        mPaint.color = Color.parseColor("#8B74A1")
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 3F
        mPaint.isAntiAlias = true

        val scaleMarksRadius = scaleOuterRadius - resources.getDimensionPixelSize(R.dimen.dialScaleMarksPadding)
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

        if (mode == Mode.ARC) {
            mRectF.set(cx - scaleInnerRadius, cy - scaleInnerRadius, cx + scaleInnerRadius, cy + scaleInnerRadius)
            canvas.drawArc(mRectF, 0f, -180f, true, mPaint)
        } else {
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), scaleInnerRadius, mPaint)
        }
        super.dispatchDraw(canvas)
    }

    fun setCenterTitle(title: String) {
        post {
            centerTitleLbl.text = title
        }
    }

    fun setCenterMsg(msg: String) {
        post {
            centerMsgLbl.text = msg
        }
    }

    private enum class Mode {
        CIRCLE,
        ARC
    }
}