package com.csit284.matchitmania

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class MessageActivity :Activity() {
    var btnYes: MButton ?= null
    var btnNo: MButton ?= null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_message)

        btnYes = findViewById(R.id.btnYes)
        btnNo = findViewById(R.id.btnNo)
        findViewById<TextView>(R.id.tvMessage).text = intent.getStringExtra("MESSAGE")

        val type = intent.getStringExtra("TYPE") ?: ""

        if(type == "OK"){
            btnYes?.text = "OK"
            btnNo?.isVisible = false
            btnYes?.setOnClickListener {
                val intent = Intent(this, SelectLevelActivity::class.java)
                startActivity(intent)
                finish()
            }
        }else {
            btnNo?.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                finish()
            }

            btnYes?.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                (application as MatchItMania).logOut()

                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}