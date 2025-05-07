package com.csit284.matchitmania

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.app.MatchItMania
import userGenerated.UserProfile

enum class AdapterType {
    FIND,
    FRIENDS,
    RECEIVED,
    SENT,
    LEADERBOARD
}

class MListView(
    private val activity: Activity,
    private val originalUsers: List<UserProfile>,
    val adapterType: AdapterType = AdapterType.FIND
) : BaseAdapter() {

    private var filteredUsers: List<UserProfile> = originalUsers
        .filter { user ->
            if (adapterType != AdapterType.LEADERBOARD) {
                user.username != (activity.applicationContext as MatchItMania).userProfile.username
            } else {
                true
            }
        }
        .toList()

    private var expandedPosition = -1

    override fun getCount(): Int = filteredUsers.size

    override fun getItem(position: Int): Any = filteredUsers[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: activity.layoutInflater.inflate(R.layout.list_item, parent, false)
        val user = filteredUsers[position]

        val tvRank = view.findViewById<TextView>(R.id.tvRank)
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvLevel = view.findViewById<TextView>(R.id.tvLevel)
        val btnAddRemoveFriend = view.findViewById<ImageButton>(R.id.btnAddRemoveFriend)
        val btnUserInfo = view.findViewById<ImageButton>(R.id.btnUserInfo)

        val app = activity.applicationContext as MatchItMania
        val currentUser = app.userProfile
        val username = currentUser.username

        tvRank.text = (position + 1).toString()
        tvUsername.text = if (user.username == username) "You" else user.username
        tvLevel.text = "Level ${user.level}"

        val resourceId = activity.resources.getIdentifier(user.profileImageId, "drawable", activity.packageName)
        val colorId = activity.resources.getIdentifier(user.profileColor, "color", activity.packageName)

        ivAvatar.setImageResource(if (resourceId != 0) resourceId else R.drawable.avatar1)
        ivAvatar.setBackgroundColor(
            if (colorId != 0) ContextCompat.getColor(activity, colorId) else colorId
        )

        val isCurrentUser = user.username == username
        val isFriend = currentUser.friends.contains(user.username)
        val isSent = currentUser.sentReq.contains(user.username)
        val isReceived = currentUser.recReq.contains(user.username)

        btnAddRemoveFriend.visibility = when {
            adapterType == AdapterType.LEADERBOARD -> View.GONE
            !isCurrentUser && position == expandedPosition -> View.VISIBLE
            else -> View.GONE
        }

        btnUserInfo.visibility = if (!isCurrentUser && position == expandedPosition) View.VISIBLE else View.GONE

        btnAddRemoveFriend.setBackgroundResource(
            when (adapterType) {
                AdapterType.RECEIVED -> R.drawable.check
                AdapterType.SENT -> R.drawable.remove
                AdapterType.FIND -> {
                    when {
                        isFriend -> R.drawable.remove
                        isSent -> R.drawable.remove
                        isReceived -> R.drawable.check
                        else -> R.drawable.add
                    }
                }
                AdapterType.FRIENDS -> R.drawable.remove
                else -> R.drawable.add
            }
        )

        view.setOnClickListener {
            if (!isCurrentUser) {
                expandedPosition = if (position == expandedPosition) -1 else position
                notifyDataSetChanged()
            }
        }

        btnAddRemoveFriend.setOnClickListener {
            btnAddRemoveFriend.isEnabled = false

            when (adapterType) {
                AdapterType.RECEIVED -> {
                    app.acceptRequest(user.username) {
                        activity.runOnUiThread {
                            notifyDataSetChanged()
                            btnAddRemoveFriend.isEnabled = true
                        }
                    }
                }
                AdapterType.SENT -> {
                    app.declineRequest(user.username) {
                        activity.runOnUiThread {
                            notifyDataSetChanged()
                            btnAddRemoveFriend.isEnabled = true
                        }
                    }
                }
                AdapterType.FIND -> {
                    when {
                        isFriend -> {
                            app.removeFriend(user.username) {
                                activity.runOnUiThread {
                                    notifyDataSetChanged()
                                    btnAddRemoveFriend.isEnabled = true
                                }
                            }
                        }
                        isSent -> {
                            app.declineRequest(user.username) {
                                activity.runOnUiThread {
                                    notifyDataSetChanged()
                                    btnAddRemoveFriend.isEnabled = true
                                }
                            }
                        }
                        isReceived -> {
                            app.acceptRequest(user.username) {
                                activity.runOnUiThread {
                                    notifyDataSetChanged()
                                    btnAddRemoveFriend.isEnabled = true
                                }
                            }
                        }
                        else -> {
                            app.sendRequest(user.username) {
                                activity.runOnUiThread {
                                    notifyDataSetChanged()
                                    btnAddRemoveFriend.isEnabled = true
                                }
                            }
                        }
                    }
                }
                AdapterType.FRIENDS -> {
                    app.removeFriend(user.username) {
                        activity.runOnUiThread {
                            notifyDataSetChanged()
                            btnAddRemoveFriend.isEnabled = true
                        }
                    }
                }
                AdapterType.LEADERBOARD -> {
                    btnAddRemoveFriend.isEnabled = true
                }
            }
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
        expandedPosition = -1
        notifyDataSetChanged()
    }
}
