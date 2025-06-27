package pl.sjanda.jpamietnik.data

import android.net.Uri
import com.example.locationdiary.data.database.FirebaseManager
import com.example.locationdiary.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DiaryRepository {
    private val firebaseManager = FirebaseManager()

    suspend fun createEntry(entry: DiaryEntry): Result<String> {
        return firebaseManager.saveEntry(entry)
    }

    suspend fun updateEntry(entry: DiaryEntry): Result<String> {
        return firebaseManager.saveEntry(entry)
    }

    suspend fun deleteEntry(entryId: String): Result<Unit> {
        return firebaseManager.deleteEntry(entryId)
    }

    suspend fun getEntry(entryId: String): Result<DiaryEntry?> {
        return firebaseManager.getEntry(entryId)
    }

    fun getAllEntries(): Flow<List<DiaryEntry>> = flow {
        val result = firebaseManager.getAllEntries()
        if (result.isSuccess) {
            emit(result.getOrNull() ?: emptyList())
        } else {
            emit(emptyList())
        }
    }

    suspend fun uploadImage(imageUri: Uri, entryId: String): Result<String> {
        return firebaseManager.uploadImage(imageUri, entryId)
    }

    suspend fun uploadAudio(audioUri: Uri, entryId: String): Result<String> {
        return firebaseManager.uploadAudio(audioUri, entryId)
    }
}