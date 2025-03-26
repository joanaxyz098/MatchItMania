package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.app.MatchItMania
import music.BackgroundMusic
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class HomeActivity : AppCompatActivity() {
//    private val auth = FirebaseAuth.getInstance()
    private var userProfile: UserProfile ?= null
    private var userSettings: UserSettings ?= null
    private var btnHome: MButton ?= null
    private var btnLeaderb: MButton ?= null
    private var btnFriends: MButton ?= null
    private var activeButton: MButton ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val matchItMania = application as MatchItMania

        // Check if data is already loaded
        userProfile = matchItMania.userProfile
        userSettings = matchItMania.userSettings

        loadUserData()
        setupViews()
        // Initialize music at app start
        BackgroundMusic.initialize(this)
    }

    override fun onPause() {
        super.onPause()
        BackgroundMusic.pause()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
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
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
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
        val matchItMania = application as MatchItMania
        userProfile = matchItMania.userProfile
        userSettings = matchItMania.userSettings

        // Handle music settings
        if (userSettings?.music == true) {
            BackgroundMusic.play()
        } else {
            BackgroundMusic.pause()
        }

        // Handle profile button update
        val profileImageId = userProfile?.profileImageId
        val profileColor = userProfile?.profileColor

        if (!profileImageId.isNullOrEmpty()) {
            try {
                val drawableId = resources.getIdentifier(profileImageId, "drawable", packageName)
                val colorId = resources.getIdentifier(profileColor, "color", packageName)

                Log.d("TASK", "Profile Image ID: $profileImageId")
                Log.d("TASK", "Profile Color: $profileColor")
                Log.d("TASK", "Drawable ID: $drawableId")
                Log.d("TASK", "Color ID: $colorId")

                if (drawableId != 0) {
                    val profileButton = findViewById<MButton>(R.id.btnProfile)

                    // Set image background
                    val drawable = ContextCompat.getDrawable(this, drawableId)
                    profileButton.imageBackground = drawable

                    // Set background color
                    if (colorId != 0) {
                        val color = ContextCompat.getColor(this, colorId)
                        profileButton.backColor = color
                    }

                    Log.i("TASK", "btnProfile updated successfully")
                } else {
                    Log.e("TASK", "Drawable not found: $profileImageId")
                }
            } catch (e: Exception) {
                Log.e("TASK", "Error updating profile button", e)
            }
        } else {
            Log.e("TASK", "profileImageId is null or empty")
        }
    }
}