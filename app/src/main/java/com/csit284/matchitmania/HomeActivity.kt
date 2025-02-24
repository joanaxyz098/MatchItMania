package com.csit284.matchitmania

import UserSettings
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import firebase.UserRepository
import firebase.UserSettingsRepository
import kotlinx.coroutines.launch
import userGenerated.UserProfile
import views.MButton

class HomeActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val userSettingsRepository = UserSettingsRepository()
    private val userRepository = UserRepository()
    private var userProfile: UserProfile? = null
    private var userSettings: UserSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupViews()
        loadUserData()
    }

    private fun setupViews() {
        // Settings button
        findViewById<MButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java).apply {
                putExtra("userSettings", userSettings)
                putExtra("userProfile", userProfile)
                putExtra("isGuest", auth.currentUser == null)
            }
            startActivity(intent)
        }

        // Profile button
        findViewById<MButton>(R.id.btnProfile).setOnClickListener {
            if (auth.currentUser == null) {
                // If guest, prompt to login
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            // Guest user - use default settings
            userSettings = UserSettings(
                music = true,
                sound = true,
                vibration = true
            )
            return
        }

        lifecycleScope.launch {
            try {
                // Load user settings
                userSettings = userSettingsRepository.loadUserSettings(userId) ?: UserSettings(
                    music = true,
                    sound = true,
                    vibration = true
                )

                // Load user profile
                userProfile = userRepository.loadUserProfile(userId)

                // Update settings if needed
                userSettingsRepository.updateUserSettings(userId, userSettings!!)
            } catch (e: Exception) {
                // Handle error appropriately
            }
        }
    }
}