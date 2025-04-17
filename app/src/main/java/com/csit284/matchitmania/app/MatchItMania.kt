package com.csit284.matchitmania.app

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import userGenerated.UserProfile
import userGenerated.UserSettings

class MatchItMania : Application() {
    var userProfile: UserProfile = UserProfile()
    var userSettings: UserSettings = UserSettings()
    val activeFrames: SparseArray<SparseArray<Drawable>> = SparseArray()
    val disableFrames: SparseArray<SparseArray<Drawable>> = SparseArray()
    var users  = mutableListOf<UserProfile>()
    var friends = mutableListOf<UserProfile>()
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    // Changed to public for easier debugging
    var profileLoaded = false
    var settingsLoaded = false
    var spritesLoaded = false
    var usersLoaded = false

    // Callback for notifying activities when initialization is complete
    private var onInitCompleteCallback: (() -> Unit)? = null

    fun setOnInitCompleteListener(callback: () -> Unit) {
        if (isInitializationComplete()) {
            // If already initialized, call callback immediately
            callback()
        } else {
            // Store callback for later
            onInitCompleteCallback = callback
        }
    }

    fun isInitializationComplete(): Boolean {
        val result = profileLoaded && settingsLoaded && spritesLoaded && usersLoaded
        Log.d("MatchItMania", "Init check - Profile: $profileLoaded, " +
                "Settings: $settingsLoaded, Sprites: $spritesLoaded, Complete: $result")
        return result
    }

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            // Start loading all required data
            fetchSprites()

            val currentUser = auth.currentUser
            if (currentUser != null) {
                fetchUserProfile(currentUser.uid)
                fetchUserSettings(currentUser.uid)
                fetchUsers(currentUser.uid)
            } else {
                // No user logged in, mark profile and settings as loaded
                profileLoaded = true
                settingsLoaded = true
                usersLoaded = true
                checkInitializationComplete()
            }
        } catch (e: Exception) {
            Log.e("MatchItMania", "Initialization error", e)
            // Mark all as loaded to prevent hanging
            profileLoaded = true
            settingsLoaded = true
            spritesLoaded = true
            usersLoaded = true
            checkInitializationComplete()
        }
    }

    private fun fetchSprites() {
        // Load active frames
        for (i in 0..6) {
            val rowArray = SparseArray<Drawable>()
            for (j in 0..7) {
                val resName = "blocks_${i}_$j"
                val resId = resources.getIdentifier(resName, "drawable", packageName)
                if (resId != 0) {
                    val drawable = ContextCompat.getDrawable(this, resId)
                    if (drawable != null) {
                        rowArray.put(j, drawable)
                    } else {
                        Log.w("fetchSprites", "Drawable not found for $resName")
                    }
                } else {
                    Log.w("fetchSprites", "Resource ID not found for $resName")
                }
            }
            activeFrames.put(i, rowArray)
        }

        // Load disabled frames
        for (i in 0..6) {
            val rowArray = SparseArray<Drawable>()
            for (j in 0..8) {
                val resName = "blocksk_${i}_$j"
                val resId = resources.getIdentifier(resName, "drawable", packageName)
                if (resId != 0) {
                    val drawable = ContextCompat.getDrawable(this, resId)
                    if (drawable != null) {
                        rowArray.put(j, drawable)
                    } else {
                        Log.w("fetchSprites", "Drawable not found for $resName")
                    }
                } else {
                    Log.w("fetchSprites", "Resource ID not found for $resName")
                }
            }
            disableFrames.put(i, rowArray)
        }

        spritesLoaded = true
        checkInitializationComplete()
    }

    private fun fetchUsers(uid: String) {
        db.collection("users").limit(100).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageId = document.getString("profileImageId") ?: "avatar1"
                    val profileColor = document.getString("profileColor") ?: "MBlue"
                    val level = document.getLong("level")?.toInt() ?: 1
                    val highestScore = document.getLong("highestScore")?.toInt() ?: 0
                    val fastestClear = document.getLong("fastestClear") ?: Long.MAX_VALUE
                    val maxCombo = document.getLong("maxCombo")?.toInt() ?: 0
                    val mistakes = document.getLong("mistakes")?.toInt() ?: 0
                    users.add(UserProfile(username, "", profileImageId, profileColor, level, highestScore, fastestClear, maxCombo, mistakes))
                }
                users.sortByDescending { it.level }

                Log.d("Firestore", "Users fetched successfully: ${users.size} users (limited to 100)")
                usersLoaded = true
                checkInitializationComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching users", e)
                usersLoaded = true // Mark as loaded even if failed
                checkInitializationComplete()
            }
    }

    private fun fetchUserProfile(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userProfile.apply {
                        email = document.getString("email") ?: ""
                        username = document.getString("username") ?: ""
                        profileImageId = document.getString("profileImageId") ?: ""
                        profileColor = document.getString("profileColor") ?: ""
                        level = document.getLong("level")?.toInt() ?: 1
                        highestScore = document.getLong("highestScore")?.toInt() ?: 0
                        fastestClear = document.getLong("fastestClear") ?: Long.MAX_VALUE
                        maxCombo = document.getLong("maxCombo")?.toInt() ?: 0
                        losses = document.getLong("losses")?.toInt() ?: 0
                        friends = document.get("friends") as? MutableList<String> ?: mutableListOf()
                    }

                    // Fetch friends data and add to friends list
                    fetchFriends(userProfile.friends)

                    Log.d("Firestore", "User Profile fetched successfully: ${userProfile.username}")
                } else {
                    Log.d("Firestore", "User profile does not exist")
                }
                profileLoaded = true
                checkInitializationComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user profile", e)
                profileLoaded = true // Mark as loaded even if failed
                checkInitializationComplete()
            }
    }

    fun logOut(){
        userProfile = UserProfile()
        userSettings = UserSettings()
    }

    fun refreshAllData() {
        // Reset all loaded flags
        profileLoaded = false
        settingsLoaded = false
        usersLoaded = false

        // Clear existing data
        users.clear()
        friends.clear()

        // Re-fetch everything
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserProfile(currentUser.uid)
            fetchUserSettings(currentUser.uid)
            fetchUsers(currentUser.uid)
        }
    }

    private fun fetchFriends(usernames: List<String>) {
        friends.clear()
        friends.add(userProfile)  // Add current user first

        if (usernames.isEmpty()) {
            Log.d("Firestore", "No friends to fetch")
            return
        }

        for (username in usernames) {
            addFriend(username)
        }

        friends.sortByDescending { it.level }
    }


    fun addFriend(username: String) {
        // Check if friend already exists
        if (friends.any { it.username == username }) {
            return
        }

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val friendProfile = UserProfile(
                            username = document.getString("username") ?: "Unknown",
                            email = document.getString("email") ?: "",
                            profileImageId = document.getString("profileImageId") ?: "avatar1",
                            profileColor = document.getString("profileColor") ?: "MBlue",
                            level = document.getLong("level")?.toInt() ?: 1,
                            highestScore = document.getLong("highestScore")?.toInt() ?: 0,
                            fastestClear = document.getLong("fastestClear") ?: Long.MAX_VALUE,
                            maxCombo = document.getLong("maxCombo")?.toInt() ?: 0,
                            losses = document.getLong("mistakes")?.toInt() ?: 0
                        )
                        friends.add(friendProfile)
                        // Sort the list immediately
                        friends.sortByDescending { it.level }
                        Log.d("Firestore", "Added and sorted friend: ${friendProfile.username}")
                    }
                } else {
                    Log.d("Firestore", "No user found with username: $username")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user with username: $username", e)
            }
    }

    // Add to MatchItMania class
    fun removeFriend(context: Context, username: String) {
        // Remove from friends list in memory
        val friendToRemove = friends.find { it.username == username }
        if (friendToRemove != null) {
            friends.remove(friendToRemove)
            Log.d("Firestore", "Removed friend locally: ${username}")
        }

        // Remove from userProfile.friends list
        userProfile.friends.remove(username)

        // Save to Firestore
        saveUserData(context, userProfile)

        // Log the current state for debugging
        Log.d("Firestore", "After removal: ${userProfile.friends.size} friends in profile, ${friends.size} friends in list")
    }

    private fun fetchUserSettings(uid: String) {
        db.collection("settings").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userSettings.apply {
                        music = document.getBoolean("music") ?: true
                        sound = document.getBoolean("sound") ?: true
                        vibration = document.getBoolean("vibration") ?: true
                    }
                    Log.d("Firestore", "User Settings fetched successfully")
                } else {
                    Log.d("Firestore", "User settings do not exist, using defaults")
                }
                settingsLoaded = true
                checkInitializationComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user settings", e)
                settingsLoaded = true // Mark as loaded even if failed
                checkInitializationComplete()
            }
    }

    private fun checkInitializationComplete() {
        if (isInitializationComplete()) {
            Log.d("MatchItMania", "All initialization complete")
            // Notify callback if registered
            onInitCompleteCallback?.invoke()
            onInitCompleteCallback = null // Clear callback after use
        }
    }
    // In MatchItMania class, update the saveUserData function
    fun saveUserData(activity: Context, userProfile: UserProfile, onComplete: () -> Unit = {}) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Log the friends list size before saving to help debug
            Log.d("Firestore", "Saving user profile with ${userProfile.friends.size} friends")

            val userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)

            userRef.set(userProfile.toMap())
                .addOnSuccessListener {
                    Log.d("Firestore", "User data saved successfully")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error saving user data: ${e.message}", e)
                    Toast.makeText(activity, "Failed to save profile changes", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
        } else {
            Log.e("MListView", "Cannot update profile: User not authenticated")
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }
}