package com.csit284.matchitmania

import Game.GameParameters
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import userGenerated.UserSettings
import views.MButton
import kotlin.math.ln
import kotlin.math.sqrt

class Test2 : AppCompatActivity() {
    var level: Int = 1
    var activePiece: Int = -1
    var activePieceView: MButton ?= null
    var matchedPieces: Int = 0
    lateinit var params: GameParameters
    private val auth = FirebaseAuth.getInstance()
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

        val btnExit = findViewById<MButton>(R.id.btnSettings)
        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val tvLevelName = findViewById<TextView>(R.id.tvLevelName)
        level = intent.getIntExtra("LEVEL", 0)
        tvLevelName.text = "Level $level"

        params = calculateGameParameters(level)
        initializeGame(params)
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
        glGame.columnCount = 4
        glGame.rowCount = params.rows

        // Create a list of paired numbers
        val pairsList = mutableListOf<Int>()
        for (i in 0 until params.pieceTypes) {
            // Add each number twice (for matching pairs)
            pairsList.add(i)
            pairsList.add(i)
        }

        // If we need more pairs to fill the grid (in case pieceTypes < grid capacity/2)
        val remainingPairs = (params.rows * params.cols - pairsList.size) / 2
        for (i in 0 until remainingPairs) {
            // Add additional pairs using modulo to reuse existing types
            val extraType = i % params.pieceTypes
            pairsList.add(extraType)
            pairsList.add(extraType)
        }

        // Shuffle the list to randomize the positions
        pairsList.shuffle()

        for (row in 0 until params.rows) {
            for (col in 0 until params.cols) {
                val index = row * params.cols + col
                if (index < pairsList.size) {
                    val pieceType = pairsList[index]

                    // Use a Button or ImageView or custom view here
                    val pieceView = MButton(this).apply {
                        text = pieceType.toString() // Replace with icon if needed
                        textSize = 18f
                        gravity = Gravity.CENTER
                        setTextColor(Color.WHITE)
                        cornerRadius = 16f
                        background = ContextCompat.getDrawable(this@Test2, R.drawable.bg_card)

                        setOnClickListener {
                            handleCLick(this, pieceType)
                        }
                    }

                    val gParams = GridLayout.LayoutParams().apply {
                        width = 0 // width 0 to allow weight distribution
                        height = 0 // height will be set proportionally to width
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Distribute columns evenly
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Distribute rows evenly
                        setMargins(8, 8, 8, 8) // Optional: add margin around buttons
                    }

                    pieceView.layoutParams = gParams

                    pieceView.post {
                        val width = pieceView.width
                        val height = pieceView.height
                        if (width != height) {
                            // Adjust height based on width to keep buttons square
                            pieceView.layoutParams.height = width
                            pieceView.requestLayout()
                        }
                    }

                    glGame.addView(pieceView)
                }
            }
        }
    }
    private fun handleCLick(currentPieceView: MButton, currentPiece: Int){
        currentPieceView.background = ContextCompat.getDrawable(this, R.drawable.bg_active)
        if(activePieceView != currentPieceView && activePiece == currentPiece){
            currentPieceView.background = null
            currentPieceView.backColor = ContextCompat.getColor(this, R.color.transparent)
            currentPieceView.text = null
            activePieceView?.background = null
            activePieceView?.backColor = ContextCompat.getColor(this, R.color.transparent)
            activePieceView?.text = null
            activePiece = -1
            activePieceView = null
            matchedPieces++
        }else{
            if (activePieceView != currentPieceView && activePieceView != null) activePieceView?.background = ContextCompat.getDrawable(this, R.drawable.bg_card)
            activePiece = currentPiece
            activePieceView = currentPieceView
        }

        if((params.rows * params.cols) / 2 == matchedPieces){
            updateLevel()
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("MESSAGE", "You won! Congratulations.")
            intent.putExtra("TYPE", "OK")
            startActivity(intent)
        }
    }

    private fun updateLevel() {
        (application as MatchItMania).userProfile.level += 1
        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set((application as MatchItMania).userProfile.toMap())
                        .await()

                    Log.i("TASK", "Settings successfully saved!")
                } catch (e: Exception) {
                    Log.e("TASK", "Failed to save settings: ${e.message}", e)
                    Toast.makeText(this@Test2, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: Log.e("TASK", "User is not logged in. Cannot save settings.")
    }
}