package pl.sjanda.jpamietnik.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class FirebaseManager {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val entriesCollection = db.collection("diary_entries")
    private val storageRef = storage.reference

    suspend fun saveEntry(
        entry: DiaryEntry,
        imageUri: Uri? = null,
        audioUri: Uri? = null
    ): Result<String> {
        return try {
            val docRef = if (entry.id.isEmpty()) {
                entriesCollection.document()
            } else {
                entriesCollection.document(entry.id)
            }

            val entryId = docRef.id
            var imageUrl: String? = entry.imageUrl
            var audioUrl: String? = entry.audioUrl

            if (imageUri != null && (entry.imageUrl == null || entry.imageUrl != imageUri.toString())) {
                val imageResult = uploadImage(imageUri, entryId)
                if (imageResult.isSuccess) {
                    imageUrl = imageResult.getOrNull()
                } else {
                    return Result.failure(
                        imageResult.exceptionOrNull() ?: Exception("Błąd przesyłania obrazu")
                    )
                }
            }

            if (audioUri != null && (entry.audioUrl == null || entry.audioUrl != audioUri.toString())) {
                val audioResult = uploadAudio(audioUri, entryId)
                if (audioResult.isSuccess) {
                    audioUrl = audioResult.getOrNull()
                } else {
                    return Result.failure(
                        audioResult.exceptionOrNull() ?: Exception("Błąd przesyłania audio")
                    )
                }
            }

            val entryToSave = entry.copy(
                id = entryId,
                imageUrl = imageUrl,
                audioUrl = audioUrl,
                updatedAt = Date()
            )

            docRef.set(entryToSave).await()
            Result.success(entryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEntry(
        entry: DiaryEntry,
        imageUri: Uri? = null,
        audioUri: Uri? = null
    ): Result<String> {
        return try {
            val entryId = entry.id
            var imageUrl: String? = entry.imageUrl
            var audioUrl: String? = entry.audioUrl

            if (imageUri != null && (entry.imageUrl == null || entry.imageUrl != imageUri.toString())) {
                if (entry.imageUrl != null && entry.imageUrl.isNotEmpty()) {
                    deleteImageFromStorage(entry.imageUrl)
                }

                val imageResult = uploadImage(imageUri, entryId)
                if (imageResult.isSuccess) {
                    imageUrl = imageResult.getOrNull()
                } else {
                    return Result.failure(
                        imageResult.exceptionOrNull() ?: Exception("Błąd przesyłania obrazu")
                    )
                }
            }

            if (audioUri != null && (entry.audioUrl == null || entry.audioUrl != audioUri.toString())) {
                if (entry.audioUrl != null && entry.audioUrl.isNotEmpty()) {
                    deleteAudioFromStorage(entry.audioUrl)
                }

                val audioResult = uploadAudio(audioUri, entryId)
                if (audioResult.isSuccess) {
                    audioUrl = audioResult.getOrNull()
                } else {
                    return Result.failure(
                        audioResult.exceptionOrNull() ?: Exception("Błąd przesyłania audio")
                    )
                }
            }

            val entryToUpdate = entry.copy(
                imageUrl = imageUrl,
                audioUrl = audioUrl,
                updatedAt = Date()
            )

            entriesCollection.document(entryId).set(entryToUpdate).await()
            Result.success(entryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEntry(entryId: String): Result<DiaryEntry?> {
        return try {
            val doc = entriesCollection.document(entryId).get().await()
            val entry = doc.toObject(DiaryEntry::class.java)
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllEntries(): Result<List<DiaryEntry>> {
        return try {
            val snapshot = entriesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val entries = snapshot.documents.mapNotNull { doc ->
                doc.toObject(DiaryEntry::class.java)
            }
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEntry(entryId: String): Result<Unit> {
        return try {
            val entryResult = getEntry(entryId)
            if (entryResult.isSuccess) {
                val entry = entryResult.getOrNull()
                entry?.let {
                    if (it.imageUrl != null && it.imageUrl.isNotEmpty()) {
                        deleteImageFromStorage(it.imageUrl)
                    }
                    if (it.audioUrl != null && it.audioUrl.isNotEmpty()) {
                        deleteAudioFromStorage(it.audioUrl)
                    }
                }
            }

            entriesCollection.document(entryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImage(imageUri: Uri, entryId: String): Result<String> {
        return try {
            val imageRef = storageRef.child("images/$entryId/${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadAudio(audioUri: Uri, entryId: String): Result<String> {
        return try {
            val audioRef = storageRef.child("audio/$entryId/${UUID.randomUUID()}.mp3")
            val uploadTask = audioRef.putFile(audioUri).await()
            val downloadUrl = audioRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun deleteImageFromStorage(imageUrl: String) {
        try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
        }
    }

    private suspend fun deleteAudioFromStorage(audioUrl: String) {
        try {
            val audioRef = storage.getReferenceFromUrl(audioUrl)
            audioRef.delete().await()
        } catch (e: Exception) {
        }
    }
}