package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import views.MButton

class Test : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay)

        val btnExit = findViewById<MButton>(R.id.btnSettings)
        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

}