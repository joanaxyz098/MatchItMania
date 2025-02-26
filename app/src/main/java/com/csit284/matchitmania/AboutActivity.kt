package com.csit284.matchitmania

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import org.w3c.dom.Text
import views.MButton
import views.MView

class AboutActivity: Activity() {

    var origMvHeight: Int ?= null
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

        Jmv?.post {
            origMvHeight = Jmv?.height ?: 0
            Log.i("TASK", "Jmv height after layout: $origMvHeight")
        }
        Lbtn = findViewById(R.id.vectorArrowL)
        Lmv = findViewById(R.id.mvLuis)
        Ltv = findViewById(R.id.tvLuis)
        Ltv2 = findViewById(R.id.tvLuisE)
        Lll = findViewById(R.id.llLuis)
        setUpJListener()
        setUpLListener()
    }

    private fun toggleViewHeight(
        button: ImageView?,
        viewToExpand: MView?,
        viewToShrink: MView?,
        text1: TextView?,
        text2: TextView?,
        lLayout: LinearLayout?,
        isClicked: Boolean
    ): Boolean {
        val newExpandHeight = if (isClicked) origMvHeight ?: 0 else 1200
        val newShrinkHeight = if (isClicked) origMvHeight ?: 0 else 320

        // Animate expanding view
        ValueAnimator.ofInt(viewToExpand?.height ?: 0, newExpandHeight).apply {
            addUpdateListener { animation ->
                viewToExpand?.layoutParams?.height = animation.animatedValue as Int
                viewToExpand?.requestLayout()
            }
            duration = 500
            start()
        }

        // Animate shrinking view
        ValueAnimator.ofInt(viewToShrink?.height ?: 0, newShrinkHeight).apply {
            addUpdateListener { animation ->
                viewToShrink?.layoutParams?.height = animation.animatedValue as Int
                viewToShrink?.requestLayout()
            }
            duration = 500
            start()
        }

        // Toggle visibility
        val isGone = !isClicked
        text1?.isGone = isGone
        text2?.isGone = isGone
        lLayout?.isGone = !isGone
        button?.isGone = isGone

        return !isClicked
    }

    private fun setUpJListener() {
        Jbtn?.setOnClickListener {
            Jbtn?.animate()?.rotation(if (isJClicked) 0f else 180f)?.setDuration(300)?.start()
            isJClicked = toggleViewHeight(Lbtn, Jmv, Lmv, Ltv, Ltv2, Jll, isJClicked)
        }
    }

    private fun setUpLListener() {
        Lbtn?.setOnClickListener {
            Lbtn?.animate()?.rotation(if (isLClicked) 0f else 180f)?.setDuration(300)?.start()
            isLClicked = toggleViewHeight(Jbtn, Lmv, Jmv, Jtv, Jtv2, Lll, isLClicked)
        }
    }

}