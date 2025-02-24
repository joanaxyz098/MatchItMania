package views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.csit284.matchitmania.R
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class MView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    var cornerRadius: Float = 50f
    var borderColor: Int = Color.BLACK
    var borderWidth: Float = 6f
    var backColor: Int = Color.WHITE
    var gradientColors: IntArray? = null
    var gradientAngle: Float = 0f  // Gradient angle in degrees (0° = left to right)

    private var gradientShader: LinearGradient? = null

    init {
        attrs?.let { attributeSet ->
            val typedArray = context.obtainStyledAttributes(
                attributeSet,
                R.styleable.MView
            )
            try {
                cornerRadius = typedArray.getDimension(R.styleable.MView_cornerRadius, cornerRadius)
                borderColor = typedArray.getColor(R.styleable.MView_borderColor, borderColor)
                borderWidth = typedArray.getDimension(R.styleable.MView_borderWidth, borderWidth)
                backColor = typedArray.getColor(R.styleable.MView_backColor, backColor)

                val gradientColorsId = typedArray.getResourceId(R.styleable.MView_gradientColors, 0)
                if (gradientColorsId != 0) {
                    gradientColors = resources.getIntArray(gradientColorsId)
                }
                gradientAngle = typedArray.getFloat(R.styleable.MView_gradientAngle, 0f) % 360
            } finally {
                typedArray.recycle()
            }
        }

        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth
    }

    private fun updateGradient() {
        if (gradientColors != null && width > 0 && height > 0) {
            val radians = Math.toRadians(gradientAngle.toDouble())
            val centerX = rect.centerX()
            val centerY = rect.centerY()
            val halfDiagonal = hypot(rect.width(), rect.height()) / 2

            val startX = (centerX - halfDiagonal * cos(radians)).toFloat()
            val startY = (centerY - halfDiagonal * sin(radians)).toFloat()
            val endX = (centerX + halfDiagonal * cos(radians)).toFloat()
            val endY = (centerY + halfDiagonal * sin(radians)).toFloat()

            gradientShader = LinearGradient(
                startX, startY, endX, endY,
                gradientColors!!, null, Shader.TileMode.CLAMP
            )
            paint.shader = gradientShader
        } else {
            paint.shader = null
            paint.color = backColor
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(borderWidth / 2, borderWidth / 2, width - borderWidth / 2, height - borderWidth / 2)
        updateGradient()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
    }
}