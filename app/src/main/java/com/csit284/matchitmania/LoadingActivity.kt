package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoadingActivity : Activity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserProfile(currentUser.uid)
        } else {
            navigateToLogin()
        }
    }

    private fun fetchUserProfile(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    (application as MatchItMania).userProfile.apply {
                        email = document.getString("email") ?: ""
                        username = document.getString("username") ?: ""
                        profileImageId = document.getString("profileImageId") ?: ""
                        profileColor = document.getString("profileColor") ?: ""
                        level = document.getLong("level")?.toInt() ?: 0
                    }

                    Log.d("LoadingActivity", "User data loaded successfully: ${(application as MatchItMania).userProfile.username}")
                    navigateToHome()
                } else {
                    Log.d("LoadingActivity", "No user profile found")
                    navigateToLogin()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoadingActivity", "Error fetching user profile", e)
                navigateToLogin()
            }
    }


    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
