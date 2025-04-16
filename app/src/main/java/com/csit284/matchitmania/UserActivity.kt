package com.csit284.matchitmania

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.csit284.matchitmania.fragments.LoginFragment
import com.csit284.matchitmania.fragments.RegisterFragment
import com.csit284.matchitmania.interfaces.Clickable

class UserActivity : AppCompatActivity(), Clickable {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        val loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fUser, loginFragment)
            .commit()

    }
    override fun onClicked(condition: String) {
        if(condition == "register") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fUser, RegisterFragment())
                .addToBackStack(null)
                .commit()
            findViewById<TextView>(R.id.tvTitle).text = "Register"
        }else if(condition == "login") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fUser, LoginFragment())
                .addToBackStack(null)
                .commit()
            findViewById<TextView>(R.id.tvTitle).text = "Login"
        }else if(condition == "done"){
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, LoadingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}