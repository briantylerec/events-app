package com.redbull.lab.events.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redbull.lab.domain.Product
import com.redbull.lab.events.domain.GetEventHighlightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventsUiState(
    val isLoading: Boolean = true,
    val highlights: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val hasError: Boolean = false,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventHighlights: GetEventHighlightsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = EventsUiState(isLoading = true)
            try {
                _uiState.value = EventsUiState(isLoading = false, highlights = getEventHighlights())
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.value = EventsUiState(
                    isLoading = false,
                    errorMessage = error.message,
                    hasError = true,
                )
            }
        }
    }
}
