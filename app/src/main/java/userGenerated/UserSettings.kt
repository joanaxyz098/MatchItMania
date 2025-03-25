package userGenerated

import java.io.Serializable

data class UserSettings(
    var music: Boolean = true,
    val sound: Boolean = true,
    val vibration: Boolean = true
) : Serializable {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "music" to music,
            "sound" to sound,
            "vibration" to vibration
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): UserSettings {
            return UserSettings(
                music = map["music"] as? Boolean ?: true,
                sound = map["sound"] as? Boolean ?: true,
                vibration = map["vibration"] as? Boolean ?: true
            )
        }
    }
}
