package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class SelectLevelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_level)
        setUpLevels()
        val btnExit = findViewById<MButton>(R.id.btnExit)

        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }


    private fun setUpLevels() {
        val gridLayout = findViewById<GridLayout>(R.id.glLevels)
        val numberOfLevels = 100
        val numColumns = 3  // Define the number of columns for the grid
        gridLayout.columnCount = numColumns

        // This ensures that the row count is based on the number of levels and number of columns.
        val numRows = (numberOfLevels + numColumns - 1) / numColumns
        gridLayout.rowCount = numRows

        for (i in 1..numberOfLevels) {
            val button = views.MButton(this).apply {
                text = i.toString()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                this.cornerRadius = 20f
                this.backColor = ContextCompat.getColor(context, R.color.MPurple)
                this.borderColor = ContextCompat.getColor(context, R.color.MShader)
                this.borderWidth = 10f
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
                val intent = Intent(this, Test2::class.java)
                intent.putExtra("LEVEL", i)
                startActivity(intent)
            }

            gridLayout.addView(button)
        }
    }
}