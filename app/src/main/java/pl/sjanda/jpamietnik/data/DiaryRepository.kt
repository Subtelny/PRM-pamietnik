package pl.sjanda.jpamietnik.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object DiaryRepository {
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

}