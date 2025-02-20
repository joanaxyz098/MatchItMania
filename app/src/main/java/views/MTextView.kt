package views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import com.csit284.matchitmania.R
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class MTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {
        private var borderColor = Color.BLACK
        private var borderWidth = 0f
        private var startColor = Color.TRANSPARENT
        private var endColor = Color.TRANSPARENT
        private var cornerRadius = 0f

        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF(0f, 0f, 0f, 0f)

        private var lastWith = 0
        private var lastHeight = 0

        enum class GradientDirection {
            LEFT_TO_RIGHT,
            TOP_TO_BOTTOM,
            DIAGONAL,
            ANGLE
        }
        private var gradientDirection = GradientDirection.LEFT_TO_RIGHT
        private var gradientAngle = 0f // degrees

    init{
        attrs?.let {attributeSet ->
            val typedArray = context.obtainStyledAttributes(
                attributeSet, R.styleable.MTextView
            )
            try{
                borderColor = typedArray.getColor(
                    R.styleable.MTextView_borderColor,
                    borderColor
                )
                borderWidth = typedArray.getDimension(
                    R.styleable.MTextView_borderWidth,
                    borderWidth
                )
                startColor = typedArray.getColor(
                    R.styleable.MTextView_startColor,
                    startColor
                )
                endColor = typedArray.getColor(
                    R.styleable.MTextView_endColor,
                    endColor
                )
                cornerRadius = typedArray.getDimension(
                    R.styleable.MTextView_cornerRadius,
                    cornerRadius
                )
                gradientDirection = GradientDirection.values()[typedArray.getInt(
                    R.styleable.MTextView_gradientDirection,
                    gradientDirection.ordinal
                )]
                gradientAngle = typedArray.getFloat(
                    R.styleable.MTextView_gradientAngle,
                    gradientAngle
                )
            }finally{
                typedArray.recycle()
            }
        }
        setupPaints()
    }
    private fun setupPaints(){
        borderPaint.apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
        gradientPaint.apply {
            style = Paint.Style.FILL
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(w != lastWith || h != lastHeight){
            createGradientShader(w, h)
            lastWith = w
            lastHeight = h
        }
    }

    private fun createGradientShader(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            // Choose one of these gradient directions
            val gradient = when (gradientDirection) {
                GradientDirection.LEFT_TO_RIGHT -> LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    startColor, endColor, Shader.TileMode.CLAMP
                )
                GradientDirection.TOP_TO_BOTTOM -> LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    startColor, endColor, Shader.TileMode.CLAMP
                )
                GradientDirection.DIAGONAL -> LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    startColor, endColor, Shader.TileMode.CLAMP
                )
                GradientDirection.ANGLE -> {
                    // For custom angle in degrees
                    val angleRadians = Math.toRadians(gradientAngle.toDouble())
                    val length = hypot(width.toDouble(), height.toDouble())
                    val endX = (cos(angleRadians) * length).toFloat()
                    val endY = (sin(angleRadians) * length).toFloat()

                    LinearGradient(
                        width / 2f, height / 2f,
                        width / 2f + endX, height / 2f + endY,
                        startColor, endColor, Shader.TileMode.CLAMP
                    )
                }
            }

            gradientPaint.shader = gradient
        }
    }


    override fun onDraw(canvas: Canvas) {
        rect.set(borderWidth / 2, borderWidth / 2, width - borderWidth / 2, height - borderWidth / 2)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, gradientPaint)

        if(borderWidth > 0){
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
        }
        super.onDraw(canvas)
    }

}