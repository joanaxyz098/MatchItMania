package com.csit284.matchitmania

import android.app.Activity
import android.os.Bundle
import kotlin.math.ln
import kotlin.math.sqrt

class GameActivity : Activity() {
    var level: Int = 1

    // Constants for configuration
    companion object {
        const val BASE_GRID_SIZE = 2
        const val MAX_GRID_SIZE = 8
        const val BASE_PIECE_TYPES = 5
        const val MAX_PIECE_TYPES = 15
        const val BASE_TIME_SECONDS = 120
        const val TIME_PER_PIECE = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay)

        // Get level from intent or use default
        level = intent.getIntExtra("LEVEL", 1)

        // Calculate game parameters and initialize the game
        val gameParams = calculateGameParameters(level)
        initializeGame(gameParams)
    }

    fun calculateGameParameters(level: Int): GameParameters {
        // Base difficulty determined by level % 10 (0-9)
        val difficultyModifier = level % 10

        // Determine which "decade" of levels we're in (1-10, 11-20, etc.)
        val levelTier = ((level - 1) / 10) + 1

        // Calculate grid dimensions with non-linear scaling
        val rows = calculateGridDimension(BASE_GRID_SIZE, levelTier, MAX_GRID_SIZE)
        val cols = calculateGridDimension(BASE_GRID_SIZE, levelTier, MAX_GRID_SIZE, difficultyModifier)

        // Calculate number of different piece types with gradual scaling
        val pieceTypes = calculatePieceTypes(BASE_PIECE_TYPES, levelTier, MAX_PIECE_TYPES)

        // Calculate time limit (in seconds) with balanced scaling
        val timeLimit = calculateTimeLimit(BASE_TIME_SECONDS, levelTier, rows * cols, TIME_PER_PIECE)

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

data class GameParameters(
    val difficultyModifier: Int,
    val rows: Int,
    val cols: Int,
    val pieceTypes: Int,
    val timeLimit: Int
)


//import android.animation.ValueAnimator;
//import android.os.Bundle;
//import android.view.animation.LinearInterpolator;
//import android.widget.View;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class MainActivity extends AppCompatActivity {
//
//    private View timerBar;
//    private int fullWidth; // The full width of the timer bar
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        timerBar = findViewById(R.id.timerBar);
//
//        // Get the full width of the timer bar
//        timerBar.post(new Runnable() {
//            @Override
//            public void run() {
//                fullWidth = timerBar.getWidth();
//                startTimerAnimation();
//            }
//        });
//    }
//
//    private void startTimerAnimation() {
//        // Animate the width of the timer bar to shrink over time
//        ValueAnimator animator = ValueAnimator.ofInt(fullWidth, 0);
//        animator.setDuration(10000); // Duration in milliseconds (10 seconds for example)
//        animator.setInterpolator(new LinearInterpolator()); // Set to linear so it shrinks evenly
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                // Update the width of the timer bar as it shrinks
//                int value = (int) animation.getAnimatedValue();
//                timerBar.getLayoutParams().width = value;
//                timerBar.requestLayout(); // Apply the change in width
//            }
//        });
//        animator.start();
//    }
//}