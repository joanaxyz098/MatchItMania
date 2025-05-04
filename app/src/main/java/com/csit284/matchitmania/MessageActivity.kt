package com.csit284.matchitmania

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import music.BackgroundMusic
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class MessageActivity :Activity() {
    private var btnYes: MButton? = null
    private var btnNo: MButton? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        btnYes = findViewById(R.id.btnYes)
        btnNo = findViewById(R.id.btnNo)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)

        val message = intent.getStringExtra("MESSAGE") ?: "Message"
        val type = intent.getStringExtra("TYPE") ?: ""

        tvMessage.text = message

        when (type) {
            "OK" -> {
                btnYes?.text = "OK"
                btnNo?.isVisible = false
                btnYes?.setOnClickListener {
                    startActivity(Intent(this, SelectLevelActivity::class.java))
                    finish()
                }
            }

            "EXIT_GAME", "CONFIRM_EXIT" -> {
                btnYes?.text = "Yes"
                btnNo?.text = "No"
                btnNo?.isVisible = true

                btnYes?.setOnClickListener {
                    val resultIntent = Intent()
                    resultIntent.putExtra("RESPONSE", "YES")
                    setResult(RESULT_OK, resultIntent)

                    val intent = Intent(this, SelectLevelActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }

                btnNo?.setOnClickListener {
                    val resultIntent = Intent()
                    resultIntent.putExtra("RESPONSE", "NO")
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }


            else -> {
                btnYes?.text = "Yes"
                btnNo?.text = "No"
                btnNo?.isVisible = true

                btnYes?.setOnClickListener {
                    FirebaseAuth.getInstance().signOut()
                    (application as MatchItMania).logOut()
                    startActivity(Intent(this, UserActivity::class.java))
                    finish()
                }

                btnNo?.setOnClickListener {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val musicEnabled = (application as MatchItMania).userSettings.music ?: true
        if (musicEnabled) {
            BackgroundMusic.play()
        }
    }

}