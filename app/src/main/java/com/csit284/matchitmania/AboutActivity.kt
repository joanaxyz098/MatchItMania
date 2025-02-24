package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import views.MButton

class AboutActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val btnExit = findViewById<MButton>(R.id.btnExit)
        btnExit.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}