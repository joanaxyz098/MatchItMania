package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import music.BackgroundMusic
import music.GameBGMusic
import views.MButton
import userGenerated.UserProfile

class ProfileActivity : AppCompatActivity() {
    private var tvUser: TextView? = null
    private var tvLevel: TextView? = null
    private var userProfile: UserProfile ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userProfile = intent.getSerializableExtra("USERPROFILE") as UserProfile?
        if(userProfile == null) userProfile = (application as MatchItMania).userProfile

        tvUser = findViewById(R.id.tvUsername)
        tvLevel = findViewById(R.id.tvLevel)
        Log.i("TASK", "Username in profile activity ${userProfile?.level}")

        val username = userProfile?.username ?: "Unknown Player"
        val fullText = "Player: $username"

        val spannable = SpannableString(fullText)

        spannable.setSpan(
            StyleSpan(Typeface.BOLD), // Bold style
            0, 7, // "Player:" index range
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.MYellow)), // Change color (e.g., blue)
            8, fullText.length, // Username index range
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvUser?.text = spannable
        tvLevel?.text = userProfile?.level.toString()
        val highestScore = userProfile?.highestScore
        findViewById<TextView>(R.id.tvScore).text = if(highestScore == 0){
            "???"
        }else {
            highestScore.toString()
        }
        val maxCombo = userProfile?.maxCombo
        findViewById<TextView>(R.id.tvCombo).text = if(maxCombo == 0) {
            "???"
        }else{
            maxCombo.toString()
        }
        val losses = userProfile?.losses
        findViewById<TextView>(R.id.tvLosses).text = if(losses == 0){
            "???"
        }else{
            losses.toString()
        }
        val remainingMillis = userProfile?.fastestClear
        findViewById<TextView>(R.id.tvTime).text = if(remainingMillis == Long.MAX_VALUE){
            "???"
        }else{
            val totalSeconds = (remainingMillis?.div(1000))?.toInt()
            val minutes = totalSeconds?.div(60)
            val seconds = totalSeconds?.rem(60)
            String.format("%d:%02d", minutes, seconds)
        }

        val btnExit = findViewById<MButton>(R.id.btnExit)

        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userProfile", userProfile)
            startActivity(intent)
        }
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        val settings = (application as MatchItMania).userSettings
        val musicEnabled = settings.music ?: true
        val gameMusicEnabled = settings.music ?: true

        if (musicEnabled) {
            BackgroundMusic.play()
        } else {
            BackgroundMusic.pause()
        }

        if (gameMusicEnabled) {
            GameBGMusic.play()
        } else {
            GameBGMusic.pause()
        }
    }

    private fun loadUserData() {
        try {
            val profileImageId = userProfile?.profileImageId
            val profileColor = userProfile?.profileColor
            Log.i("TASK", "UserProfile btnProfile id ${userProfile?.profileImageId}")
            if (!profileImageId.isNullOrEmpty()) {
                val drawableId = resources.getIdentifier(profileImageId, "drawable", packageName)
                val colorId = resources.getIdentifier(profileColor, "color", packageName)
                if (drawableId != 0) {
                    setupAvatarButton(drawableId)
                    setupColorButton(colorId)
                    Log.i("TASK", "btnProfile is now set to $drawableId")
                } else {
                    Log.e("TASK", "Drawable not found: $profileImageId")
                }
            } else {
                Log.e("TASK", "profileImageId is null or empty")
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Failed to load user data", e)
        }
    }
    private fun setupAvatarButton(avatarResourceId: Int) {
        findViewById<MButton>(R.id.btnProfile).let { profile ->
            profile.imageBackground = ContextCompat.getDrawable(this, avatarResourceId)
            profile.invalidate()
        }
    }
    private fun setupColorButton(colorResourceId: Int) {
        findViewById<MButton>(R.id.btnProfile).let { profile ->
                profile.backColor = ContextCompat.getColor(this, colorResourceId)
                profile.invalidate()
        }
    }
}
