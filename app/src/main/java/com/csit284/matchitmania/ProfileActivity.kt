package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import views.MButton
import firebase.FirebaseRepository
import userGenerated.UserProfile

class ProfileActivity : AppCompatActivity() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var tvUser: TextView? = null
    private var userProfile: UserProfile ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userName = intent.getStringExtra("userName")


        tvUser = findViewById(R.id.tvUsername)
        Log.i("TASK", "Username in profile activity ${userProfile?.username}")

        tvUser?.text = userName

        val btnExit = findViewById<MButton>(R.id.btnExit)

        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Load user data when the activity starts
//        fetchUserProfile()
    }

//    private fun fetchUserProfile() {
//        if (userId != null) {
//            lifecycleScope.launch {
//                val documentData = FirebaseRepository.getDocumentById("users", userId)
//                if (documentData != null) {
//                    val userProfile = UserProfile.fromMap(documentData)
//                    tvUser?.text = userProfile.username
//                } else {
//                    Log.e("Firestore", "User not found")
//                    tvUser?.text = "User not found"
//                }
//            }
//        }
//    }


}
