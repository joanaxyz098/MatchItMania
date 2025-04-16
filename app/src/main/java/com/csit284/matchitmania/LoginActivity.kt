package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import extensions.fieldEmpty
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private var userID : String ?= null
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
        username = (application as MatchItMania).userProfile?.username
        Toast.makeText(this@LoginActivity, "Welcome back! $username", Toast.LENGTH_SHORT).show()
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

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if(email.fieldEmpty(etEmail, "Please input your email")) return
        if (password.fieldEmpty(etPassword, "Please input your password")) return

        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val app = application as MatchItMania
                    app.userProfile = UserProfile()
                    app.userSettings = UserSettings()

                    fetchUsername()  // Load fresh data
                    navigateToHome()
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun navigateToHome() {
        val intent = Intent(this, LoadingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}