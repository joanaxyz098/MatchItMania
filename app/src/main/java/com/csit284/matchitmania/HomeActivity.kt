package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private var btnHome: MButton ?= null
    private var btnLeaderb: MButton ?= null
    private var btnFriends: MButton ?= null
    private var activeButton: MButton ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        loadUserData()
        // Initialize music at app start
        BackgroundMusic.initialize(this)
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
        btnHome = findViewById(R.id.btnHome)
        btnLeaderb = findViewById(R.id.btnboards)
        btnFriends = findViewById(R.id.btnFriends)
        activeButton = btnHome

        // Set initial active button state
        btnHome?.let {
            setButtonActive(it)
        }

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

        btnHome?.setOnClickListener{
            setButtonInactive(activeButton)
            setButtonActive(btnHome)
            activeButton = btnHome
        }

        btnLeaderb?.setOnClickListener{
            setButtonInactive(activeButton)
            setButtonActive(btnLeaderb)
            activeButton = btnLeaderb
        }

        btnFriends?.setOnClickListener{
            setButtonInactive(activeButton)
            setButtonActive(btnFriends)
            activeButton = btnFriends
        }
    }

    private fun setButtonActive(button: MButton?) {
        button?.apply {
            backColor = Color.parseColor("#AED9D2")
            backHeightScale = 0.7f
            backVerticalOffset = 1.1f
            imageScale = 1.2f
            imageVerticalOffset = 0.5f
        }
    }

    private fun setButtonInactive(button: MButton?) {
        button?.apply {
            backColor = Color.parseColor("#56557F")
            backHeightScale = 0.58f
            backVerticalOffset = 1.1f
            imageScale = 1f
            imageVerticalOffset = 0.6f
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
                    val profileImageId = userProfile?.profileImageId
                    val profileColor = userProfile?.profileColor
                    Log.i("TASK", "UserProfile btnProfile id ${userProfile?.profileImageId}")
                    if (!profileImageId.isNullOrEmpty()) {
                        val drawableId = resources.getIdentifier(profileImageId, "drawable", packageName)
                        val colorId = resources.getIdentifier(profileColor, "color", packageName)
                        if (drawableId != 0) {
                            setUpImageBgButton(drawableId)
                            setupColorButton(R.id.btnProfile, colorId)
                            Log.i("TASK", "btnProfile is now set to $drawableId")
                        } else {
                            Log.e("TASK", "Drawable not found: $profileImageId")
                        }
                    } else {
                        Log.e("TASK", "profileImageId is null or empty")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Failed to load user data", e)
            }
        }
    }

    private fun setUpImageBgButton(avatarResourceId: Int) {
        findViewById<MButton>(R.id.btnProfile).let { profile ->
            profile.imageBackground = ContextCompat.getDrawable(this, avatarResourceId)
            // No need to call invalidate() if proper setter is implemented
        }
    }

    private fun setupColorButton(buttonId: Int, colorResourceId: Int) {
        findViewById<MButton>(buttonId).let { profile ->
            profile.backColor = ContextCompat.getColor(this, colorResourceId)
            // No need to call invalidate() if proper setter is implemented
        }
    }
}