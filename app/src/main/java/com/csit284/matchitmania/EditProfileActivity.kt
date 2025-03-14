package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import firebase.FirebaseRepository
import kotlinx.coroutines.launch
import userGenerated.UserProfile
import views.MButton

class EditProfileActivity : AppCompatActivity() {
    private var tvUser: TextView? = null
    private var userProfile: UserProfile?= null
    private var btnProfile: MButton ?= null
    private var currentBtnProfileId: String ?= null
    private var currentColorId: String ?= null
    private var svAvatar: ScrollView ?= null
    private var svBG: ScrollView ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        btnProfile = findViewById<MButton>(R.id.btnProfile)
        svAvatar = findViewById<ScrollView>(R.id.svAvatar)
        svBG = findViewById<ScrollView>(R.id.svBG)

        userProfile = intent.getSerializableExtra("userProfile") as? UserProfile

        tvUser = findViewById(R.id.tvUsername)
        Log.i("TASK", "Username in edit profile activity ${userProfile?.username}")

        currentBtnProfileId = userProfile?.profileImageId
        currentColorId = userProfile?.profileColor

        loadUserData()
        val btnExit = findViewById<MButton>(R.id.btnExit)

        btnExit.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userProfile", userProfile)
            startActivity(intent)
        }

        findViewById<MButton>(R.id.btnAvatar).setOnClickListener{
            svAvatar?.isGone = false
            svBG?.isGone = true
        }
        findViewById<MButton>(R.id.btnBG).setOnClickListener{
            svAvatar?.isGone = true
            svBG?.isGone = false
        }

        findViewById<MButton>(R.id.btnSave).setOnClickListener{
            userProfile?.username = tvUser?.text.toString()
            userProfile?.profileImageId = currentBtnProfileId!!
            userProfile?.profileColor = currentColorId!!
            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                lifecycleScope.launch {
                    try {
                        FirebaseRepository.setDocument("users", userId, userProfile!!.toMap())
                        val intent = Intent(this@EditProfileActivity, ProfileActivity::class.java)
                        intent.putExtra("userProfile", userProfile)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this@EditProfileActivity, "Failed to save username $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        setupAvatarButtons()
        setupBGButtons()
    }
    private fun setupAvatarButton(buttonId: Int, avatarResourceId: Int) {
        findViewById<MButton>(buttonId).setOnClickListener {
            btnProfile?.let { profile ->
                profile.imageBackground = ContextCompat.getDrawable(this, avatarResourceId)
                profile.invalidate()
            }
            currentBtnProfileId = resources.getResourceEntryName(buttonId)
        }
    }

    // In onCreate or wherever you're setting up these buttons
    private fun setupAvatarButtons() {
        // Map each button ID to its corresponding avatar drawable resource ID
        val avatarPairs = mapOf(
            R.id.avatar1 to R.drawable.avatar1,
            R.id.avatar2 to R.drawable.avatar2,
            R.id.avatar3 to R.drawable.avatar3,
            R.id.avatar4 to R.drawable.avatar4,
            R.id.avatar5 to R.drawable.avatar5,
            R.id.avatar6 to R.drawable.avatar6,
            R.id.avatar7 to R.drawable.avatar7,
            R.id.avatar8 to R.drawable.avatar8,
            R.id.avatar9 to R.drawable.avatar9,
            R.id.avatar10 to R.drawable.avatar10,
            R.id.avatar11 to R.drawable.avatar11,
            R.id.avatar12 to R.drawable.avatar12
        )

        // Set up each avatar button
        avatarPairs.forEach { (buttonId, avatarId) ->
            setupAvatarButton(buttonId, avatarId)
        }
    }
    private fun setupBGButtons() {
        val colorPairs = mapOf(
            R.id.MBlue to R.color.MBlue,
            R.id.MYellow to R.color.MYellow,
            R.id.MPurple to R.color.MPurple,
            R.id.MLavender to R.color.MLavender,
            R.id.MShader to R.color.MShader,
            R.id.MDarkPurple to R.color.MDarkPurple,
            R.id.MOrange to R.color.MOrange,
            R.id.MLightYellow to R.color.MLightYellow,
            R.id.MDarkLavender to R.color.MDarkLavender,
            R.id.MNavy to R.color.MNavy,
            R.id.MGreen to R.color.MGreen,
            R.id.white to R.color.white
        )

        // Set up each avatar button
        colorPairs.forEach { (buttonId, colorId) ->
            setupColorButton(buttonId, colorId)
        }
    }

    private fun setupColorButton(buttonId: Int, colorResourceId: Int) {
        findViewById<MButton>(buttonId).setOnClickListener {
            btnProfile?.let { profile ->
                profile.backColor = ContextCompat.getColor(this, colorResourceId)
                profile.invalidate()
            }
            currentColorId = resources.getResourceEntryName(buttonId)
            Log.i("Task", "$currentColorId is being clicked")
        }
    }

    private fun loadUserData() {
        val username = userProfile?.username ?: "Unknown Player"
        tvUser?.text = username
        try {
            Log.i("TASK", "userProfile has been mapped")
            Log.i("TASK", "UserProfile ${userProfile?.username}")
            val profileImageId = userProfile?.profileImageId
            Log.i("TASK", "UserProfile btnProfile id ${userProfile?.profileImageId}")
            if (!profileImageId.isNullOrEmpty()) {
                val drawableId = resources.getIdentifier(profileImageId, "drawable", packageName)
                if (drawableId != 0) {
                    btnProfile?.imageBackground = ContextCompat.getDrawable(this, drawableId)
                    btnProfile?.invalidate()
                    currentBtnProfileId = profileImageId
                    Log.i("TASK", "btnProfile in edit is now set to $drawableId")
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
}