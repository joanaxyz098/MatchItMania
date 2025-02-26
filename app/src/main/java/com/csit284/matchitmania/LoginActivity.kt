package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import firebase.FirebaseRepository
import kotlinx.coroutines.launch
import views.MButton

class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val userID = auth.currentUser?.uid
    private var username: String ?= null
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupViews()
    }

    private fun fetchUsername(){
        if(userID != null){
            lifecycleScope.launch {
                username = FirebaseRepository.getPartialDocument("users", userID, "username") as String
            }
        }
    }

    private fun setupViews() {
        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        progressBar = findViewById(R.id.pbRegister)

        // Setup button listeners
        findViewById<MButton>(R.id.btnDone).setOnClickListener {
            handleLogin()
        }

        findViewById<MButton>(R.id.btnExit).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        // Validate input
        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password required"
            return
        }

        // Show loading
        progressBar.visibility = View.VISIBLE

        // Attempt login
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    fetchUsername()
                    Toast.makeText(this, "Welcome back! $username", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}