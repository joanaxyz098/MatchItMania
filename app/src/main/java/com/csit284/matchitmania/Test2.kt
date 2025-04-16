package com.csit284.matchitmania

import Game.GameParameters
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import views.MButton
import kotlin.math.ln
import kotlin.math.sqrt

class Test2 : AppCompatActivity() {
    var level: Int = 1

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

        calculateGameParameters(level)
    }

    fun calculateGameParameters(level: Int): GameParameters {
        // Base difficulty determined by level % 10 (0-9)
        val difficultyModifier = level % 10

        // Determine which "decade" of levels we're in (1-10, 11-20, etc.)
        val levelTier = ((level - 1) / 10) + 1

        // Calculate grid dimensions with non-linear scaling
        val rows = calculateGridDimension(
            Test2.BASE_GRID_SIZE, levelTier,
            Test2.MAX_GRID_SIZE
        )
        val cols = calculateGridDimension(
            Test2.BASE_GRID_SIZE, levelTier,
            Test2.MAX_GRID_SIZE, difficultyModifier)

        // Calculate number of different piece types with gradual scaling
        val pieceTypes = calculatePieceTypes(
            Test2.BASE_PIECE_TYPES, levelTier,
            Test2.MAX_PIECE_TYPES
        )

        // Calculate time limit (in seconds) with balanced scaling
        val timeLimit = calculateTimeLimit(
            Test2.BASE_TIME_SECONDS, levelTier, rows * cols,
            Test2.TIME_PER_PIECE
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
        // Create a 2D ArrayList to represent the grid
        val grid = ArrayList<ArrayList<Int>>()

        // Populate the grid with pieces
        for (row in 0 until params.rows) {
            val rowList = ArrayList<Int>()
            for (col in 0 until params.cols) {
                // Assign a random piece type (0 until pieceTypes)
                val pieceType = (0 until params.pieceTypes).random()
                rowList.add(pieceType)
            }
            grid.add(rowList)
        }

        // TODO: Integrate the grid with your layout
        // For example, bind the grid to your UI elements (e.g., RecyclerView, GridView, or custom views)
    }
}

