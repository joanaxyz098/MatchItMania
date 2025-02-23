package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import views.MButton

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val btnExit = findViewById<MButton>(R.id.btnExit)
        btnExit.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        tvRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}