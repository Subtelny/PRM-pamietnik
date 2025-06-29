package pl.sjanda.jpamietnik.data

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class JournalRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val entriesCollection = firestore.collection("journal_entries")

    suspend fun addJournalEntry(entry: JournalEntry): Result<String> {
        return try {
            val documentReference = entriesCollection.add(entry).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJournalEntry(entry: JournalEntry): Result<Unit> {
        return try {
            entriesCollection.document(entry.id).set(entry).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getJournalEntriesFlow(): Flow<List<JournalEntry>> {
        return entriesCollection
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    document.toObject(JournalEntry::class.java)?.apply { id = document.id }
                }
            }
    }

    suspend fun getJournalEntry(entryId: String): Result<JournalEntry?> {
        return try {
            val documentSnapshot = entriesCollection.document(entryId).get().await()
            val entry = documentSnapshot.toObject(JournalEntry::class.java)
                ?.apply { id = documentSnapshot.id }
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dodaj funkcję do pobierania wszystkich wpisów, jeśli potrzebujesz na liście

    // --- Operacje na Firebase Storage (pliki mediów) ---

    private suspend fun uploadFileToStorage(
        fileUri: Uri,
        path: String,
        entryId: String,
        fileType: String
    ): Result<String> {
        return try {
            val fileName = "${fileType}_${UUID.randomUUID()}" // Unikalna nazwa pliku
            val fileRef = storage.reference.child("$path/$entryId/$fileName")
            fileRef.putFile(fileUri).await() // Poczekaj na zakończenie wysyłania
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhoto(photoUri: Uri, entryId: String): Result<String> {
        return uploadFileToStorage(photoUri, "journal_photos", entryId, "photo")
    }

    suspend fun uploadVoiceRecording(recordingUri: Uri, entryId: String): Result<String> {
        return uploadFileToStorage(recordingUri, "journal_recordings", entryId, "voice")
    }

    // Możesz dodać funkcje do usuwania plików ze Storage, jeśli wpis jest usuwany
}