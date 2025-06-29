package pl.sjanda.jpamietnik.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.sjanda.jpamietnik.data.JournalRepository

class JournalEntryEditViewModelFactory(
    private val repository: JournalRepository,
    private val journalId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalEntryEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalEntryEditViewModel(repository, journalId) as T
        }
        throw createUnknownViewModelClassException()
    }
}

class JournalDetailViewModelFactory(
    private val repository: JournalRepository,
    private val journalId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalDetailViewModel(repository, journalId) as T
        }
        throw createUnknownViewModelClassException()
    }
}

class JournalListViewModelFactory(private val repository: JournalRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalListViewModel(repository) as T
        }
        throw createUnknownViewModelClassException()
    }
}

private fun createUnknownViewModelClassException(): IllegalArgumentException =
    IllegalArgumentException("Unknown ViewModel class")