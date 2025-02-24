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
import android.widget.Button
import com.csit284.matchitmania.R
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.graphics.toRect


class MButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ): androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr){
    private var backColor: Int = Color.TRANSPARENT
    private var cornerRadius: Float = 20f
    private var borderColor: Int = Color.BLACK
    private var borderWidth: Float = 0f
    private var gradientColors: IntArray? = null
    private var gradientOrientation: Int = 0 // 0 = horizontal, 1 = vertical

    //shadow properties
    private var shadowRadius: Float = 0f
    private var shadowDx: Float = 0f
    private var shadowDy: Float = 0f
    private var shadowColor: Int = Color.DKGRAY

    //image background properties
    private var imageBackground: Drawable? = null
    private var imageScale: Float = 1f

    //offset and scale properties
    private var backWidthScale: Float = 1f
    private var backHeightScale: Float = 1f
    private var backHorizontalOffset: Float = 0f
    private var backVerticalOffset: Float = 0f

    //click state
    private var isPressed = false
    private var pressedAlpha = 0.7f

    //paints
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG) //anti_alias - smooths out rough edges
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    //rect
    private val backgroundRect = RectF() // we use rectF for precise positioning (floats) instead of rect which is just for int
    private val imageRect = RectF()

    //onclicklistener
    private var onClickListener: OnClickListener? = null

    //
    private var gradientColor: LinearGradient? = null

    private var isClipped = false
    init{
        attrs?.let{
            attributeSet ->
            val typedArray = context.obtainStyledAttributes(
                attributeSet,
                R.styleable.MButton
            )
            try{
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
                val gradientColorsId = typedArray.getResourceId(R.styleable.MButton_gradientColors, 0)
                if (gradientColorsId != 0) {
                    gradientColors = resources.getIntArray(gradientColorsId)
                }
                gradientOrientation = typedArray.getInt(R.styleable.MButton_gradientOrientation, 0)
                isClipped = typedArray.getBoolean(R.styleable.MButton_isClipped, isClipped)
            }finally {
                typedArray.recycle()
            }
        }

        // initialize paints
        backgroundPaint.style = Paint.Style.FILL
        borderPaint.style = Paint.Style.STROKE
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.color = backColor
        setLayerType(LAYER_TYPE_SOFTWARE, backgroundPaint)

        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor

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

        shadowPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)

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


        if (gradientColors != null) {
            gradientColor = if (gradientOrientation == 0) {
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
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Apply alpha for pressed state
        val originalAlpha = backgroundPaint.alpha
        if (isPressed) {
            backgroundPaint.alpha = (originalAlpha * pressedAlpha).toInt()
        }

        // Draw shadow first (before clipping)
        if (shadowRadius > 0) {
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, shadowPaint)
        }

        if(isClipped) {
            // Create a clipping path with rounded corners
            val clipPath = Path().apply {
                addRoundRect(backgroundRect, cornerRadius, cornerRadius, Path.Direction.CW)
            }
            // Save the current canvas state
            canvas.save()

            // Clip the canvas to the rounded rect
            canvas.clipPath(clipPath)
        }
        // Apply gradient shader if available
        backgroundPaint.shader = gradientColor
        if (gradientColors == null) {
            backgroundPaint.color = backColor
        }

        // Draw background
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)

        backgroundPaint.alpha = originalAlpha

        // Draw image background (clipped to rounded corners)
        imageBackground?.let { drawable ->
            if (isPressed) {
                drawable.alpha = (255 * pressedAlpha).toInt()
            }
            drawable.bounds = imageRect.toRect()
            drawable.draw(canvas)
            drawable.alpha = originalAlpha
        }

        // Restore the canvas state (remove clipping)
        canvas.restore()

        // Draw border
        if (borderWidth > 0) {
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, borderPaint)
        }

        super.onDraw(canvas)
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
    }
