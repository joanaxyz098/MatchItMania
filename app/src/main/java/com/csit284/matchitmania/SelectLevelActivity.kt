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
        val numberOfLevels = 20

        for (i in 1..numberOfLevels) {
            val button = views.MButton(this).apply {
                text = i.toString()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                this.cornerRadius = 20f
                this.backColor = (ContextCompat.getColor(context, R.color.MDarkPurple))

                typeface = ResourcesCompat.getFont(context, R.font.poppins)
            }

            val size = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 70f, resources.displayMetrics
            ).toInt()
            val margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
            ).toInt()

            val params = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(margin, margin, margin, margin)
            }

            button.layoutParams = params

            button.setOnClickListener {
                Toast.makeText(this, "Clicked Level $i", Toast.LENGTH_SHORT).show()
            }

            gridLayout.addView(button)
        }
    }
}