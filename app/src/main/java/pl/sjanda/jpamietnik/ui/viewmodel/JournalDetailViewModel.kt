package pl.sjanda.jpamietnik.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.data.JournalEntry
import pl.sjanda.jpamietnik.data.JournalRepository


class JournalDetailViewModel(
    private val repository: JournalRepository,
    private val journalId: String
) : ViewModel() {

    private val _journal = MutableStateFlow<JournalEntry?>(null)
    val journal: StateFlow<JournalEntry?> = _journal.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.getJournalEntry(journalId)
            if (result.isSuccess) {
                _journal.value = result.getOrThrow()
            } else {
                _error.value = "Nie udało się załadować wpisu: ${result.exceptionOrNull()}"
                _journal.value = null
            }
            _isLoading.value = false
        }
    }

}