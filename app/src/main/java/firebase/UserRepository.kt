package firebase

import userGenerated.UserProfile

class UserRepository : FirebaseRepository<UserProfile>("users", UserProfile::class.java) {

    suspend fun loadUserProfile(userId: String): UserProfile? {
        return getDocumentById(userId)
    }
    suspend fun updateUserProfile(userId: String, user: String, email: String) {
        val usersMap = mapOf(
            "username" to user,
            "email" to email
        )
        updateDocument(userId, usersMap)
    }

    suspend fun setUserProfile(userId: String, user: String, email: String) {
        val usersMap = mapOf(
            "username" to user,
            "email" to email
        )
        setDocument(userId, usersMap)
    }
}
