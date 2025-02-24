package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import views.MButton

class RegisterActivity : Activity() {
    private var etEmail: EditText? = null
    private var etPass: EditText? = null
    private var etUser: EditText? = null
    private var etConfirmPass: EditText? = null
    private var btnRegister: MButton? = null
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
        setContentView(R.layout.activity_register)

        // Initialize UI elements
        mAuth = FirebaseAuth.getInstance()
        etUser = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPass = findViewById(R.id.etPassword)
        etConfirmPass = findViewById(R.id.etCPassword)
        btnRegister = findViewById(R.id.btnDone)
        pbRegister = findViewById(R.id.pbRegister)

        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val btnExit = findViewById<MButton>(R.id.btnExit)
        btnExit.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        btnRegister?.setOnClickListener {
            pbRegister?.visibility = View.VISIBLE

            val user = etUser?.text?.toString()?.trim() ?: ""
            val email = etEmail?.text?.toString()?.trim() ?: ""
            val password = etPass?.text?.toString() ?: ""
            val cPassword = etConfirmPass?.text?.toString() ?: ""

            // Input validation
            if (user.isEmpty()) {
                etUser?.error = "Username required"
                pbRegister?.visibility = View.GONE
                return@setOnClickListener
            }

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

            if (password != cPassword) {
                etConfirmPass?.error = "Passwords don't match"
                pbRegister?.visibility = View.GONE
                return@setOnClickListener
            }

            // Firebase Authentication
            mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->
                    pbRegister?.visibility = View.GONE  // Hide progress bar in both cases

                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Welcome to Match It Mania! $user has been successfully registered",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = task.exception?.message
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
