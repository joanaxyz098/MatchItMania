package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth

class LoadingActivity : Activity() {
    private lateinit var app: MatchItMania
    private val INITIALIZATION_TIMEOUT = 10000L // 10 seconds timeout
    private var shouldRefresh = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        app = application as MatchItMania

        shouldRefresh = intent.getBooleanExtra("REFRESH_DATA", false)
        if (shouldRefresh) {
            app.refreshAllData()
        }


        // Register a callback that will be invoked when initialization is complete
        app.setOnInitCompleteListener {
            Log.d("LoadingActivity", "Received init complete callback")
            navigateToNextScreen()
        }

        // Start timeout check in case callback doesn't fire
        startTimeoutCheck()
    }

    override fun onResume() {
        super.onResume()
        // Check init status immediately in case it completed between onCreate and onResume
        if (app.isInitializationComplete()) {
            Log.d("LoadingActivity", "Initialization already complete in onResume")
            navigateToNextScreen()
        }
    }

    private fun startTimeoutCheck() {
        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            // If we're still in this activity after the timeout
            if (!isFinishing) {
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime >= INITIALIZATION_TIMEOUT) {
                    Log.e("LoadingActivity", "Initialization timed out after $elapsedTime ms")

                    // Report to the user
                    Toast.makeText(
                        this,
                        "Loading timed out. Please check your internet connection.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Force navigation to prevent being stuck
                    navigateToNextScreen(forceLogin = true)
                }
            }
        }, INITIALIZATION_TIMEOUT)
    }

    private fun navigateToNextScreen(forceLogin: Boolean = false) {
        // Determine which screen to show next
        if (forceLogin || FirebaseAuth.getInstance().currentUser == null) {
            navigateToLogin()
        } else {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        Log.d("LoadingActivity", "Navigating to Home")
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        Log.d("LoadingActivity", "Navigating to Login")
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
        finish()
    }
}