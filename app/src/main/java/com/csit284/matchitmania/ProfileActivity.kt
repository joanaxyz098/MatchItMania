package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import views.MButton
import firebase.FirebaseRepository
import userGenerated.UserProfile

class ProfileActivity : AppCompatActivity() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val userRepository = FirebaseRepository("users", UserProfile::class.java)

    private var tvUser: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUser = findViewById(R.id.tvUsername)
        val btnExit = findViewById<MButton>(R.id.btnExit)

        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Load user data when the activity starts
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        if (userId != null) {
            lifecycleScope.launch {
                val userProfile = userRepository.getDocumentById(userId)
                if (userProfile != null) {
                    tvUser?.setText(userProfile.username)
                } else {
                    Log.e("Firestore", "User not found")
                }
            }
        }
    }
}
