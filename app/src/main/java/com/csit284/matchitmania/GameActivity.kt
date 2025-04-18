package com.csit284.matchitmania

import Game.GameParameters
import Game.Piece
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

class GameActivity : AppCompatActivity() {
    var level: Int = 1
    var activePieceView:Piece ?= null
    var matchedPieces: Int = 0
    lateinit var params: GameParameters
    private val auth = FirebaseAuth.getInstance()
    val imgList = mutableListOf<Drawable>()
    private lateinit var tvScore: TextView
    private lateinit var tvCombo: TextView
    private lateinit var ivCombo: ImageView
    private var currentScore = 0
    private var comboCount = 0
    private var lastMatchTime = 0L
    private var gameStartTime = 0L
    private val comboTimeWindow = 2000L  // 2 seconds to maintain combo
    private val handler = Handler(Looper.getMainLooper())
    private var timeBonusRunnable: Runnable? = null
    private lateinit var vTimer: View
    private lateinit var tvTimer: TextView
    private var maxTimerWidth = 0
    private val timerUpdateInterval = 100L // Update every 100ms for smoother animation
    companion object {
        const val BASE_GRID_SIZE = 2
        const val MAX_GRID_SIZE = 8
        const val BASE_PIECE_TYPES = 5
        const val MAX_PIECE_TYPES = 15
        const val BASE_TIME_SECONDS = 120
        const val TIME_PER_PIECE = 5
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay)
        tvScore = findViewById(R.id.tvScore)
        tvCombo = findViewById(R.id.tvCombo)  // Add this TextView to your layout
        ivCombo = findViewById(R.id.ivCombo)  // Add this TextView to your layout
        tvTimer = findViewById(R.id.tvTimer) // Make sure this exists in your layout
        vTimer = findViewById(R.id.vTimer) // Make sure this exists in your layout
        vTimer.post {
            maxTimerWidth = tvTimer.width // Store the maximum width
            updateTimerWidth(params.timeLimit * 1000L) // Initialize with full width
        }

        updateScore(0)
        updateComboDisplay()

        val btnExit = findViewById<ImageView>(R.id.btnExit)
        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val btnSettings = findViewById<ImageView>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val tvLevelName = findViewById<TextView>(R.id.tvLevelName)
        level = intent.getIntExtra("LEVEL", 0)
        tvLevelName.text = "Level $level"

        params = calculateGameParameters(level)
        initializeGame(params)

        gameStartTime = System.currentTimeMillis()
        startTimeBonusCounter()
    }

    fun calculateGameParameters(level: Int): GameParameters {
        // Base difficulty determined by level % 10 (0-9)
        val difficultyModifier = level % 10

        // Determine which "decade" of levels we're in (1-10, 11-20, etc.)
        val levelTier = ((level - 1) / 10) + 1

        // Calculate grid dimensions ensuring they can accommodate pairs
        var rows = calculateGridDimension(BASE_GRID_SIZE, levelTier, MAX_GRID_SIZE)
        var cols = calculateGridDimension(BASE_GRID_SIZE, levelTier, MAX_GRID_SIZE, difficultyModifier)

        // Ensure we have an even number of cells (for pairs)
        if ((rows * cols) % 2 != 0) {
            // Adjust columns to ensure an even total
            cols += 1
        }

        // Ensure more balanced dimensions (closer to square when possible)
        if (cols > rows * 2) {
            val temp = cols
            cols = rows
            rows = temp / 2
        } else if (rows > cols * 2) {
            val temp = rows
            rows = cols
            cols = temp / 2
        }

        // Calculate number of different piece types (pairs)
        val maxPairs = (rows * cols) / 2  // Maximum possible pairs given grid size
        val pieceTypes = minOf(
            calculatePieceTypes(BASE_PIECE_TYPES, levelTier, MAX_PIECE_TYPES),
            maxPairs  // Cannot exceed available pairs
        )

        // Calculate time limit with balanced scaling
        val timeLimit = calculateTimeLimit(
            BASE_TIME_SECONDS, levelTier, rows * cols,
            TIME_PER_PIECE
        )

        return GameParameters(
            difficultyModifier = difficultyModifier,
            rows = rows,
            cols = cols,
            pieceTypes = pieceTypes,
            timeLimit = timeLimit
        )
    }

    // Helper function to calculate grid dimensions with non-linear scaling
    private fun calculateGridDimension(
        baseSize: Int,
        levelTier: Int,
        maxSize: Int,
        difficultyModifier: Int = 0
    ): Int {
        // Use a logarithmic formula for more dynamic scaling
        val dimension = baseSize + (ln(levelTier.toDouble() + 1) * 2) + (difficultyModifier * 0.1)
        return minOf(dimension.toInt(), maxSize)
    }

    // Helper function to calculate piece types with gradual scaling
    private fun calculatePieceTypes(
        basePieceTypes: Int,
        levelTier: Int,
        maxPieceTypes: Int
    ): Int {
        // Use a square root formula for slower scaling
        return minOf(basePieceTypes + sqrt(levelTier.toDouble()).toInt(), maxPieceTypes)
    }

    // Helper function to calculate time limit with balanced scaling
    private fun calculateTimeLimit(
        baseTime: Int,
        levelTier: Int,
        gridCapacity: Int, // Total number of cells in the grid
        timePerPiece: Int
    ): Int {
        // Adjust time limit based on level tier and grid capacity
        return baseTime - (levelTier * 5) + (gridCapacity * timePerPiece / 2)
    }

    private fun initializeGame(params: GameParameters) {
        val glGame = findViewById<GridLayout>(R.id.glGame)
        glGame.columnCount = 4  // Keeping your original column count
        glGame.rowCount = params.rows

        // Create a list of paired numbers (0-7 only)
        val pairsList = mutableListOf<Int>()
        for (i in 0 until params.pieceTypes) {
            // Add each number twice (for matching pairs), modulo 8 to limit to 0-7
            pairsList.add(i % 8)
            pairsList.add(i % 8)
        }

        // Fill remaining slots with pairs from 0-7
        val remainingPairs = (params.rows * params.cols - pairsList.size) / 2
        for (i in 0 until remainingPairs) {
            val extraType = i % 8  // Limit to 0-7
            pairsList.add(extraType)
            pairsList.add(extraType)
        }

        // Shuffle the list (your original randomization)
        pairsList.shuffle()

        // Load only images for types 0-7
        imgList.clear()
        for (i in 0..7) {  // Only load 8 images (0-7)
            val resId = resources.getIdentifier("blocks_$i", "drawable", packageName)
            if (resId != 0) {
                ContextCompat.getDrawable(this, resId)?.let { imgList.add(it) }
            } else {
                Log.w("ImageLoader", "Drawable not found for blocks_$i")
            }
        }

        // Your original grid placement code remains unchanged
        for (index in 0 until pairsList.size) {
            val pieceType = pairsList[index]
            val pieceView = Piece(this).apply {
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                background = imgList[pieceType % 8]  // Ensure we only use 0-7
                value = pieceType % 8  // Ensure value is 0-7
                setOnClickListener {
                    handleCLick(this)
                }
            }

            val gParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
            pieceView.layoutParams = gParams
            pieceView.post {
                val width = pieceView.width
                val height = pieceView.height
                if (width != height) {
                    pieceView.layoutParams.height = width
                    pieceView.requestLayout()
                }
            }
            glGame.addView(pieceView)
        }
    }
    private fun updateScore(pointsToAdd: Int) {
        currentScore += pointsToAdd
        tvScore.text = "Score: $currentScore"
    }

    @SuppressLint("SetTextI18n")
    private fun updateComboDisplay() {
        if (comboCount > 1) {
            tvCombo.text = "$comboCount"
            tvCombo.visibility = View.VISIBLE

        } else {
            tvCombo.visibility = View.GONE
        }
    }

    private fun handleCLick(currentPieceView: Piece) {
        if (activePieceView != currentPieceView) {
            if (activePieceView?.value == currentPieceView.value) {
                // Successful match - handle combo
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastMatchTime < comboTimeWindow) {
                    comboCount++
                } else {
                    comboCount = 1
                }
                lastMatchTime = currentTime

                // Calculate score with combo multiplier
                val basePoints = 100 * level
                val comboMultiplier = 1 + (comboCount * 0.2f)  // 20% bonus per combo
                val totalPoints = (basePoints * comboMultiplier).toInt()

                updateScore(totalPoints)
                updateComboDisplay()

                currentPieceView.isEnabled = false
                activePieceView?.isEnabled = false
                activePieceView = null
                matchedPieces++

            } else {
                if (activePieceView != null) {
                    // Failed match - reset combo
                    comboCount = 0
                    activePieceView?.deactivate()
                    if(currentScore > 0)updateScore(-20)
                }

                updateComboDisplay()
                activePieceView = currentPieceView
            }
        }

        if ((params.rows * params.cols) / 2 == matchedPieces) {
            // Calculate time bonus
            val elapsedTime = (System.currentTimeMillis() - gameStartTime) / 1000
            val timeLeft = max(0, params.timeLimit - elapsedTime)
            val timeBonus = (timeLeft * 10 * level)  // 10 points per second left × level

            updateScore(timeBonus.toInt())
            updateScore(500 * level)  // Base level completion bonus

            handler.removeCallbacks(timeBonusRunnable!!)
            handleWin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun handleWin() {
        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                    val app = (application as MatchItMania)
                    if(level == app.userProfile.level) app.userProfile.level += 1
                    if(currentScore > app.userProfile.highestScore) app.userProfile.highestScore = currentScore
                    if(comboCount > app.userProfile.maxCombo) app.userProfile.maxCombo = comboCount
                    val elapsedMillis = System.currentTimeMillis() - gameStartTime
                    if(elapsedMillis < app.userProfile.fastestClear) app.userProfile.fastestClear = elapsedMillis

                    app.saveUserData(this@GameActivity, app.userProfile)

                    Log.i("TASK", "Settings successfully saved!")
                    val intent = Intent(this@GameActivity, MessageActivity::class.java)
                    intent.putExtra("MESSAGE", "You won!\n Total score: $currentScore")
                    intent.putExtra("TYPE", "OK")
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("TASK", "Failed to save settings: ${e.message}", e)
                    Toast.makeText(this@GameActivity, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: Log.e("TASK", "User is not logged in. Cannot save settings.")
    }
    private fun handleLoss() {
        val intent = Intent(this@GameActivity, MessageActivity::class.java)
        val app = (application as MatchItMania)
        app.userProfile.losses += 1
        app.saveUserData(this@GameActivity, app.userProfile)
        intent.putExtra("MESSAGE", "You lost! Try again.")
        intent.putExtra("TYPE", "OK")
        startActivity(intent)
    }
    private fun startTimeBonusCounter() {
        timeBonusRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - gameStartTime
                val remainingMillis = max(0, (params.timeLimit * 1000L) - elapsedMillis)

                if (remainingMillis == 0L) {
                    // Time is up — handle loss
                    handleLoss()
                    return // Exit the runnable, stop posting more updates
                }

                // Update timer bar width or progress
                updateTimerWidth(remainingMillis)

                // Calculate minutes and seconds
                val totalSeconds = (remainingMillis / 1000).toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60

                // Format time as mm:ss
                tvTimer.text = String.format("%d:%02d", minutes, seconds)

                // Change text color if time is running out
                if (totalSeconds <= 10) {
                    tvTimer.setTextColor(Color.RED)
                } else {
                    tvTimer.setTextColor(ContextCompat.getColor(this@GameActivity, R.color.white))
                }

                // Schedule next update
                handler.postDelayed(this, timerUpdateInterval)
            }
        }

        handler.post(timeBonusRunnable!!)
    }


    private fun updateTimerWidth(remainingMillis: Long) {
        if (::vTimer.isInitialized && maxTimerWidth > 0) {
            val progress = remainingMillis.toFloat() / (params.timeLimit * 1000L)
            val newWidth = (maxTimerWidth * progress).toInt()
            vTimer.layoutParams.width = max(1, newWidth) // Ensure it never goes to 0
            vTimer.requestLayout()
        }
    }
}