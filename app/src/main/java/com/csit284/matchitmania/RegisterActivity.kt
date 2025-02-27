package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import extensions.fieldEmpty
import firebase.FirebaseRepository
import views.MButton
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var etUser: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnRegister: MButton
    private lateinit var pbRegister: ProgressBar
    private lateinit var mAuth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
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
        tvLogin.paintFlags = tvLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<MButton>(R.id.btnExit).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnRegister.setOnClickListener {
            pbRegister.visibility = View.VISIBLE

            val user = etUser.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPass.text.toString()
            val cPassword = etConfirmPass.text.toString()



            // Input validation
            when {
                user.fieldEmpty(etUser) -> {
                    pbRegister.visibility = View.GONE
                    return@setOnClickListener
                }
                email.fieldEmpty(etEmail)-> {
                    pbRegister.visibility = View.GONE
                    return@setOnClickListener
                }
                password.fieldEmpty(etPass) -> {
                    pbRegister.visibility = View.GONE
                    return@setOnClickListener
                }
                password != cPassword -> {
                    etConfirmPass.error = "Passwords don't match"
                    pbRegister.visibility = View.GONE
                    return@setOnClickListener
                }
            }

            // Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = mAuth.currentUser?.uid
                        if (userId != null) {
                            lifecycleScope.launch {
                                FirebaseRepository.setDocument("users",
                                    userId,
                                    mapOf("username" to user, "email" to email)
                                )
                                Toast.makeText(this@RegisterActivity, "Player $user registered!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            pbRegister.visibility = View.GONE
                            Toast.makeText(this, "User registration failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        pbRegister.visibility = View.GONE
                        val errorMessage = task.exception?.message ?: "Registration failed"
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
