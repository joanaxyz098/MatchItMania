package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.csit284.matchitmania.app.MatchItMania
import music.BackgroundMusic
import views.MButton

class SelectLevelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_level)
        val btnExit = findViewById<MButton>(R.id.btnExit)

        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        setUpLevels()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val musicEnabled = (application as MatchItMania).userSettings.music ?: true
        if (musicEnabled) {
            BackgroundMusic.play()
        }
    }

    private fun setUpLevels() {
        val gridLayout = findViewById<GridLayout>(R.id.glLevels)
        val userLevel = (application as MatchItMania).userProfile.level
        val numberOfLevels = 51
        Log.i("TASK", "username ${ (application as MatchItMania).userProfile.username} level $numberOfLevels")
        val numColumns = 3  // Define the number of columns for the grid
        gridLayout.columnCount = numColumns

        // This ensures that the row count is based on the number of levels and number of columns.
        val numRows = (numberOfLevels + numColumns - 1) / numColumns
        gridLayout.rowCount = numRows

        for (i in 1..numberOfLevels) {
            val button = MButton(this).apply {
                gravity = Gravity.CENTER
                this.cornerRadius = 20f
                if(i <= userLevel){
                    this.background = ContextCompat.getDrawable(context, R.drawable.level_back)
                    text = i.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    setTextColor(Color.WHITE)
                }else {
                    this.background = ContextCompat.getDrawable(context, R.drawable.level_lock)
                    this.isEnabled = false
                }
                typeface = ResourcesCompat.getFont(context, R.font.poppins)
            }

            val size = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 70f, resources.displayMetrics
            ).toInt()

            // Set up LayoutParams with row and column weight
            val params = GridLayout.LayoutParams().apply {
                width = 0 // width 0 to allow weight distribution
                height = 0 // height will be set proportionally to width
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Distribute columns evenly
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Distribute rows evenly
                setMargins(8, 8, 8, 8) // Optional: add margin around buttons
            }

            // Set the button layout params
            button.layoutParams = params

            // Ensure that the button becomes square by controlling width and height
            button.post {
                val width = button.width
                val height = button.height
                if (width != height) {
                    // Adjust height based on width to keep buttons square
                    button.layoutParams.height = width
                    button.requestLayout()
                }
            }

            button.setOnClickListener {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("LEVEL", i)
                startActivity(intent)
            }

            gridLayout.addView(button)
        }
    }
}