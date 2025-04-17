package Game

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.R
import com.csit284.matchitmania.app.MatchItMania

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

    private var activeFrames = SparseArray<Drawable>()
    private var disableFrames = SparseArray<Drawable>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeRunnable: Runnable? = null
    private var disableRunnable: Runnable? = null
    private var isAnimating = false

    // Preload all frames when value is set
    private fun preloadFrames() {
        activeFrames.clear()
        disableFrames.clear()

        val app = context.applicationContext as MatchItMania

        activeFrames = app.activeFrames[value]
        disableFrames = app.disableFrames[value]

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