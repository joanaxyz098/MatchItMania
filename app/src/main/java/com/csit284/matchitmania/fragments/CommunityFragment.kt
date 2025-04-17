package com.csit284.matchitmania.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.csit284.matchitmania.MListView
import com.csit284.matchitmania.R
import com.csit284.matchitmania.app.MatchItMania
import com.csit284.matchitmania.interfaces.Clickable
import views.MButton

class CommunityFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val users = (context?.applicationContext as MatchItMania).users
        val lvCommunity = view.findViewById<ListView>(R.id.lvboard)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        val findAdapter = MListView(requireActivity(), users)
        lvCommunity.adapter = findAdapter
        // Set button click handlers for the Global/Friends tabs
        view.findViewById<MButton>(R.id.btnFind).setOnClickListener {
            lvCommunity.adapter = findAdapter
        }

        val friends = (context?.applicationContext as MatchItMania).friends
        val friendsAdapter = MListView(requireActivity(), friends)

        view.findViewById<MButton>(R.id.btnFriends).setOnClickListener {
            lvCommunity.adapter = friendsAdapter
        }

        // to do friend requests
        val requests = (context?.applicationContext as MatchItMania).friends
        val requestsAdapter = MListView(requireActivity(), requests)

        view.findViewById<MButton>(R.id.btnFriends).setOnClickListener {
            lvCommunity.adapter = requestsAdapter
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                findAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

    }
}