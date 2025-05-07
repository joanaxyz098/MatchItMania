package com.csit284.matchitmania.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.csit284.matchitmania.R
import com.csit284.matchitmania.app.MatchItMania
import com.csit284.matchitmania.interfaces.Clickable
import com.google.firebase.auth.FirebaseAuth
import extensions.fieldEmpty
import music.BackgroundMusic
import userGenerated.UserProfile
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

        // Handle back press to exit the app
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish() // This will close the app
            }
        })

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

        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)
        val fullText = getString(R.string.registerMsg)
        val spannable = SpannableString(fullText)

        val registerStart = fullText.indexOf("Register")
        val registerEnd = registerStart + "Register".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                clickListener?.onClicked("register")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.blue) // use your defined color
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(clickableSpan, registerStart, registerEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvRegister.text = spannable
        tvRegister.movementMethod = LinkMovementMethod.getInstance()
        tvRegister.highlightColor = Color.TRANSPARENT
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
                    val errorMessage = when (val error = task.exception?.message ?: "") {
                        "The email address is badly formatted." -> "Invalid email format."
                        "There is no user record corresponding to this identifier. The user may have been deleted." -> "User not found."
                        "The password is invalid or the user does not have a password." -> "Incorrect password."
                        else -> "Incorrect username or password."
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
    override fun onResume() {
        super.onResume()
        BackgroundMusic.pause()
    }

}
