package firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    // Fetch a single document as a Map
    suspend fun getDocumentById(collectionName: String, id: String): Map<String, Any>? {
        return try {
            val document = db.collection(collectionName).document(id).get().await()
            document.data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fetch a specific field from a document
    suspend fun getPartialDocument(collectionName: String, id: String, field: String): Any? {
        return try {
            val document = db.collection(collectionName).document(id).get().await()
            document.get(field)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fetch all documents as a List of Maps
    suspend fun getAllDocuments(collectionName: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection(collectionName).get().await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Add a new document with auto-generated ID
    suspend fun addDocument(collectionName: String, data: Map<String, Any>): String? {
        return try {
            val documentRef = db.collection(collectionName).add(data).await()
            documentRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Update an existing document by ID
    suspend fun updateDocument(collectionName: String, id: String, data: Map<String, Any>) {
        try {
            db.collection(collectionName).document(id).update(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Set (overwrite) a document
    suspend fun setDocument(collectionName: String, id: String, data: Map<String, Any>) {
        try {
            db.collection(collectionName).document(id).set(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Delete a document by ID
    suspend fun deleteDocument(collectionName: String, id: String) {
        try {
            db.collection(collectionName).document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
