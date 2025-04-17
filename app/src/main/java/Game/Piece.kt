package Game

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.R

class Piece @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    var value: Int = -1
        set(newValue) {
            field = newValue
            preloadFrames()
        }

    private val activeFrames = SparseArray<android.graphics.drawable.Drawable>()
    private val disableFrames = SparseArray<android.graphics.drawable.Drawable>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeRunnable: Runnable? = null
    private var disableRunnable: Runnable? = null
    private var isAnimating = false

    // Preload all frames when value is set
    private fun preloadFrames() {
        activeFrames.clear()
        disableFrames.clear()

        // Preload active animation frames (0-7)
        for (i in 0..7) {
            val resName = "blocks_${value}_$i"
            val resId = resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                activeFrames.put(i, ContextCompat.getDrawable(context, resId))
            }
        }

        // Preload disable animation frames (0-8)
        for (i in 0..8) {
            val resName = "blocksk_${value}_$i"
            val resId = resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                disableFrames.put(i, ContextCompat.getDrawable(context, resId))
            }
        }
    }

    override fun performClick(): Boolean {
        if (!isAnimating) {
            isAnimating = true
            startActiveAnimation()
            return super.performClick()
        }
        return false
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            stopActiveAnimation()
            startDisableAnimation()
        }
    }

    fun deactivate() {
        stopActiveAnimation()
        stopDisableAnimation()
        isAnimating = false

        // Use preloaded default frame if available
        val defaultFrame = activeFrames[0] ?: run {
            val resId = resources.getIdentifier("blocks_$value", "drawable", context.packageName)
            if (resId != 0) ContextCompat.getDrawable(context, resId) else null
        }
        background = defaultFrame
    }

    private fun startActiveAnimation() {
        stopActiveAnimation()

        val initialFrames = listOf(0, 1, 2, 3)
        val loopFrames = listOf(4, 5, 6, 7)
        var currentFrame = 0
        var isInitialSequence = true

        activeRunnable = object : Runnable {
            override fun run() {
                val frame = if (isInitialSequence) {
                    initialFrames[currentFrame]
                } else {
                    loopFrames[currentFrame % loopFrames.size]
                }

                background = activeFrames[frame]
                currentFrame++

                if (isInitialSequence && currentFrame >= initialFrames.size) {
                    isInitialSequence = false
                    currentFrame = 0
                }

                mainHandler.postDelayed(this, 100L)
            }
        }

        mainHandler.post(activeRunnable!!)
    }

    private fun startDisableAnimation() {
        stopDisableAnimation()
        isAnimating = false

        var currentFrame = 0
        val totalFrames = 9

        disableRunnable = object : Runnable {
            override fun run() {
                background = disableFrames[currentFrame]
                currentFrame++

                if (currentFrame < totalFrames) {
                    mainHandler.postDelayed(this, 100L)
                } else {
                    isAnimating = false
                }
            }
        }

        mainHandler.post(disableRunnable!!)
    }

    private fun stopActiveAnimation() {
        activeRunnable?.let {
            mainHandler.removeCallbacks(it)
            activeRunnable = null
        }
    }

    private fun stopDisableAnimation() {
        disableRunnable?.let {
            mainHandler.removeCallbacks(it)
            disableRunnable = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopActiveAnimation()
        stopDisableAnimation()
        mainHandler.removeCallbacksAndMessages(null)
    }
}