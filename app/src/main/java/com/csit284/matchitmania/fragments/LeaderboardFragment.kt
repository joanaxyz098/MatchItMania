package com.csit284.matchitmania.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.csit284.matchitmania.AdapterType
import com.csit284.matchitmania.MListView
import com.csit284.matchitmania.R
import com.csit284.matchitmania.app.MatchItMania
import com.csit284.matchitmania.interfaces.Clickable
import views.MButton

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

    private fun setupViews(view: View) {
        val app = (context?.applicationContext as MatchItMania)
        val users = app.users
        val lvLeaderboard = view.findViewById<ListView>(R.id.lvboard)

        val globalAdapter = MListView(requireActivity(), users, AdapterType.LEADERBOARD)
        lvLeaderboard.adapter = globalAdapter

        // Set button click handlers for the Global/Friends tabs
        view.findViewById<MButton>(R.id.btnGlobal).setOnClickListener {
            lvLeaderboard.adapter = globalAdapter
        }

        val friends = app.friends.toMutableList().apply {
            if (!any { it.username == app.userProfile.username }) {
                add(app.userProfile)
            }
        }
        val friendsAdapter = MListView(requireActivity(), friends, AdapterType.LEADERBOARD)

        view.findViewById<MButton>(R.id.btnFriends).setOnClickListener {
            lvLeaderboard.adapter = friendsAdapter
        }
    }
}