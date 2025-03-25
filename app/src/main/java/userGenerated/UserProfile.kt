package userGenerated

import java.io.Serializable

data class UserProfile(
    var username: String = "",
    var email: String = "",
    var profileImageId: String = "",
    var profileColor: String = ""
) : Serializable {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "email" to email,
            "profileImageId" to profileImageId,
            "profileColor" to profileColor
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): UserProfile {
            return UserProfile(
                username = map["username"] as? String ?: "",
                email = map["email"] as? String ?: "",
                profileImageId = map["profileImageId"] as? String?: "",
                profileColor = map["profileColor"] as? String?: ""
            )
        }
    }
}
