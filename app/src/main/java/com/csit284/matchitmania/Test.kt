package com.csit284.matchitmania

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import views.MButton

class Test : AppCompatActivity() {
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
        val level = intent.getIntExtra("LEVEL", 0)
        tvLevelName.text = "Level $level"
    }

}