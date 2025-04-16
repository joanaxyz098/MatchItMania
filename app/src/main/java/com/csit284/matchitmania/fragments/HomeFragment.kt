package com.csit284.matchitmania.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.csit284.matchitmania.ProfileActivity
import com.csit284.matchitmania.R
import com.csit284.matchitmania.SelectLevelActivity
import com.csit284.matchitmania.SettingsActivity
import com.csit284.matchitmania.app.MatchItMania
import com.csit284.matchitmania.interfaces.Clickable
import music.BackgroundMusic
import userGenerated.UserProfile
import userGenerated.UserSettings
import views.MButton
class HomeFragment : Fragment() {
    private var userProfile: UserProfile?= null
    private var userSettings: UserSettings?= null
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
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setUpViews(view)
        return view
    }

    private fun setUpViews(view: View){
        loadUserData(view)
        view.findViewById<MButton>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(requireActivity(), SettingsActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<MButton>(R.id.btnProfile).setOnClickListener {
            val intent = Intent(requireActivity(), ProfileActivity::class.java)
            if(!userProfile?.username.isNullOrEmpty()) {
                startActivity(intent)
                Log.i("TASK", "userProfile has been passed to Profile Activity: ")
                Log.i("TASK", "UserProfile ${userProfile?.username}")
            }else Log.i("TASK", "user profile is empty")
        }
        view.findViewById<MButton>(R.id.btnPlay)?.setOnClickListener{

            clickListener?.onClicked("play")
            val intent = Intent(requireActivity(), SelectLevelActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData(view: View) {
        val matchItMania = requireActivity().application as MatchItMania
        userProfile = matchItMania.userProfile
        userSettings = matchItMania.userSettings

        // Handle music settings
        if (userSettings?.music == true) {
            BackgroundMusic.play()
        } else {
            BackgroundMusic.pause()
        }

        // Handle profile button update
        val profileImageId = userProfile?.profileImageId
        val profileColor = userProfile?.profileColor

        if (!profileImageId.isNullOrEmpty()) {
            try {
                val drawableId = resources.getIdentifier(profileImageId, "drawable", requireActivity().packageName)
                val colorId = resources.getIdentifier(profileColor, "color", requireActivity().packageName)

                Log.d("TASK", "Profile Image ID: $profileImageId")
                Log.d("TASK", "Profile Color: $profileColor")
                Log.d("TASK", "Drawable ID: $drawableId")
                Log.d("TASK", "Color ID: $colorId")

                if (drawableId != 0) {
                    val profileButton = view.findViewById<MButton>(R.id.btnProfile)

                    // Set image background
                    val drawable = ContextCompat.getDrawable(requireActivity(), drawableId)
                    profileButton.imageBackground = drawable

                    // Set background color
                    if (colorId != 0) {
                        val color = ContextCompat.getColor(requireActivity(), colorId)
                        profileButton.backColor = color
                    }

                    Log.i("TASK", "btnProfile updated successfully")
                } else {
                    Log.e("TASK", "Drawable not found: $profileImageId")
                }
            } catch (e: Exception) {
                Log.e("TASK", "Error updating profile button", e)
            }
        } else {
            Log.e("TASK", "profileImageId is null or empty")
        }
    }
}