package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import firebase.FirebaseRepository
import kotlinx.coroutines.launch
import music.BackgroundMusic
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class SettingsActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    private lateinit var scMusic: SwitchCompat
    private lateinit var scSound: SwitchCompat
    private lateinit var scVib: SwitchCompat
    private lateinit var btnLogIn: MButton

    private var userSettings: UserSettings? = null
    private var userProfile: UserProfile? = null
    private var isGuest = auth.currentUser == null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupViews()
        loadUserSettings()
    }

    override fun onDestroy() {
        updateSettings()
        super.onDestroy()
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
        // Setup login/logout button
        updateLoginButton()
        onScMusic()
    }

    private fun onScMusic(){
        scMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                BackgroundMusic.play()
            } else {
                BackgroundMusic.pause()
            }
        }
    }

    private fun updateSwitch() {
        Log.i("TASK", "Initializing Settings: nimal1 $userSettings")

        // Set the correct values from userSettings
        scMusic.isChecked = userSettings?.music ?: true
        scSound.isChecked = userSettings?.sound ?: true
        scVib.isChecked = userSettings?.vibration ?: true

        Log.i("TASK", "Initializing Settings: nimal2 $userSettings")
    }


    private fun updateSettings() {
        Log.i("TASK", "Before Updating Settings: $userSettings")

        userSettings = UserSettings(
            scMusic.isChecked,
            scSound.isChecked,
            scVib.isChecked
        )

        // Save settings to Firebase only if user is logged in
        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                   FirebaseRepository.setDocument("settings", userId, userSettings!!.toMap())
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Log.i("TASK", "After Updating Settings: $userSettings")
    }


    private fun loadUserSettings() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val documentData = FirebaseRepository.getDocumentById("settings", userId)
                if (documentData != null) {
                    userSettings = UserSettings.fromMap(documentData)

                    Log.i("TASK", "documentdata: $documentData userSettings: $userSettings")
                    updateSwitch()

                } else {
                    userSettings = UserSettings()
                    updateSwitch()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Failed to load settings", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(this, LogoutActivity::class.java)
        startActivity(intent)
        updateSettings()
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        updateSettings()
        startActivity(intent)
        finish()
    }
}