package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import music.BackgroundMusic
import music.SoundEffects
import userGenerated.UserSettings
import views.MButton

class SettingsActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    private lateinit var scMusic: SwitchCompat
    private lateinit var scSound: SwitchCompat
    private lateinit var scVib: SwitchCompat
    private lateinit var btnLogIn: MButton

    private var isGuest = auth.currentUser == null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupViews()
        loadUserSettings()
    }

    override fun onPause() {
        super.onPause()
        saveAndApplySettings() // <- Save and apply settings when user leaves Settings
    }

    private fun setupViews() {
        scMusic = findViewById(R.id.scMusic)
        scSound = findViewById(R.id.scSound)
        scVib = findViewById(R.id.scVibration)
        btnLogIn = findViewById(R.id.btnLogIn)

        findViewById<MButton>(R.id.btnExit).setOnClickListener {
            navigateToHome()
        }

        findViewById<MButton>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        updateLoginButton()

        scMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                BackgroundMusic.play()
            } else {
                BackgroundMusic.pause()
            }
            // Also update app-wide settings immediately
            (application as MatchItMania).userSettings = (application as MatchItMania).userSettings.copy(
                music = isChecked
            )
        }

        scSound.setOnCheckedChangeListener { _, isChecked ->
            // Toggle sound effects enabled/disabled
            SoundEffects.setEnabled(isChecked)

            // Also update app-wide settings immediately
            (application as MatchItMania).userSettings = (application as MatchItMania).userSettings.copy(
                sound = isChecked
            )
        }
    }

    private fun updateSwitch(userSettings: UserSettings) {
        Log.i("TASK", "Initializing Settings: $userSettings")
        scMusic.isChecked = userSettings.music ?: true
        scSound.isChecked = userSettings.sound ?: true
        scVib.isChecked = userSettings.vibration ?: true
    }

    private fun saveAndApplySettings() {
        val updatedSettings = UserSettings(
            scMusic.isChecked,
            scSound.isChecked,
            scVib.isChecked
        )

        (application as MatchItMania).userSettings = updatedSettings

        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("settings")
                        .document(userId)
                        .set(
                            mapOf(
                                "music" to updatedSettings.music,
                                "sound" to updatedSettings.sound,
                                "vibration" to updatedSettings.vibration
                            )
                        )
                        .await()

                    Log.i("TASK", "Settings successfully saved!")
                } catch (e: Exception) {
                    Log.e("TASK", "Failed to save settings: ${e.message}", e)
                    Toast.makeText(this@SettingsActivity, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserSettings() {
        updateSwitch((application as MatchItMania).userSettings)
    }

    private fun updateLoginButton() {
        btnLogIn.text = if (isGuest) "Log In" else "Log Out"

        btnLogIn.setOnClickListener {
            if (isGuest) {
                startActivity(Intent(this, UserActivity::class.java))
            } else {
                handleLogout()
            }
        }
    }

    private fun handleLogout() {
        val intent = Intent(this, MessageActivity::class.java)
        intent.putExtra("MESSAGE", "Are you sure you want to logout?")
        startActivity(intent)
        saveAndApplySettings()
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        saveAndApplySettings()
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val musicEnabled = (application as MatchItMania).userSettings.music ?: true
        if (musicEnabled) {
            BackgroundMusic.play()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHome()
    }
}
