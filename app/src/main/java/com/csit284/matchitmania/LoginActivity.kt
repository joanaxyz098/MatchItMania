package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import views.MButton

class LoginActivity : Activity() {
    private var etEmail: EditText? = null
    private var etPass: EditText? = null
    private var btnLogIn: MButton? = null
    private var mAuth: FirebaseAuth? = null
    private var pbRegister: ProgressBar? = null

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = mAuth?.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI components
        mAuth = FirebaseAuth.getInstance()
        etEmail = findViewById(R.id.etEmail)
        etPass = findViewById(R.id.etPassword)
        btnLogIn = findViewById(R.id.btnDone)
        pbRegister = findViewById(R.id.pbRegister)

        val btnExit = findViewById<MButton>(R.id.btnExit)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Exit button listener
        btnExit.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Register button listener
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Login button listener
        btnLogIn?.setOnClickListener {
            pbRegister?.visibility = View.VISIBLE
            val email = etEmail?.text?.toString()?.trim() ?: ""
            val password = etPass?.text?.toString() ?: ""

            // Input validation
            if (email.isEmpty()) {
                etEmail?.error = "Email required"
                pbRegister?.visibility = View.GONE
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPass?.error = "Password required"
                pbRegister?.visibility = View.GONE
                return@setOnClickListener
            }

            // Firebase Authentication
            mAuth?.signInWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->
                    pbRegister?.visibility = View.GONE  // Always hide ProgressBar
                    if (task.isSuccessful) {
                        val user = mAuth?.currentUser
                        Toast.makeText(this, "Welcome back, ${user?.email}!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()  // Close LoginActivity
                    } else {
                        val errorMessage = task.exception?.message
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
