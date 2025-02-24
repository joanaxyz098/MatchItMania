package firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

open class FirebaseRepository<T>(private val collectionName: String, private val clazz: Class<T>) {
    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection(collectionName)

    // 🔹 Fetch a single document by ID
    suspend fun getDocumentById(id: String): T? {
        return try {
            val document = collectionRef.document(id).get().await()
            document.toObject(clazz)  // Convert Firestore document to an object of type T
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun <R> getPartialDocument(id: String, field: String, clazz: Class<R>): R? {
        return try {
            val document = collectionRef.document(id).get().await()
            val data = document.get(field, clazz)  // Get only the specific field
            data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // 🔹 Fetch all documents in the collection
    suspend fun getAllDocuments(): List<T> {
        return try {
            val snapshot = collectionRef.get().await()
            snapshot.documents.mapNotNull { it.toObject(clazz) }  // Convert Firestore documents to List<T>
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 🔹 Add a new document (Auto-generated ID)
    suspend fun addDocument(data: T): String? {
        return try {
            val documentRef = collectionRef.add(data as Any).await()  // Force type to Any
            documentRef.id  // Return document ID
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    // 🔹 Update an existing document by ID
    suspend fun updateDocument(id: String, data: Map<String, Any>) {
        try {
            collectionRef.document(id).update(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setDocument(id: String, data: Map<String, Any>) {
        try {
            collectionRef.document(id).set(data).await() // Overwrites or creates a new document
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // 🔹 Delete a document by ID
    suspend fun deleteDocument(id: String) {
        try {
            collectionRef.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
