package com.csit284.matchitmania

import UserSettings
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import firebase.UserSettingsRepository
import kotlinx.coroutines.launch
import userGenerated.UserProfile
import views.MButton

class SettingsActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val userSettingsRepository = UserSettingsRepository()

    private lateinit var scMusic: SwitchCompat
    private lateinit var scSound: SwitchCompat
    private lateinit var scVib: SwitchCompat
    private lateinit var btnLogIn: MButton

    private var userSettings: UserSettings? = null
    private var userProfile: UserProfile? = null
    private var isGuest: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Get data from intent
        userSettings = intent.getParcelableExtra("userSettings")
        userProfile = intent.getParcelableExtra("userProfile")
        isGuest = intent.getBooleanExtra("isGuest", true)

        setupViews()
        initializeSettings()
    }

    private fun setupViews() {
        // Initialize switches
        scMusic = findViewById(R.id.scMusic)
        scSound = findViewById(R.id.scSound)
        scVib = findViewById(R.id.scVibration)

        // Initialize buttons
        btnLogIn = findViewById(R.id.btnLogIn)

        findViewById<MButton>(R.id.btnExit).setOnClickListener {
            navigateToHome()
        }

        findViewById<MButton>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // Setup switch listeners
        scMusic.setOnCheckedChangeListener { _, isChecked -> updateSettings(music = isChecked) }
        scSound.setOnCheckedChangeListener { _, isChecked -> updateSettings(sound = isChecked) }
        scVib.setOnCheckedChangeListener { _, isChecked -> updateSettings(vibration = isChecked) }

        // Setup login/logout button
        updateLoginButton()
    }

    private fun initializeSettings() {
        scMusic.isChecked = userSettings?.music ?: true
        scSound.isChecked = userSettings?.sound ?: true
        scVib.isChecked = userSettings?.vibration ?: true
    }

    private fun updateSettings(
        music: Boolean? = null,
        sound: Boolean? = null,
        vibration: Boolean? = null
    ) {
        userSettings = UserSettings(
            music = music ?: scMusic.isChecked,
            sound = sound ?: scSound.isChecked,
            vibration = vibration ?: scVib.isChecked
        )

        // Save settings to Firebase only if user is logged in
        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                    userSettingsRepository.updateUserSettings(userId, userSettings!!)
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateLoginButton() {
        btnLogIn.text = if (isGuest) "Log In" else "Log Out"

        btnLogIn.setOnClickListener {
            if (isGuest) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                handleLogout()
            }
        }
    }

    private fun handleLogout() {
        auth.signOut()
        userProfile = null
        userSettings = UserSettings(music = true, sound = true, vibration = true)
        isGuest = true
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        updateLoginButton()
        initializeSettings()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("userSettings", userSettings)
            putExtra("userProfile", userProfile)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }
}