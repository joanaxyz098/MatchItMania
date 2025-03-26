package com.csit284.matchitmania

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.csit284.matchitmania.helper.MListView
import com.google.firebase.firestore.FirebaseFirestore
import userGenerated.UserProfile
import views.MButton

class LeaderboardActivity : AppCompatActivity() {
    private var btnHome: MButton? = null
    private var btnLeaderb: MButton? = null
    private var btnFriends: MButton? = null
    private var activeButton: MButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val db = FirebaseFirestore.getInstance()
        val users = mutableListOf<UserProfile>()
        val lvLeaderboard = findViewById<ListView>(R.id.lvboard)

        btnHome = findViewById(R.id.btnHome)
        btnLeaderb = findViewById(R.id.btnboards)
        btnFriends = findViewById(R.id.btnFriends)
        activeButton = btnLeaderb

        btnLeaderb?.let {
            setButtonActive(it)
            setButtonInactive(btnHome)
        }

        db.collection("users").get()
            .addOnSuccessListener { documents ->
                for ((index, document) in documents.withIndex()) {
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageId = document.getString("profileImageId") ?: "avatar1"
                    val profileColor = document.getString("profileColor") ?: "MBlue"
                    val level = document.getLong("level")?.toInt() ?: 0
                    users.add(UserProfile(username, "", profileImageId, profileColor, level))
                }
                users.sortedByDescending { it.level }
                Log.d("Firestore", "User Profiles: $users")

                val customAdapter = MListView(this, users)
                lvLeaderboard.adapter = customAdapter
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting usernames", exception)
            }

        // Button Click Listeners
        btnHome?.setOnClickListener {
            setButtonInactive(activeButton)
            setButtonActive(btnHome)
            activeButton = btnHome
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnLeaderb?.setOnClickListener {
            setButtonInactive(activeButton)
            setButtonActive(btnLeaderb)
            activeButton = btnLeaderb
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        btnFriends?.setOnClickListener {
            setButtonInactive(activeButton)
            setButtonActive(btnFriends)
            activeButton = btnFriends
        }
    }

    private fun setButtonActive(button: MButton?) {
        button?.apply { backColor = Color.parseColor("#AED9D2") }
    }

    private fun setButtonInactive(button: MButton?) {
        button?.apply { backColor = Color.parseColor("#56557F") }
    }
}
