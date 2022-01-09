package pt.hventura.loadapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var mLoadColor by Delegates.notNull<Int>()
    private var mLoadCircleColor by Delegates.notNull<Int>()
    private var mButtonText: String = resources.getString(R.string.button_name)


    private val valueAnimator by lazy {
        ValueAnimator.ofFloat(0f, widthSize.toFloat())
    }


    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> buttonStateClicked()
            ButtonState.Loading -> buttonStateLoading()
            ButtonState.Completed -> buttonStateCompleted()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        typeface = Typeface.create("raleway_bold", Typeface.BOLD)
    }

    init {
        // Information obtained in https://developer.android.com/training/custom-views/create-view
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,
            0
        ).apply {
            try {
                mLoadColor = getColor(
                    R.styleable.LoadingButton_loadColor,
                    resources.getColor(R.color.colorPrimaryDark, null)
                )
                mLoadCircleColor = getColor(
                    R.styleable.LoadingButton_loadCircleColor,
                    Color.YELLOW
                )
            } finally {
                recycle()
            }

        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawRectangle(it)
            drawLoadingRectangle(it)
            drawButtonText(it)
        }

    }

    private fun drawRectangle(canvas: Canvas) {
        paint.color = resources.getColor(R.color.colorPrimary, null)
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)
    }

    private fun drawLoadingRectangle(canvas: Canvas) {
        paint.color = mLoadColor
        canvas.drawRect(0f, 0f, 0f, heightSize.toFloat(), paint)
    }

    private fun drawButtonText(canvas: Canvas) {
        paint.color = Color.WHITE
        // Help from : https://stackoverflow.com/questions/15537420/how-to-center-text-in-an-android-view-class
        val positionX = (widthSize / 2).toFloat()
        val positionY = heightSize / 2 - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(mButtonText, positionX, positionY, paint)
    }

    override fun performClick(): Boolean {
        buttonState = ButtonState.Clicked
        invalidate()
        return true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun buttonStateClicked() {
        /* Testing purpose only */
        mButtonText = resources.getString(R.string.button_loading)
        invalidate()
    }

    private fun buttonStateLoading() {
        mButtonText = resources.getString(R.string.button_loading)
        invalidate()
    }

    private fun buttonStateCompleted() {
        isClickable = true
    }


}