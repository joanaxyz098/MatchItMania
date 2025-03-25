package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import extensions.fieldEmpty
import views.MButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var etUser: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnRegister: MButton
    private lateinit var pbRegister: ProgressBar
    private lateinit var mAuth: FirebaseAuth

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
        tvLogin.paintFlags = tvLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<MButton>(R.id.btnExit).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        pbRegister.visibility = View.VISIBLE
        btnRegister.isEnabled = false // Disable button to prevent multiple clicks

        val user = etUser.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPass.text.toString()
        val cPassword = etConfirmPass.text.toString()

        // Input validation
        when {
            user.fieldEmpty(etUser) || email.fieldEmpty(etEmail) || password.fieldEmpty(etPass) -> {
                pbRegister.visibility = View.GONE
                btnRegister.isEnabled = true
                return
            }
            password != cPassword -> {
                etConfirmPass.error = "Passwords don't match"
                pbRegister.visibility = View.GONE
                btnRegister.isEnabled = true
                return
            }
        }

        // Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser?.uid
                    if (userId != null) {
                        saveUserData(userId, user, email)
                    } else {
                        handleRegistrationFailure("User registration failed")
                    }
                } else {
                    handleRegistrationFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun saveUserData(userId: String, username: String, email: String) {
        lifecycleScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(
                        mapOf(
                            "username" to username,
                            "email" to email,
                            "profileImageId" to "avatar1"
                        )
                    ).await()

                Toast.makeText(this@RegisterActivity, "Player $username registered!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("REGISTER", "Failed to save user data: ${e.message}", e)
                handleRegistrationFailure("Failed to save user data")
            }
        }
    }

    private fun handleRegistrationFailure(errorMessage: String) {
        pbRegister.visibility = View.GONE
        btnRegister.isEnabled = true
        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
    }
}
