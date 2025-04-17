package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.app.MatchItMania
import com.csit284.matchitmania.fragments.CommunityFragment
import com.csit284.matchitmania.fragments.HomeFragment
import com.csit284.matchitmania.fragments.LeaderboardFragment
import com.csit284.matchitmania.fragments.LoginFragment
import com.csit284.matchitmania.interfaces.Clickable
import music.BackgroundMusic
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class HomeActivity : AppCompatActivity(), Clickable {
//    private val auth = FirebaseAuth.getInstance()
    private var btnHome: MButton ?= null
    private var btnLeaderb: MButton ?= null
    private var btnFriends: MButton ?= null
    private var activeButton: MButton ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fHome, homeFragment)
            .commit()
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
        BackgroundMusic.play()
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

        btnHome?.setOnClickListener{
            setButtonInactive(activeButton)
            setButtonActive(btnHome)
            activeButton = btnHome
            val fragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fHome, fragment)
                .commit()
        }

        btnLeaderb?.setOnClickListener{
            setButtonInactive(activeButton)
            setButtonActive(btnLeaderb)
            activeButton = btnLeaderb
            val fragment = LeaderboardFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fHome, fragment)
                .commit()
        }

        btnFriends?.setOnClickListener{
            setButtonInactive(activeButton)
            setButtonActive(btnFriends)
            activeButton = btnFriends
            val fragment = CommunityFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fHome, fragment)
                .commit()
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

    override fun onClicked(condition: String) {
       //
    }
}