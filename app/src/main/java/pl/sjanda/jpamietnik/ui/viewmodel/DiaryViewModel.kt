package pl.sjanda.jpamietnik.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.data.DiaryEntry
import pl.sjanda.jpamietnik.data.DiaryRepository

class DiaryViewModel : ViewModel() {
    private val repository = DiaryRepository

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentEntry = MutableStateFlow<DiaryEntry?>(null)
    val currentEntry: StateFlow<DiaryEntry?> = _currentEntry.asStateFlow()

    fun loadEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllEntries().collect { entriesList ->
                _entries.value = entriesList
            }
            _isLoading.value = false
        }
    }

    fun loadEntry(entryId: String) {
        viewModelScope.launch {
            val result = repository.getEntry(entryId)
            if (result.isSuccess) {
                _currentEntry.value = result.getOrNull()
            }
        }
    }

    fun saveEntry(entry: DiaryEntry, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.createEntry(entry)
            if (result.isSuccess) {
                loadEntries()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Error during save")
            }
        }
    }

    fun updateEntry(entry: DiaryEntry, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateEntry(entry)
            if (result.isSuccess) {
                loadEntries()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Error during update")
            }
        }
    }

    fun deleteEntry(entryId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteEntry(entryId)
            if (result.isSuccess) {
                loadEntries()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Error during delete")
            }
        }
    }
}