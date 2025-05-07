package com.csit284.matchitmania

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.csit284.matchitmania.app.MatchItMania
import music.BackgroundMusic

class TutorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        val btnExit = findViewById<Button>(R.id.btnExit)
        btnExit.setOnClickListener {
            finish() // Closes the activity
        }
    }

    override fun onResume() {
        super.onResume()
        val musicEnabled = (application as MatchItMania).userSettings.music ?: true
        if (musicEnabled) {
            BackgroundMusic.play()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}