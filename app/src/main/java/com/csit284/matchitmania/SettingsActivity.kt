package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import views.MButton

class SettingsActivity : Activity() {
    private lateinit var mAuth: FirebaseAuth
    private var isLoggedIn: Boolean = false
    private var btnLogIn: MButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()

        btnLogIn = findViewById(R.id.btnLogIn)
        val btnExit = findViewById<MButton>(R.id.btnExit)

        updateLoginButton()

        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        btnLogIn?.setOnClickListener {
            val intent = if (!isLoggedIn) {
                Intent(this, LoginActivity::class.java)
            } else {
                Intent(this, LogoutActivity::class.java)
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        updateLoginButton()
    }

    private fun updateLoginButton() {
        val currentUser: FirebaseUser? = mAuth.currentUser
        isLoggedIn = currentUser != null
        btnLogIn?.text = if (isLoggedIn) "Log Out" else "Log In"
    }
}
