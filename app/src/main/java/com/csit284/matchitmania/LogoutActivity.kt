package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class LogoutActivity :Activity() {

    var btnYes: MButton ?= null
    var btnNo: MButton ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_logout)

        btnYes = findViewById(R.id.btnYes)
        btnNo = findViewById(R.id.btnNo)

        btnNo?.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnYes?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val app = application as MatchItMania
            app.userProfile = UserProfile()
            app.userSettings = UserSettings()

            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}