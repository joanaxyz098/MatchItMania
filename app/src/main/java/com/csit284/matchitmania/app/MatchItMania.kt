package com.csit284.matchitmania.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import userGenerated.UserProfile
import userGenerated.UserSettings

class MatchItMania : Application() {
    var userProfile: UserProfile = UserProfile()
    var userSettings: UserSettings = UserSettings()
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            val currentUser = auth.currentUser
            if (currentUser != null) {
                fetchUserProfile(currentUser.uid) { Log.d("MatchItMania", "User profile loaded in application class") }
                fetchUserSettings(currentUser.uid)
            }
        } catch (e: Exception) {
            Log.e("MatchItMania", "Firebase initialization error", e)
        }
    }

    private fun fetchUserProfile(uid: String, onComplete: () -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userProfile.apply {
                        email = document.getString("email") ?: ""
                        username = document.getString("username") ?: ""
                        profileImageId = document.getString("profileImageId") ?: ""
                        profileColor = document.getString("profileColor") ?: ""
                        level = document.getLong("level")?.toInt() ?: 0
                    }
                    Log.d("Firestore", "User Profile fetched successfully: ${userProfile.username}")
                } else {
                    Log.d("Firestore", "User profile does not exist")
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user profile", e)
                onComplete()
            }
    }

    private fun fetchUserSettings(uid: String) {
        db.collection("settings").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userSettings.apply {
                        music = document.getBoolean("music") ?: true
                        sound = document.getBoolean("sound") ?: true
                        vibration = document.getBoolean("vibration") ?: true
                    }
                    Log.d("Firestore", "User Settings fetched successfully")
                } else {
                    Log.d("Firestore", "User settings do not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user settings", e)
            }
    }
}
