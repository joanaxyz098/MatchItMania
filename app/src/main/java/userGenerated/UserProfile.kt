package userGenerated

import java.io.Serializable

data class UserProfile(
    val username: String = "",
    val email: String = ""
) : Serializable {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "email" to email
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): UserProfile {
            return UserProfile(
                username = map["username"] as? String ?: "",
                email = map["email"] as? String ?: ""
            )
        }
    }
}
