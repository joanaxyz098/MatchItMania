package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import views.MButton

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        val btnExit = findViewById<MButton>(R.id.btnExit)

        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}