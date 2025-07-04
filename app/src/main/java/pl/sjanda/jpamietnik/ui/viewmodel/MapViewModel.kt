package pl.sjanda.jpamietnik.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.data.DiaryEntry
import pl.sjanda.jpamietnik.data.DiaryRepository

class MapViewModel : ViewModel() {
    private val repository = DiaryRepository

    private val _mapEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val mapEntries: StateFlow<List<DiaryEntry>> = _mapEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMapEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllEntries().collect { entries ->
                _mapEntries.value = entries.filter {
                    it.location.latitude != 0.0 && it.location.longitude != 0.0
                }
            }
            _isLoading.value = false
        }
    }
}