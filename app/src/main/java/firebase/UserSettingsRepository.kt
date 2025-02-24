package firebase

import UserSettings
import kotlinx.coroutines.tasks.await

class UserSettingsRepository : FirebaseRepository<UserSettings>("users", UserSettings::class.java) {

    // Load user settings from Firestore
    suspend fun loadUserSettings(userId: String): UserSettings? {
        return getPartialDocument(userId, "settings", UserSettings::class.java)
    }

    // Update user settings in Firestore
    suspend fun updateUserSettings(userId: String, settings: UserSettings) {
        val settingsMap = mapOf(
            "settings.music" to settings.music,
            "settings.sound" to settings.sound,
            "settings.vibration" to settings.vibration
        )
        updateDocument(userId, settingsMap)
    }
}
