package userGenerated

import java.io.Serializable

data class UserProfile(
    var username: String = "",
    var email: String = "",
    var profileImageId: String = "",
    var profileColor: String = "",
    var level: Int = 0,
    var highestScore: Int = 0,
    var fastestClear: Long = 0,
    var maxCombo: Int = 0,
    var losses: Int = 0,
    var friends: MutableList<String> = mutableListOf(),
    var sentReq: MutableList<String> = mutableListOf(),
    var recReq: MutableList<String> = mutableListOf()
) : Serializable {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "email" to email,
            "profileImageId" to profileImageId,
            "profileColor" to profileColor,
            "level" to level,
            "highestScore" to highestScore,
            "fastestClear" to fastestClear,
            "maxCombo" to maxCombo,
            "losses" to losses,
            "friends" to friends,
            "sentReq" to sentReq,
            "recReq" to recReq
        )
    }
}
