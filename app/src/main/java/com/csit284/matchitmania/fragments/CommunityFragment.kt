package com.csit284.matchitmania.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SwitchCompat
import com.csit284.matchitmania.AdapterType
import com.csit284.matchitmania.MListView
import com.csit284.matchitmania.R
import com.csit284.matchitmania.app.MatchItMania
import com.csit284.matchitmania.interfaces.Clickable
import views.MButton

class CommunityFragment : Fragment() {

    private var clickListener: Clickable? = null

    // Data refresh listener interface
    interface DataRefreshListener {
        fun onDataRefresh()
    }

    // Class-level adapter variables
    private lateinit var findAdapter: MListView
    private lateinit var friendsAdapter: MListView
    private lateinit var receivedRequestsAdapter: MListView
    private lateinit var sentRequestsAdapter: MListView
    private var dataRefreshListener: DataRefreshListener? = null
    private lateinit var lvCommunity: ListView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Clickable) {
            clickListener = context
        }

        // Set up data refresh listener
        dataRefreshListener = object : DataRefreshListener {
            override fun onDataRefresh() {
                activity?.runOnUiThread {
                    refreshAdapters()
                }
            }
        }
        (context.applicationContext as? MatchItMania)?.setDataRefreshListener(dataRefreshListener)
    }

    override fun onDetach() {
        super.onDetach()
        clickListener = null
        (context?.applicationContext as? MatchItMania)?.setDataRefreshListener(null)
        dataRefreshListener = null
    }

    override fun onResume() {
        super.onResume()
        refreshAdapters()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        setupViews(view)
        return view
    }

    private fun refreshAdapters() {
        if (!this::lvCommunity.isInitialized || !isAdded) return

        val app = context?.applicationContext as? MatchItMania ?: return

        // Update adapters with fresh data
        findAdapter = MListView(requireActivity(), app.users, AdapterType.FIND)
        friendsAdapter = MListView(requireActivity(), app.friends, AdapterType.FRIENDS)
        receivedRequestsAdapter = MListView(requireActivity(), app.req, AdapterType.RECEIVED)
        sentRequestsAdapter = MListView(requireActivity(), app.sent, AdapterType.SENT)

        // Update the current adapter in the ListView based on current adapter type
        val currentAdapter = lvCommunity.adapter

        when (currentAdapter) {
            is MListView -> {
                when (currentAdapter.adapterType) {
                    AdapterType.FIND -> lvCommunity.adapter = findAdapter
                    AdapterType.FRIENDS -> lvCommunity.adapter = friendsAdapter
                    AdapterType.RECEIVED -> lvCommunity.adapter = receivedRequestsAdapter
                    AdapterType.SENT -> lvCommunity.adapter = sentRequestsAdapter
                    else -> lvCommunity.adapter = findAdapter
                }
            }
            else -> lvCommunity.adapter = findAdapter
        }
    }

    private fun setupViews(view: View) {
        lvCommunity = view.findViewById(R.id.lvboard)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        etSearch.isEnabled = true
        val scToggle = view.findViewById<SwitchCompat>(R.id.scToggle)

        val app = context?.applicationContext as MatchItMania

        // Initialize adapters
        findAdapter = MListView(requireActivity(), app.users, AdapterType.FIND)
        friendsAdapter = MListView(requireActivity(), app.friends, AdapterType.FRIENDS)
        receivedRequestsAdapter = MListView(requireActivity(), app.req, AdapterType.RECEIVED)
        sentRequestsAdapter = MListView(requireActivity(), app.sent, AdapterType.SENT)

        lvCommunity.adapter = findAdapter

        // Set button click handlers for the Global/Friends tabs
        view.findViewById<MButton>(R.id.btnFind).setOnClickListener {
            lvCommunity.adapter = findAdapter
            scToggle.visibility = View.GONE
            etSearch.visibility = View.VISIBLE
        }

        view.findViewById<MButton>(R.id.btnFriends).setOnClickListener {
            lvCommunity.adapter = friendsAdapter
            scToggle.visibility = View.GONE
            etSearch.visibility = View.VISIBLE
        }

        view.findViewById<MButton>(R.id.btnRequest).setOnClickListener {
            lvCommunity.adapter = receivedRequestsAdapter
            scToggle.visibility = View.VISIBLE
            etSearch.visibility = View.GONE
        }

        scToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                scToggle.text = "Received"
                lvCommunity.adapter = receivedRequestsAdapter
            } else {
                scToggle.text = "Sent"
                lvCommunity.adapter = sentRequestsAdapter
            }
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                (lvCommunity.adapter as? MListView)?.filter(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
}