package com.csit284.matchitmania.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.csit284.matchitmania.R
import com.csit284.matchitmania.app.MatchItMania
import com.csit284.matchitmania.interfaces.Clickable
import com.google.firebase.auth.FirebaseAuth
import extensions.fieldEmpty
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton

class LoginFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()
    private var username: String? = null
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var progressBar: ProgressBar
    private var clickListener: Clickable? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Clickable) {
            clickListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        clickListener = null
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        setupViews(view)
        return view
    }

    private fun fetchUsername() {
        val app = requireActivity().application as MatchItMania
        username = app.userProfile.username
        Toast.makeText(requireContext(), "Welcome back! $username", Toast.LENGTH_SHORT).show()
    }

    private fun setupViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        progressBar = view.findViewById(R.id.pbRegister)

        view.findViewById<MButton>(R.id.btnDone).setOnClickListener {
            handleLogin()
        }

        view.findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            clickListener?.onClicked("register")
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.fieldEmpty(etEmail, "Please input your email")) return
        if (password.fieldEmpty(etPassword, "Please input your password")) return

        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    fetchUsername()
                    clickListener?.onClicked("doneLogin")
                } else {
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
