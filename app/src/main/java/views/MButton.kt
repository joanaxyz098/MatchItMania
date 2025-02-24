package views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import com.csit284.matchitmania.R
import android.os.Build
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.toRect
import kotlin.math.abs


class MButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatButton(context, attrs, defStyleAttr) {

    // Background properties
    private var backColor: Int = Color.TRANSPARENT
    private var cornerRadius: Float = 20f
    private var borderColor: Int = Color.BLACK
    private var borderWidth: Float = 0f

    // Shadow properties
    private var shadowRadius: Float = 0f
    private var shadowDx: Float = 0f
    private var shadowDy: Float = 0f
    private var shadowColor: Int = Color.DKGRAY

    // Image background properties
    private var imageBackground: Drawable? = null
    private var imageScale: Float = 1f

    // Offset and scale properties
    private var backWidthScale: Float = 1f
    private var backHeightScale: Float = 1f
    private var backHorizontalOffset: Float = 0f
    private var backVerticalOffset: Float = 0f

    // Click state
    private var isPressed = false
    private var pressedAlpha = 0.7f

    // Paints
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Rects
    private val backgroundRect = RectF()
    private val imageRect = RectF()

    // Animation properties
    private var scaleAnimationDuration: Long = 100
    private var pressedScale: Float = 0.95f
    private var currentScale: Float = 1f
    private var animationStartTime: Long = 0

    init {
        attrs?.let { attributeSet ->
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MButton)
            try {
                backColor = typedArray.getColor(R.styleable.MButton_backColor, backColor)
                cornerRadius = typedArray.getDimension(R.styleable.MButton_cornerRadius, cornerRadius)
                borderColor = typedArray.getColor(R.styleable.MButton_borderColor, borderColor)
                borderWidth = typedArray.getDimension(R.styleable.MButton_borderWidth, borderWidth)
                imageBackground = typedArray.getDrawable(R.styleable.MButton_imageBackground)
                imageScale = typedArray.getFloat(R.styleable.MButton_imageScale, 1f)
                backWidthScale = typedArray.getFloat(R.styleable.MButton_backWidthScale, backWidthScale)
                backHeightScale = typedArray.getFloat(R.styleable.MButton_backHeightScale, backHeightScale)
                backHorizontalOffset = typedArray.getFloat(R.styleable.MButton_backHorizontalOffset, backHorizontalOffset)
                backVerticalOffset = typedArray.getFloat(R.styleable.MButton_backVerticalOffset, backVerticalOffset)
                shadowRadius = typedArray.getDimension(R.styleable.MButton_shadowRadius, shadowRadius)
                shadowDx = typedArray.getDimension(R.styleable.MButton_shadowDx, shadowDx)
                shadowDy = typedArray.getDimension(R.styleable.MButton_shadowDy, shadowDy)
                shadowColor = typedArray.getColor(R.styleable.MButton_shadowColor, shadowColor)
            } finally {
                typedArray.recycle()
            }
        }

        // Initialize paints
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = backColor

        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor

        shadowPaint.style = Paint.Style.FILL
        shadowPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)

        isClickable = true
        isFocusable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calculate shadow padding
        val shadowPadding = if (shadowRadius > 0) {
            maxOf(shadowRadius + abs(shadowDx), shadowRadius + abs(shadowDy))
        } else {
            0f
        }

        // Calculate available space considering shadow padding
        val availableWidth = w - (2 * shadowPadding)
        val availableHeight = h - (2 * shadowPadding)

        // Calculate scaled dimensions for background
        val width = availableWidth * backWidthScale
        val height = availableHeight * backHeightScale

        // Calculate offsets based on available space and offset parameters
        val horizontalOffset = (availableWidth - width) * backHorizontalOffset + shadowPadding
        val verticalOffset = (availableHeight - height) * backVerticalOffset + shadowPadding

        // Adjust the background rectangle to account for the border width
        val borderOffset = borderWidth / 2f
        backgroundRect.set(
            horizontalOffset + borderOffset,
            verticalOffset + borderOffset,
            horizontalOffset + width - borderOffset,
            verticalOffset + height - borderOffset
        )

        // Calculate image rectangle
        imageBackground?.let { drawable ->
            val drawableWidth = drawable.intrinsicWidth.toFloat()
            val drawableHeight = drawable.intrinsicHeight.toFloat()

            // Calculate scale to fit within view bounds while maintaining aspect ratio
            val scale = minOf(
                (w - 2 * shadowPadding) / drawableWidth,
                (h - 2 * shadowPadding) / drawableHeight
            ) * imageScale

            val scaledWidth = drawableWidth * scale
            val scaledHeight = drawableHeight * scale

            // Center the image within the view bounds
            val imageLeft = w / 2f - (scaledWidth / 2)
            val imageTop = h / 2f - (scaledHeight / 2)

            imageRect.set(
                imageLeft,
                imageTop,
                imageLeft + scaledWidth,
                imageTop + scaledHeight
            )
        }
    }

    private fun handleAnimation() {
        val currentTime = System.currentTimeMillis()

        if (isPressed) {
            if (animationStartTime == 0L) {
                animationStartTime = currentTime
            }

            val elapsedTime = (currentTime - animationStartTime).toFloat()
            val progress = (elapsedTime / scaleAnimationDuration).coerceIn(0f, 1f)
            currentScale = 1f + (pressedScale - 1f) * progress

            if (progress < 1f) {
                postInvalidateOnAnimation()
            }
        } else {
            if (currentScale != 1f) {
                if (animationStartTime == 0L) {
                    animationStartTime = currentTime
                }

                val elapsedTime = (currentTime - animationStartTime).toFloat()
                val progress = (elapsedTime / scaleAnimationDuration).coerceIn(0f, 1f)
                currentScale = pressedScale + (1f - pressedScale) * progress

                if (progress < 1f) {
                    postInvalidateOnAnimation()
                } else {
                    currentScale = 1f
                    animationStartTime = 0L
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Save the initial canvas state
        canvas.save()

        // Handle animations
        handleAnimation()

        // Apply scale transformation
        canvas.scale(currentScale, currentScale, width / 2f, height / 2f)

        // Draw shadow
        if (shadowRadius > 0) {
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, shadowPaint)
        }

        // Draw background
        if (Color.alpha(backColor) > 0) {
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)
        }

        // Draw image
        imageBackground?.let { drawable ->
            drawable.alpha = if (isPressed) (255 * pressedAlpha).toInt() else 255
            drawable.bounds = imageRect.toRect()
            drawable.draw(canvas)
        }

        // Draw border
        if (borderWidth > 0) {
            borderPaint.alpha = if (isPressed) (255 * pressedAlpha).toInt() else 255
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, borderPaint)
        }

        // Draw text
        super.onDraw(canvas)

        // Restore the canvas state
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isPointInsideButton(event.x, event.y)) {
                    isPressed = true
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val wasPressed = isPressed
                isPressed = isPointInsideButton(event.x, event.y)
                if (wasPressed != isPressed) {
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isPressed) {
                    isPressed = false
                    invalidate()
                    performClick()
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isPointInsideButton(x: Float, y: Float): Boolean {
        return backgroundRect.contains(x, y)
    }

    override fun performClick(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playSoundEffect(android.view.SoundEffectConstants.CLICK)
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
        return super.performClick()
    }
}