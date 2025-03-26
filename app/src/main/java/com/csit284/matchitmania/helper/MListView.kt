package com.csit284.matchitmania.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.R
import userGenerated.UserProfile

class MListView(private val context: Context, private val playerList: List<UserProfile>) : BaseAdapter() {
    override fun getCount(): Int = playerList.size
    override fun getItem(position: Int): Any = playerList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val rank = view.findViewById<TextView>(R.id.tvRank)
        val avatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val name = view.findViewById<TextView>(R.id.tvUsername)
        val lvl = view.findViewById<TextView>(R.id.tvLevel)

        val player = playerList[position]

        rank.text = (position + 1).toString() // Ranks start at 1

        name.text = player.username
        lvl.text = "Level ${player.level}"

        val resourceId = context.resources.getIdentifier(
            player.profileImageId, "drawable", context.packageName
        )

        val colorId = context.resources.getIdentifier(
            player.profileColor, "color", context.packageName
        )

        if (resourceId != 0) {
            avatar.setImageResource(resourceId)
        } else {
            avatar.setImageResource(R.drawable.avatar1) // Default image
        }

        if(colorId != 0){
            val color = ContextCompat.getColor(context, colorId)
            avatar.setBackgroundColor(color)
        }else{
            avatar.setBackgroundColor(colorId)
        }

        return view
    }
}
