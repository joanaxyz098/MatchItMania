// GameActivity.kt
package com.csit284.matchitmania

import Game.GameParameters
import Game.Piece
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
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
import music.BackgroundMusic
import music.GameBGMusic
import music.SoundEffects
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

class GameActivity : AppCompatActivity() {
    private var level: Int = 1
    private var activePieceView: Piece? = null
    private var matchedPieces: Int = 0
    private lateinit var params: GameParameters
    private val auth = FirebaseAuth.getInstance()
    private val imgList = mutableListOf<Drawable>()
    private var gameEnded = false
    private var isPausedForDialog = false

    private lateinit var tvScore: TextView
    private lateinit var tvCombo: TextView
    private lateinit var ivCombo: ImageView
    private lateinit var tvTimer: TextView
    private lateinit var vTimer: View

    private var currentScore = 0
    private var comboCount = 0
    private var lastMatchTime = 0L
    private var gameStartTime = 0L
    private var maxTimerWidth = 0

    private val handler = Handler(Looper.getMainLooper())
    private var timeBonusRunnable: Runnable? = null
    private var musicOff = false

    companion object {
        const val BASE_GRID_SIZE = 2
        const val MAX_GRID_SIZE = 6
        const val BASE_PIECE_TYPES = 5
        const val MAX_PIECE_TYPES = 7
        const val BASE_TIME_SECONDS = 3
        const val TIME_PER_PIECE = 3
        const val COMBO_TIME_WINDOW = 2000L
        const val TIMER_UPDATE_INTERVAL = 100L
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay)
        GameBGMusic.initialize(this)

        val musicEnabled = (application as MatchItMania).userSettings.music ?: true
        if (musicEnabled) {
            GameBGMusic.play()
        }

        SoundEffects.init(this)

        tvScore = findViewById(R.id.tvScore)
        tvCombo = findViewById(R.id.tvCombo)
        ivCombo = findViewById(R.id.ivCombo)
        tvTimer = findViewById(R.id.tvTimer)
        vTimer = findViewById(R.id.vTimer)

        level = intent.getIntExtra("LEVEL", 0)
        findViewById<TextView>(R.id.tvLevelName).text = "Level $level"

        params = calculateGameParameters(level)
        initializeGame(params)

        vTimer.post {
            maxTimerWidth = vTimer.width
            updateTimerWidth(params.timeLimit * 1000L)
        }

        findViewById<ImageView>(R.id.btnExit).setOnClickListener {
            if (!gameEnded) {
                isPausedForDialog = true
                timeBonusRunnable?.let { handler.removeCallbacks(it) }
                if (GameBGMusic.mediaPlayer?.isPlaying == false){
                    musicOff = true
                }
                GameBGMusic.pause()

                val intent = Intent(this, MessageActivity::class.java)
                intent.putExtra("MESSAGE", "Do you want to end the game?")
                intent.putExtra("TYPE", "CONFIRM_EXIT")
                startActivityForResult(intent, 1)
            }
        }
        updateScore(0)
        updateComboDisplay()

        gameStartTime = System.currentTimeMillis()
        startTimeBonusCounter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val userResponse = data?.getStringExtra("RESPONSE")
            if (userResponse == "YES") {
                handleLoss()
            } else if (userResponse == "NO") {
                if (isPausedForDialog) {
                    isPausedForDialog = false
                    if (musicOff) {
                        GameBGMusic.pause()
                    }  else {
                        GameBGMusic.play()
                    }
                    startTimeBonusCounter()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BackgroundMusic.pause()
    }

    fun calculateGameParameters(level: Int): GameParameters {
        val difficultyModifier = if (level == 1) {
            0 // or a special value, like -1, to mark it as "unique"
        } else {
            level % 10
        }

        val levelTier = if (level == 1) {
            0 // or 1, depending on whether tiering starts at 0 or 1
        } else {
            ((level - 1) / 10) + 1
        }

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

    private fun calculateGridDimension(baseSize: Int, levelTier: Int, maxSize: Int, difficultyModifier: Int = 0): Int {
        // Use a logarithmic formula for more dynamic scaling
        val dimension = baseSize + (ln(levelTier.toDouble() + 1) * 2) + (difficultyModifier * 0.1)
        return minOf(dimension.toInt(), maxSize)
    }

    private fun calculatePieceTypes(basePieceTypes: Int, levelTier: Int, maxPieceTypes: Int): Int {
        return minOf(basePieceTypes + sqrt(levelTier.toDouble()).toInt(), maxPieceTypes)
    }

    private fun calculateTimeLimit(baseTime: Int, levelTier: Int, gridCapacity: Int, timePerPiece: Int): Int {
        return baseTime - levelTier + (gridCapacity * timePerPiece / 2)
    }

    private fun initializeGame(params: GameParameters) {
        val glGame = findViewById<GridLayout>(R.id.glGame)
        glGame.columnCount = 4
        glGame.rowCount = params.rows

        val pairsList = mutableListOf<Int>()
        repeat(params.pieceTypes) {
            pairsList.add(it % 7)
            pairsList.add(it % 7)
        }

        val remainingPairs = (params.rows * params.cols - pairsList.size) / 2
        repeat(remainingPairs) {
            val extraType = it % 7
            pairsList.add(extraType)
            pairsList.add(extraType)
        }

        pairsList.shuffle()

        imgList.clear()
        for (i in 0..6) {
            val resId = resources.getIdentifier("blocks_$i", "drawable", packageName)
            if (resId != 0) {
                ContextCompat.getDrawable(this, resId)?.let { imgList.add(it) }
            }
        }

        for (pieceType in pairsList) {
            val pieceView = Piece(this).apply {
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                val safeIndex = pieceType % imgList.size
                background = imgList[safeIndex]
                value = pieceType
                setOnClickListener { handleClick(this) }
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
                pieceView.layoutParams.height = width
                pieceView.requestLayout()
            }
            glGame.addView(pieceView)
        }
    }

    private fun updateScore(pointsToAdd: Int) {
        currentScore += pointsToAdd
        if (currentScore < 0) currentScore = 0
        tvScore.text = "Score: $currentScore"
    }

    private fun updateComboDisplay() {
        if (comboCount > 1) {
            tvCombo.text = "x$comboCount Combo!"
            tvCombo.visibility = View.VISIBLE
        } else {
            tvCombo.visibility = View.GONE
        }
    }

    private fun handleClick(currentPieceView: Piece) {
        if (activePieceView != currentPieceView) {
            if (activePieceView?.value == currentPieceView.value) {
                SoundEffects.playMatch()
                val currentTime = System.currentTimeMillis()
                comboCount = if (currentTime - lastMatchTime < COMBO_TIME_WINDOW) comboCount + 1 else 1
                lastMatchTime = currentTime

                val basePoints = 10 * level
                val comboMultiplier = 1f + (comboCount * 0.15f)
                val totalPoints = (basePoints * comboMultiplier).toInt()

                updateScore(totalPoints)
                updateComboDisplay()

                currentPieceView.isEnabled = false
                activePieceView?.isEnabled = false
                activePieceView = null
                matchedPieces++
            } else {
                SoundEffects.playTap()
                if (activePieceView != null) {
                    comboCount = 0
                    activePieceView?.deactivate()
                    updateScore(-5 * level) // reduced penalty
                }
                updateComboDisplay()
                activePieceView = currentPieceView
            }
        }

        if ((params.rows * params.cols) / 2 == matchedPieces) {
            val elapsedTime = (System.currentTimeMillis() - gameStartTime) / 1000
            val timeLeft = max(0, params.timeLimit - elapsedTime)
            val timeBonus = (timeLeft * 3 * level)  // reduced time bonus scaling

            updateScore(timeBonus.toInt())
            updateScore(300 * level) // win bonus scaled down slightly

            timeBonusRunnable?.let { handler.removeCallbacks(it) }
            handleWin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeBonusRunnable?.let { handler.removeCallbacks(it) }
        GameBGMusic.stop()
    }

    private fun handleWin() {
        gameEnded = true
        GameBGMusic.stop()
        SoundEffects.playWin()
        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                    val app = (application as MatchItMania)
                    if (level == app.userProfile.level) app.userProfile.level++
                    if (currentScore > app.userProfile.highestScore) app.userProfile.highestScore = currentScore
                    if (comboCount > app.userProfile.maxCombo) app.userProfile.maxCombo = comboCount
                    val elapsedMillis = System.currentTimeMillis() - gameStartTime
                    if (elapsedMillis < app.userProfile.fastestClear) app.userProfile.fastestClear = elapsedMillis

                    app.saveUserData(this@GameActivity, app.userProfile)

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
        gameEnded = true
        GameBGMusic.stop()
        SoundEffects.playLose()
        val app = (application as MatchItMania)
        app.userProfile.losses++
        app.saveUserData(this, app.userProfile)

        val intent = Intent(this@GameActivity, MessageActivity::class.java)
        intent.putExtra("MESSAGE", "You lost! Try again.")
        intent.putExtra("TYPE", "OK")
        startActivity(intent)
    }

    private fun startTimeBonusCounter() {
        timeBonusRunnable?.let { handler.removeCallbacks(it) }
        timeBonusRunnable = object : Runnable {
            override fun run() {
                if (gameEnded) return
                val elapsedMillis = System.currentTimeMillis() - gameStartTime
                val remainingMillis = max(0, (params.timeLimit * 1000L) - elapsedMillis)

                if (remainingMillis == 0L) {
                    handleLoss()
                    return
                }

                updateTimerWidth(remainingMillis)

                val totalSeconds = (remainingMillis / 1000).toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                tvTimer.text = String.format("%d:%02d", minutes, seconds)

                tvTimer.setTextColor(
                    if (totalSeconds <= 10) Color.RED
                    else ContextCompat.getColor(this@GameActivity, R.color.white)
                )

                handler.postDelayed(this, TIMER_UPDATE_INTERVAL)
            }
        }

        handler.post(timeBonusRunnable!!)
    }

    private fun updateTimerWidth(remainingMillis: Long) {
        if (::vTimer.isInitialized && maxTimerWidth > 0) {
            val progress = remainingMillis.toFloat() / (params.timeLimit * 1000L)
            val newWidth = (progress * maxTimerWidth).toInt()
            vTimer.layoutParams.width = newWidth
            vTimer.requestLayout()
        }
    }

    override fun onBackPressed() {
        if (!gameEnded) {
            isPausedForDialog = true
            timeBonusRunnable?.let { handler.removeCallbacks(it) }

            if (GameBGMusic.mediaPlayer?.isPlaying == false) {
                musicOff = true
            }
            GameBGMusic.pause()

            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("MESSAGE", "Do you want to end the game?")
            intent.putExtra("TYPE", "CONFIRM_EXIT")
            startActivityForResult(intent, 1)
        } else {
            super.onBackPressed()  // or finish() if you want to close the activity
        }
    }

}
