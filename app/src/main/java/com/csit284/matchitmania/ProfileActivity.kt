package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import views.MButton
import firebase.FirebaseRepository
import music.BackgroundMusic
import userGenerated.UserProfile
import userGenerated.UserSettings

class ProfileActivity : AppCompatActivity() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var tvUser: TextView? = null
    private var userProfile: UserProfile ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userProfile = intent.getSerializableExtra("userProfile") as? UserProfile


        tvUser = findViewById(R.id.tvUsername)
        Log.i("TASK", "Username in profile activity ${userProfile?.username}")

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
