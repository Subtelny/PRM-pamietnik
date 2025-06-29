package pl.sjanda.jpamietnik.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.data.JournalEntry
import pl.sjanda.jpamietnik.data.JournalRepository

class JournalEntryEditViewModel(
    private val repository: JournalRepository,
    private val entryId: String
) : ViewModel() {


    private val _journal = MutableStateFlow<JournalEntry?>(null)
    val journal: StateFlow<JournalEntry?> = _journal.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _formState = MutableStateFlow(JournalEntryFormState())
    val formState: StateFlow<JournalEntryFormState> = _formState.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.getJournalEntry(entryId)
            if (result.isSuccess) {
                _journal.value = result.getOrThrow()


            } else {
                _error.value = "Nie udało się załadować wpisu: ${result.exceptionOrNull()}"
                _journal.value = null
            }
            _isLoading.value = false
        }
    }

    private fun loadEntryDetails(id: String) {
        _formState.update { it.copy(isNewEntry = true, isLoadingInitial = false) }

    }

    fun onNoteChange(newNote: String) {
        _formState.update { it.copy(note = newNote, noteError = null) }
    }

    fun onLocationChange(location: String) {
        _formState.update { it.copy(locationName = location) }
    }

    fun onPhotoUriChange(uri: Uri?) {
        _formState.update {
            it.copy(
                photoUri = uri,
                photoUrl = null
            )
        }
    }

    fun onVoiceRecordingUriChange(uri: Uri?) {
        _formState.update { it.copy(voiceRecordingUri = uri, voiceRecordingUrl = null) }
    }

    fun saveJournalEntry() {

    }
}

data class JournalEntryFormState(
    val entryId: String? = null,
    val note: String = "",
    val locationName: String = "", // Nazwa miejscowości
    val photoUri: Uri? = null,         // Lokalne URI zdjęcia przed wysłaniem
    val photoUrl: String? = null,       // URL zdjęcia po wysłaniu na serwer (dla edycji)
    val voiceRecordingUri: Uri? = null, // Lokalne URI nagrania przed wysłaniem
    val voiceRecordingUrl: String? = null,// URL nagrania po wysłaniu (dla edycji)

    val isNewEntry: Boolean = true,
    val isLoadingInitial: Boolean = false, // Ładowanie danych przy edycji istniejącego wpisu
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val noteError: String? = null,
    val saveError: String? = null
)