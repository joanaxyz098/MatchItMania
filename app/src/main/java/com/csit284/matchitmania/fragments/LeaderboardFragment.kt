package com.csit284.matchitmania.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.csit284.matchitmania.R
import com.csit284.matchitmania.helper.MListView
import com.csit284.matchitmania.interfaces.Clickable
import com.google.firebase.firestore.FirebaseFirestore
import userGenerated.UserProfile

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class LeaderboardFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View){

        val db = FirebaseFirestore.getInstance()
        val users = mutableListOf<UserProfile>()
        val lvLeaderboard = view.findViewById<ListView>(R.id.lvboard)


        db.collection("users").get()
            .addOnSuccessListener { documents ->
                for ((index, document) in documents.withIndex()) {
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageId = document.getString("profileImageId") ?: "avatar1"
                    val profileColor = document.getString("profileColor") ?: "MBlue"
                    val level = document.getLong("level")?.toInt() ?: 1
                    users.add(UserProfile(username, "", profileImageId, profileColor, level))
                }
                users.sortedByDescending { it.level }
                Log.d("Firestore", "User Profiles: $users")

                val customAdapter = MListView(requireActivity(), users)
                lvLeaderboard.adapter = customAdapter
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting usernames", exception)
            }
    }
}