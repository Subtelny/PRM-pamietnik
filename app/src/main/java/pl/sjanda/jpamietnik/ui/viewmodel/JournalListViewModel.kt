package pl.sjanda.jpamietnik.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import pl.sjanda.jpamietnik.data.JournalEntry
import pl.sjanda.jpamietnik.data.JournalRepository


class JournalListViewModel(private val repository: JournalRepository) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val journals: StateFlow<List<JournalEntry>> = repository.getJournalEntriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


}