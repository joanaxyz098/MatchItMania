package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import userGenerated.UserProfile
import userGenerated.UserSettings
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

        val btnLevel1 = findViewById<MButton>(R.id.level1)

        btnLevel1.setOnClickListener {
            val intent = Intent(this, Test::class.java)
            startActivity(intent)
        }
    }
}