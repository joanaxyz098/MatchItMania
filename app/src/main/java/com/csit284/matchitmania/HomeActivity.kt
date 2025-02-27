package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import firebase.FirebaseRepository
import kotlinx.coroutines.launch
import music.BackgroundMusic
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class HomeActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private var userProfile: UserProfile? = null
    private var userSettings: UserSettings = UserSettings()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize music at app start
        BackgroundMusic.initialize(this)

        loadUserData()
        setupViews()
    }

    override fun onPause() {
        super.onPause()
        BackgroundMusic.pause()
    }

    override fun onResume() {
        super.onResume()
        loadUserData() // Reload settings when returning
    }

    override fun onDestroy() {
        super.onDestroy()
        BackgroundMusic.release()
    }

    private fun setupViews() {
        findViewById<MButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        findViewById<MButton>(R.id.btnProfile).setOnClickListener {

            val intent = Intent(this, ProfileActivity::class.java)
            if(!userProfile?.username.isNullOrEmpty()) {
                intent.putExtra("userProfile", userProfile)
                startActivity(intent)
                Log.i("TASK", "userProfile has been passed to Profile Activity: ")
                Log.i("TASK", "UserProfile ${userProfile?.username}")
            }else Log.i("TASK", "user profile is empty")
            }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val documentSData = FirebaseRepository.getDocumentById("settings", userId)
                val documentPData = FirebaseRepository.getDocumentById("users", userId)

                if (documentSData != null) {
                    userSettings = UserSettings.fromMap(documentSData)
                    Log.i("HomeActivity", "Settings loaded: ${userSettings}")

                    // Play or stop music based on updated settings
                    if (userSettings.music) {
                        BackgroundMusic.play()
                    } else {
                        BackgroundMusic.pause()
                    }
                } else {
                    userSettings = UserSettings() // Default settings
                }

                if (documentPData != null) {
                    userProfile = UserProfile.fromMap(documentPData)
                    Log.i("TASK", "userProfile has been mapped")
                    Log.i("TASK", "UserProfile ${userProfile?.username}")
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Failed to load user data", e)
            }
        }
    }

}
