package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import views.MButton

class HomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val btnSettings = findViewById<MButton>(R.id.btnSettings)

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val btnProfile = findViewById<MButton>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}