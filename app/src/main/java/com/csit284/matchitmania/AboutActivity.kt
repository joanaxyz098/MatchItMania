package com.csit284.matchitmania

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isGone
import views.MButton
import views.MView

class AboutActivity: Activity() {

    var Jbtn: ImageView ?= null
    var Jmv: MView ?= null
    var Jtv: TextView ?= null
    var Jtv2: TextView ?= null
    var Jll: LinearLayout ?= null
    var isJClicked: Boolean = false

    var Lbtn: ImageView ?= null
    var Lmv: MView ?= null
    var Ltv: TextView ?= null
    var Ltv2: TextView ?= null
    var Lll: LinearLayout ?= null
    var isLClicked: Boolean = false

    var glH53: Guideline ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val btnExit = findViewById<MButton>(R.id.btnExit)
        btnExit.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        Jbtn = findViewById(R.id.vectorArrowJ)
        Jmv = findViewById(R.id.mvJoana)
        Jtv = findViewById(R.id.tvJoana)
        Jtv2 = findViewById(R.id.tvJoanaE)
        Jll = findViewById(R.id.llJoana)

        Lbtn = findViewById(R.id.vectorArrowL)
        Lmv = findViewById(R.id.mvLuis)
        Ltv = findViewById(R.id.tvLuis)
        Ltv2 = findViewById(R.id.tvLuisE)
        Lll = findViewById(R.id.llLuis)

        glH53 = findViewById(R.id.glH53)

        setUpJListener()
        setUpLListener()
    }

    private fun animateGuidelinePercent(guideline: Guideline?, fromPercent: Float, toPercent: Float) {
        ValueAnimator.ofFloat(fromPercent, toPercent).apply {
            addUpdateListener { animation ->
                val percent = animation.animatedValue as Float
                guideline?.setGuidelinePercent(percent)
            }
            duration = 500
            start()
        }
    }

    private fun toggleGuidelineAndViews(
        button: ImageView?,
        text1: TextView?,
        text2: TextView?,
        layout: LinearLayout?,
        isClicked: Boolean,
        isJ: Boolean
    ): Boolean {
        // Current guideline percent
        val currentPercent = if (glH53 != null) {
            val params = glH53?.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params?.guidePercent ?: 0.53f
        } else {
            0.53f
        }

        // Target guideline percent
        val targetPercent = if (isJ) {
            if (isClicked) 0.53f else 0.75f
        } else {
            if (isClicked) 0.53f else 0.35f
        }

        // Animate guideline
        animateGuidelinePercent(glH53, currentPercent, targetPercent)


        // Toggle visibility
        val isGone = !isClicked
        text1?.isGone = isGone
        text2?.isGone = isGone
        layout?.isGone = !isGone
        button?.isGone = isGone

        return !isClicked
    }

    private fun setUpJListener() {
        Jbtn?.setOnClickListener {
            Jbtn?.animate()?.rotation(if (isJClicked) 0f else 180f)?.setDuration(300)?.start()
            isJClicked = toggleGuidelineAndViews(Lbtn, Ltv, Ltv2, Jll, isJClicked, true)
        }
    }

    private fun setUpLListener() {
        Lbtn?.setOnClickListener {
            Lbtn?.animate()?.rotation(if (isLClicked) 0f else 180f)?.setDuration(300)?.start()
            isLClicked = toggleGuidelineAndViews(Jbtn, Jtv, Jtv2, Lll, isLClicked, false)
        }
    }
}