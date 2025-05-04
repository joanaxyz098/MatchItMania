package com.csit284.matchitmania

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isGone
import com.csit284.matchitmania.app.MatchItMania
import music.BackgroundMusic
import views.MButton
import views.MView

class AboutActivity: Activity() {

    var Jbtn: ImageView ?= null
    var Jmv: ConstraintLayout ?= null
    var Jtv: TextView ?= null
    var Jtv2: TextView ?= null
    var Jll: LinearLayout ?= null
    var isJClicked: Boolean = false

    var Lbtn: ImageView ?= null
    var Lmv: ConstraintLayout ?= null
    var Ltv: TextView ?= null
    var Ltv2: TextView ?= null
    var Lll: LinearLayout ?= null
    var isLClicked: Boolean = false

    var glH55: Guideline ?= null
    var glH04J: Guideline ?= null
    var glH04L: Guideline ?= null
    var glH4J: Guideline ?= null
    var glH4L: Guideline ?= null
    var glH45J: Guideline ?= null
    var glH45L: Guideline ?= null
    var glH59J: Guideline ?= null
    var glH59L: Guideline ?= null
    var glH75J: Guideline ?= null
    var glH75L: Guideline ?= null
    var glH77J: Guideline ?= null
    var glH77L: Guideline ?= null
    var glH96J: Guideline ?= null
    var glH96L: Guideline ?= null
    var glListJ: ArrayList<Guideline?> ?= null
    var glListL: ArrayList<Guideline?> ?= null
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

        glH55 = findViewById(R.id.glH55)

         glH04J  = findViewById(R.id.glH04J)
         glH04L  = findViewById(R.id.glH04L)
         glH4J  = findViewById(R.id.glH4J)
         glH4L  = findViewById(R.id.glH4L)
         glH45J  = findViewById(R.id.glH45J)
         glH45L  = findViewById(R.id.glH45L)
         glH59J  = findViewById(R.id.glH59J)
         glH59L  = findViewById(R.id.glH59L)
         glH75J  = findViewById(R.id.glH75J)
         glH75L  = findViewById(R.id.glH75L)
         glH77J  = findViewById(R.id.glH77J)
         glH77L  = findViewById(R.id.glH77L)
         glH96J  = findViewById(R.id.glH96J)
         glH96L  = findViewById(R.id.glH96L)
         glListL = arrayListOf(glH04L, glH4L, glH45L, glH59L,
            glH75L)
        glListJ = arrayListOf(glH04J, glH4J, glH45J, glH59J,
            glH75J)

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
        targetPercent: Float,
        targetList: ArrayList<Guideline?>?,
        otherList: ArrayList<Guideline?>?,
        glArrowUp: Guideline?,
        glArrowDown: Guideline?
    ): Boolean {
        // Current guideline percent
        val currentPercent = if (glH55 != null) {
            val params = glH55?.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params?.guidePercent ?: 0.55f
        } else {
            0.55f
        }

        // Animate guideline
        animateGuidelinePercent(glH55, currentPercent, if (isClicked) 0.55f else targetPercent)

        targetList?.forEach { gl ->
            gl?.let { guideline ->
                val params = guideline.layoutParams as? ConstraintLayout.LayoutParams
                val currentP = params?.guidePercent ?: 0f
                if(!isClicked)
                animateGuidelinePercent(guideline, currentP, currentP * 0.6f)
                else animateGuidelinePercent(guideline, currentP, currentP / 0.6f)
            }
        }

        otherList?.forEach{ gl->
            gl?.let { guideline ->
                val params = guideline.layoutParams as? ConstraintLayout.LayoutParams
                val currentP = params?.guidePercent ?: 0f
                if(!isClicked)
                    animateGuidelinePercent(guideline, currentP, currentP * 2f)
                else animateGuidelinePercent(guideline, currentP, currentP / 2f)
            }
        }

        val paramsUp = glArrowUp?.layoutParams as? ConstraintLayout.LayoutParams
        val currentPUp = paramsUp?.guidePercent ?: 0f
        val paramsDown = glArrowDown?.layoutParams as? ConstraintLayout.LayoutParams
        val currentPDown = paramsDown?.guidePercent ?: 0f
        if(!isClicked) {
            animateGuidelinePercent(glArrowUp, currentPUp, currentPUp * 1.1f)
            animateGuidelinePercent(glArrowDown, currentPDown, currentPDown * 1f)
        } else{
            animateGuidelinePercent(glArrowUp, currentPUp, currentPUp / 1.1f)
            animateGuidelinePercent(glArrowDown, currentPDown, currentPDown / 1f)
        }
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
            isJClicked = toggleGuidelineAndViews(Lbtn, Ltv, Ltv2, Jll, isJClicked, 0.75f, glListJ, glListL, glH77J, glH96J)
        }
    }

    private fun setUpLListener() {
        Lbtn?.setOnClickListener {
            Lbtn?.animate()?.rotation(if (isLClicked) 0f else 180f)?.setDuration(300)?.start()
            isLClicked = toggleGuidelineAndViews(Jbtn, Jtv, Jtv2, Lll, isLClicked, 0.35f, glListL, glListJ, glH77L, glH96L)
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