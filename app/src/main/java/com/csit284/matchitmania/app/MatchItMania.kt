package com.csit284.matchitmania.app

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.csit284.matchitmania.fragments.CommunityFragment
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
    var sent = mutableListOf<UserProfile>()
    var req = mutableListOf<UserProfile>()
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private var dataRefreshListener: CommunityFragment.DataRefreshListener? = null

    // Changed to public for easier debugging
    var profileLoaded = false
    var settingsLoaded = false
    var spritesLoaded = false
    var usersLoaded = false

    // Callback for notifying activities when initialization is complete
    private var onInitCompleteCallback: (() -> Unit)? = null
    fun setDataRefreshListener(listener: CommunityFragment.DataRefreshListener?) {
        dataRefreshListener = listener
    }
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
        FirebaseApp.initializeApp(this)
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
                        sentReq = document.get("sentReq") as? MutableList<String> ?: mutableListOf()
                        recReq = document.get("recREq") as? MutableList<String> ?: mutableListOf()
                    }

                    var completedFetches = 0

                    // Fetch friends data
                    fetchList(friends, userProfile.friends) {
                        completedFetches++
                        if (completedFetches >= 3) {
                            profileLoaded = true
                            checkInitializationComplete()
                        }
                    }


                    Log.d("Match it mania", "received requests: ${userProfile.recReq}")
                    // Fetch received request data
                    fetchList(req, userProfile.recReq) {
                        completedFetches++
                        if (completedFetches >= 3) {
                            profileLoaded = true
                            checkInitializationComplete()
                        }
                    }

                    // Fetch sent request data
                    fetchList(sent, userProfile.sentReq) {
                        completedFetches++
                        if (completedFetches >= 3) {
                            profileLoaded = true
                            checkInitializationComplete()
                        }
                    }
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

    private fun fetchList(target: MutableList<UserProfile>, usernames: List<String>, onComplete: () -> Unit = {}) {
        target.clear()

        if (usernames.isEmpty()) {
            Log.d("Firestore", "No users to fetch")
            onComplete()
            return
        }

        // Counter to track when all fetches are complete
        var fetchCount = 0
        val totalToFetch = usernames.size

        for (username in usernames) {
            findUser(username) { userProfile ->
                if (userProfile.username.isNotEmpty()) {
                    target.add(userProfile)
                    // Sort after each addition
                    target.sortByDescending { it.level }
                }

                // Increment counter and check if we're done
                fetchCount++
                if (fetchCount >= totalToFetch) {
                    Log.d("Firestore", "Completed fetching all ${target.size} profiles")
                    onComplete()
                }
            }
        }
    }

    private fun findUser(username: String, callback: (UserProfile) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val userProfile = UserProfile(
                        username = document.getString("username") ?: "Unknown",
                        email = document.getString("email") ?: "",
                        profileImageId = document.getString("profileImageId") ?: "avatar1",
                        profileColor = document.getString("profileColor") ?: "MBlue",
                        level = document.getLong("level")?.toInt() ?: 1,
                        highestScore = document.getLong("highestScore")?.toInt() ?: 0,
                        fastestClear = document.getLong("fastestClear") ?: Long.MAX_VALUE,
                        maxCombo = document.getLong("maxCombo")?.toInt() ?: 0,
                        losses = document.getLong("mistakes")?.toInt() ?: 0,
                        friends = document.get("friends") as? MutableList<String> ?: mutableListOf(),
                        sentReq = document.get("sentReq") as? MutableList<String> ?: mutableListOf(),
                        recReq = document.get("recReq") as? MutableList<String> ?: mutableListOf()
                    )
                    Log.d("Firestore", "Found User: $username")
                    callback(userProfile)
                } else {
                    Log.d("Firestore", "No user found with username: $username")
                    callback(UserProfile())
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user with username: $username", e)
                callback(UserProfile())
            }
    }
    fun sendRequest(username: String, onComplete: () -> Unit = {}) {
        findUser(username) { friendProfile ->
            friendProfile.recReq.add(userProfile.username)
            userProfile.sentReq.add(username)

            // Add to the sent requests list immediately
            val newSentProfile = friendProfile.copy()
            if (!sent.any { it.username == newSentProfile.username }) {
                sent.add(newSentProfile)
                sent.sortByDescending { it.level }
            }

            syncUserProfiles(userProfile, friendProfile) {
                dataRefreshListener?.onDataRefresh()
                onComplete()
            }
        }
    }

    fun acceptRequest(username: String, onComplete: () -> Unit = {}) {
        findUser(username) { friendProfile ->
            friendProfile.sentReq.remove(userProfile.username)
            userProfile.recReq.remove(username)

            // Update local lists
            userProfile.friends.add(username)
            friendProfile.friends.add(userProfile.username)

            // Add to friends list
            if (!friends.any { it.username == friendProfile.username }) {
                friends.add(friendProfile)
                friends.sortByDescending { it.level }
            }

            // Remove from requests lists
            req.removeIf { it.username == username }

            syncUserProfiles(userProfile, friendProfile) {
                dataRefreshListener?.onDataRefresh()
                onComplete()
            }
        }
    }

    fun declineRequest(username: String, onComplete: () -> Unit = {}) {
        findUser(username) { friendProfile ->
            friendProfile.recReq.remove(userProfile.username)
            userProfile.sentReq.remove(username)

            // Remove from requests list
            req.removeIf { it.username == username }
            sent.removeIf { it.username == username }

            syncUserProfiles(userProfile, friendProfile) {
                dataRefreshListener?.onDataRefresh()
                onComplete()
            }
        }
    }

    fun removeFriend(username: String, onComplete: () -> Unit = {}) {
        findUser(username) { friendProfile ->
            friendProfile.friends.remove(userProfile.username)
            userProfile.friends.remove(username)

            // Remove from friends list
            friends.removeIf { it.username == username }

            syncUserProfiles(userProfile, friendProfile) {
                dataRefreshListener?.onDataRefresh()
                onComplete()
            }
        }
    }


    private fun syncUserProfiles(
        user1: UserProfile,
        user2: UserProfile,
        onComplete: () -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users")
            .whereEqualTo("username", user2.username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val friendDocId = querySnapshot.documents[0].id

                    // Save both user profiles
                    val batch = db.batch()
                    batch.set(db.collection("users").document(currentUid), user1.toMap())
                    batch.set(db.collection("users").document(friendDocId), user2.toMap())

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Firestore", "User profiles synced successfully.")
                            onComplete()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error syncing profiles", e)
                            onComplete()
                        }
                } else {
                    Log.w("Firestore", "Friend document not found for ${user2.username}")
                    onComplete()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch friend document ID", e)
                onComplete()
            }
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