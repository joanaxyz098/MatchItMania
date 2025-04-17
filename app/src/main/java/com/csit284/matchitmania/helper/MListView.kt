package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.app.MatchItMania
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import userGenerated.UserProfile

class MListView(private val activity: Activity, private val originalUsers: List<UserProfile>) : BaseAdapter() {

    private var filteredUsers: List<UserProfile> = originalUsers.toList()
    private var expandedPosition = -1

    override fun getCount(): Int = filteredUsers.size

    override fun getItem(position: Int): Any = filteredUsers[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: activity.layoutInflater.inflate(R.layout.list_item, parent, false)
        val user = filteredUsers[position]

        // Get references to views
        val tvRank = view.findViewById<TextView>(R.id.tvRank)
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvLevel = view.findViewById<TextView>(R.id.tvLevel)
        val btnAddRemoveFriend = view.findViewById<ImageButton>(R.id.btnAddRemoveFriend)
        val btnUserInfo = view.findViewById<ImageButton>(R.id.btnUserInfo)

        // Set data
        tvRank.text = (position + 1).toString()
        val username = (activity.applicationContext as MatchItMania).userProfile.username
        tvUsername.text = if(user.username == username){
            "You"
        }else{
            user.username
        }
        tvLevel.text = "Level ${user.level}"

        val resourceId = activity.resources.getIdentifier(
            user.profileImageId, "drawable", activity.packageName
        )

        val colorId = activity.resources.getIdentifier(
            user.profileColor, "color", activity.packageName
        )

        if (resourceId != 0) {
            ivAvatar.setImageResource(resourceId)
        } else {
            ivAvatar.setImageResource(R.drawable.avatar1) // Default image
        }

        if(colorId != 0){
            val color = ContextCompat.getColor(activity, colorId)
            ivAvatar.setBackgroundColor(color)
        }else{
            ivAvatar.setBackgroundColor(colorId)
        }

        val app = (activity.applicationContext as MatchItMania)
        val isFriend = app.userProfile.friends.contains(user.username)

        val isCurrentUser = user.username == app.userProfile.username

        btnAddRemoveFriend.visibility = if (!isCurrentUser && position == expandedPosition) View.VISIBLE else View.GONE
        btnUserInfo.visibility = if (!isCurrentUser && position == expandedPosition) View.VISIBLE else View.GONE

        btnAddRemoveFriend.setBackgroundResource(
            if (isFriend) R.drawable.remove else R.drawable.add
        )

        view.setOnClickListener {
            if (user.username != (activity.applicationContext as MatchItMania).userProfile.username) {
                expandedPosition = if (position == expandedPosition) -1 else position
                notifyDataSetChanged()
            }
        }


        btnAddRemoveFriend.setOnClickListener {
            val app = (activity.applicationContext as MatchItMania)

            if (!app.userProfile.friends.contains(user.username)) {
                // Add friend
                btnAddRemoveFriend.background = ContextCompat.getDrawable(activity, R.drawable.remove)
                app.userProfile.friends.add(user.username)
                app.saveUserData(activity, app.userProfile)
                app.addFriend(user.username)
                Toast.makeText(activity, "Friend added", Toast.LENGTH_SHORT).show()
            } else {
                btnAddRemoveFriend.background = ContextCompat.getDrawable(activity, R.drawable.add)
                app.removeFriend(activity, user.username)
                Toast.makeText(activity, "Friend removed", Toast.LENGTH_SHORT).show()
            }
            notifyDataSetChanged()
        }

        btnUserInfo.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra("USERPROFILE", user)
            activity.startActivity(intent)
        }

        return view
    }
    fun filter(query: String) {
        val lowerQuery = query.lowercase().trim()
        filteredUsers = if (lowerQuery.isEmpty()) {
            originalUsers
        } else {
            originalUsers.filter { it.username.lowercase().contains(lowerQuery) }
        }
        expandedPosition = -1 // collapse any expanded items
        notifyDataSetChanged()
    }
}