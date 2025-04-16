package com.csit284.matchitmania.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.csit284.matchitmania.R
import com.csit284.matchitmania.interfaces.Clickable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import extensions.fieldEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import views.MButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var etUser: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnRegister: MButton
    private lateinit var pbRegister: ProgressBar
    private lateinit var mAuth: FirebaseAuth
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
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        mAuth = FirebaseAuth.getInstance()
        etUser = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etPass = view.findViewById(R.id.etPassword)
        etConfirmPass = view.findViewById(R.id.etCPassword)
        btnRegister = view.findViewById(R.id.btnDone)
        pbRegister = view.findViewById(R.id.pbRegister)
        view.findViewById<MButton>(R.id.btnDone).setOnClickListener {
            handleRegister()
        }

        view.findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            clickListener?.onClicked("login")
        }
    }

    private fun handleRegister() {
        pbRegister.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        val user = etUser.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPass.text.toString()
        val cPassword = etConfirmPass.text.toString()

        when {
            user.fieldEmpty(etUser, "Please input username") || email.fieldEmpty(etEmail, "Please input email") || password.fieldEmpty(etPass, "Please input password") -> {
                pbRegister.visibility = View.GONE
                btnRegister.isEnabled = true
                return
            }
            password != cPassword -> {
                etConfirmPass.error = "Passwords don't match"
                pbRegister.visibility = View.GONE
                btnRegister.isEnabled = true
                return
            }
        }

        // Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser?.uid
                    if (userId != null) {
                        saveUserData(userId, user, email)
                    } else {
                        handleRegistrationFailure("User registration failed")
                    }
                } else {
                    handleRegistrationFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun saveUserData(userId: String, username: String, email: String) {
        lifecycleScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(
                        mapOf(
                            "username" to username,
                            "email" to email,
                            "profileImageId" to "avatar1",
                            "profileColor" to "MBlue",
                            "level" to 1
                        )
                    ).await()
                Toast.makeText(requireContext(), "Player $username registered!", Toast.LENGTH_SHORT).show()
                clickListener?.onClicked("done")
            } catch (e: Exception) {
                Log.e("REGISTER", "Failed to save user data: ${e.message}", e)
                handleRegistrationFailure("Failed to save user data")
            }
        }
    }

    private fun handleRegistrationFailure(errorMessage: String) {
        pbRegister.visibility = View.GONE
        btnRegister.isEnabled = true
        Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
    }
}