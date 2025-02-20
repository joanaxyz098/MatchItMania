package views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.graphics.toRect
import com.csit284.matchitmania.R

class MButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Background properties
    private var backColor: Int = Color.BLUE
    private var cornerRadius: Float = 20f
    private var borderColor: Int = Color.BLACK
    private var borderWidth: Float = 0f
    private var gradientColors: IntArray? = null
    private var gradientOrientation: Int = 0 // 0 = horizontal, 1 = vertical

    // Shadow properties
    private var shadowRadius: Float = 0f
    private var shadowDx: Float = 0f
    private var shadowDy: Float = 0f
    private var shadowColor: Int = Color.DKGRAY

    // Image background properties
    private var imageBackground: Drawable? = null
    private var imageScale: Float = 1f

    // Text properties
    private var text: String? = null
    private var textColor: Int = Color.BLACK
    private var textSize: Float = 16f
    private var textTypeface: Typeface = Typeface.DEFAULT
    private var fontFamily: String? = null
    private var textStyle: Int = Typeface.NORMAL

    // Offset and scale properties
    private var backWidthScale: Float = 1f
    private var backHeightScale: Float = 1f
    private var backHorizontalOffset: Float = 0f
    private var backVerticalOffset: Float = 0f

    // Click state
    private var isPressed = false
    private var pressedAlpha = 0.7f

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Rectangles
    private val backgroundRect = RectF()
    private val imageRect = RectF()

    // OnClickListener
    private var onClickListener: OnClickListener? = null

    init {
        // Parse attributes
        attrs?.let { attributeSet ->
            val typedArray = context.obtainStyledAttributes(
                attributeSet,
                R.styleable.MButton
            )
            try {
                backColor = typedArray.getColor(R.styleable.MButton_backColor, backColor)
                cornerRadius = typedArray.getDimension(R.styleable.MButton_cornerRadius, cornerRadius)
                borderColor = typedArray.getColor(R.styleable.MButton_borderColor, borderColor)
                borderWidth = typedArray.getDimension(R.styleable.MButton_borderWidth, borderWidth)
                imageBackground = typedArray.getDrawable(R.styleable.MButton_imageBackground)
                imageScale = typedArray.getFloat(R.styleable.MButton_imageScale, 1f)
                text = typedArray.getString(R.styleable.MButton_text)
                textColor = typedArray.getColor(R.styleable.MButton_textColor, textColor)
                textSize = typedArray.getDimension(R.styleable.MButton_textSize, textSize)
                backWidthScale = typedArray.getFloat(R.styleable.MButton_backWidthScale, backWidthScale)
                backHeightScale = typedArray.getFloat(R.styleable.MButton_backHeightScale, backHeightScale)
                backHorizontalOffset = typedArray.getFloat(R.styleable.MButton_backHorizontalOffset, backHorizontalOffset)
                backVerticalOffset = typedArray.getFloat(R.styleable.MButton_backVerticalOffset, backVerticalOffset)

                // Shadow properties
                shadowRadius = typedArray.getDimension(R.styleable.MButton_shadowRadius, shadowRadius)
                shadowDx = typedArray.getDimension(R.styleable.MButton_shadowDx, shadowDx)
                shadowDy = typedArray.getDimension(R.styleable.MButton_shadowDy, shadowDy)
                shadowColor = typedArray.getColor(R.styleable.MButton_shadowColor, shadowColor)

                // Gradient background
                val gradientColorsId = typedArray.getResourceId(R.styleable.MButton_gradientColors, 0)
                if (gradientColorsId != 0) {
                    gradientColors = resources.getIntArray(gradientColorsId)
                }
                gradientOrientation = typedArray.getInt(R.styleable.MButton_gradientOrientation, 0)

                fontFamily = typedArray.getString(R.styleable.MButton_fontFamily)
                textStyle = typedArray.getInt(R.styleable.MButton_textStyle, Typeface.NORMAL)

                // Create typeface based on font family and style
                if (fontFamily != null) {
                    textTypeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Typeface.create(fontFamily, textStyle)
                    } else {
                        when (fontFamily) {
                            "sans-serif" -> Typeface.SANS_SERIF
                            "serif" -> Typeface.SERIF
                            "monospace" -> Typeface.MONOSPACE
                            else -> Typeface.create(fontFamily, textStyle)
                        }
                    }
                } else {
                    textTypeface = Typeface.create(Typeface.DEFAULT, textStyle)
                }
            } finally {
                typedArray.recycle()
            }
        }

        // Initialize paints
        backgroundPaint.style = Paint.Style.FILL

        // Set up shadow
        if (shadowRadius > 0) {
            backgroundPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)
            setLayerType(LAYER_TYPE_SOFTWARE, backgroundPaint)
        }

        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor

        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.typeface = textTypeface
        textPaint.textAlign = Paint.Align.CENTER

        // Make view clickable and focusable
        isClickable = true
        isFocusable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calculate shadow padding
        val shadowPadding = if (shadowRadius > 0) {
            Math.max(shadowRadius + Math.abs(shadowDx), shadowRadius + Math.abs(shadowDy))
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

        // Calculate image rectangle independently of background position
        imageBackground?.let { drawable ->
            val drawableWidth = drawable.intrinsicWidth.toFloat()
            val drawableHeight = drawable.intrinsicHeight.toFloat()

            // Calculate scale to fit within view bounds while maintaining aspect ratio
            val scale = Math.min(
                (w - 2 * shadowPadding) / drawableWidth,
                (h - 2 * shadowPadding) / drawableHeight
            ) * imageScale

            val scaledWidth = drawableWidth * scale
            val scaledHeight = drawableHeight * scale

            // Center the image within the view bounds, not the background
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Apply alpha for pressed state
        val originalAlpha = backgroundPaint.alpha
        if (isPressed) {
            backgroundPaint.alpha = (originalAlpha * pressedAlpha).toInt()
        }

        // Draw background (solid or gradient)
        if (gradientColors != null) {
            val gradient = if (gradientOrientation == 0) {
                LinearGradient(
                    backgroundRect.left, backgroundRect.top, backgroundRect.right, backgroundRect.top,
                    gradientColors!!, null, Shader.TileMode.CLAMP
                )
            } else {
                LinearGradient(
                    backgroundRect.left, backgroundRect.top, backgroundRect.left, backgroundRect.bottom,
                    gradientColors!!, null, Shader.TileMode.CLAMP
                )
            }
            backgroundPaint.shader = gradient
        } else {
            backgroundPaint.shader = null
            backgroundPaint.color = backColor
        }
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)

        // Restore original alpha
        backgroundPaint.alpha = originalAlpha

        // Draw border
        if (borderWidth > 0) {
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, borderPaint)
        }

        // Draw image background
        imageBackground?.let { drawable ->
            val originalAlpha = drawable.alpha
            if (isPressed) {
                drawable.alpha = (255 * pressedAlpha).toInt()
            }
            drawable.bounds = imageRect.toRect()
            drawable.draw(canvas)
            drawable.alpha = originalAlpha
        }

        // Draw text centered in the view, not relative to background
        text?.let {
            val originalAlpha = textPaint.alpha
            if (isPressed) {
                textPaint.alpha = (originalAlpha * pressedAlpha).toInt()
            }
            val x = width / 2f
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(it, x, y, textPaint)
            textPaint.alpha = originalAlpha
        }
    }

    fun setBackHeightScale(scale: Float) {
        backHeightScale = scale.coerceIn(0f, 1f)
        requestLayout()
        invalidate()
    }

    fun setBackWidthScale(scale: Float) {
        backWidthScale = scale.coerceIn(0f, 1f)
        requestLayout()
        invalidate()
    }

    fun setBackVerticalOffset(offset: Float) {
        backVerticalOffset = offset.coerceIn(0f, 1f)
        requestLayout()
        invalidate()
    }

    fun setBackHorizontalOffset(offset: Float) {
        backHorizontalOffset = offset.coerceIn(0f, 1f)
        requestLayout()
        invalidate()
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
        // Trigger haptic feedback and make sound if accessibility is enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playSoundEffect(android.view.SoundEffectConstants.CLICK)
        }
        onClickListener?.onClick(this)
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
        return super.performClick()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        onClickListener = listener
        super.setOnClickListener(listener)
    }

    // Accessibility support
    override fun getAccessibilityClassName(): CharSequence {
        return "android.widget.Button"
    }

    // Setters for dynamic properties
    fun setBackColor(color: Int) {
        backColor = color
        invalidate()
    }

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }

    fun setBorderColor(color: Int) {
        borderColor = color
        borderPaint.color = color
        invalidate()
    }

    fun setBorderWidth(width: Float) {
        borderWidth = width
        borderPaint.strokeWidth = width
        invalidate()
    }

    fun setImageBackground(drawable: Drawable?) {
        imageBackground = drawable
        invalidate()
    }

    fun setImageScale(scale: Float) {
        imageScale = scale
        invalidate()
    }

    fun setText(newText: String?) {
        text = newText
        invalidate()
    }

    fun setTextColor(color: Int) {
        textColor = color
        textPaint.color = color
        invalidate()
    }

    fun setTextSize(size: Float) {
        textSize = size
        textPaint.textSize = size
        invalidate()
    }

    fun setGradientColors(colors: IntArray?) {
        gradientColors = colors
        invalidate()
    }

    fun setGradientOrientation(orientation: Int) {
        gradientOrientation = orientation
        invalidate()
    }

    fun setShadowProperties(radius: Float, dx: Float, dy: Float, color: Int) {
        shadowRadius = radius
        shadowDx = dx
        shadowDy = dy
        shadowColor = color

        // Update shadow layer
        backgroundPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)

        // Shadow rendering requires software layer type
        if (shadowRadius > 0) {
            setLayerType(LAYER_TYPE_SOFTWARE, backgroundPaint)
        } else {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        }

        invalidate()
    }

    fun setFontFamily(family: String?) {
        fontFamily = family
        updateTypeface()
    }

    fun setTextStyle(style: Int) {
        textStyle = style
        updateTypeface()
    }

    private fun updateTypeface() {
        textTypeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && fontFamily != null) {
            Typeface.create(fontFamily, textStyle)
        } else {
            when (fontFamily) {
                "sans-serif" -> Typeface.create(Typeface.SANS_SERIF, textStyle)
                "serif" -> Typeface.create(Typeface.SERIF, textStyle)
                "monospace" -> Typeface.create(Typeface.MONOSPACE, textStyle)
                null -> Typeface.create(Typeface.DEFAULT, textStyle)
                else -> Typeface.create(fontFamily, textStyle)
            }
        }
        textPaint.typeface = textTypeface
        invalidate()
    }
}