package com.finq.app.ui.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewRepository
import com.finq.app.ui.userErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GardenUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val garden: ReviewGarden? = null,
)

class GardenViewModel(
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { reviewRepository.getGarden() }
                .onSuccess { garden ->
                    _uiState.update { it.copy(isLoading = false, garden = garden) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = userErrorMessage(e, "정원을 불러오지 못했어요"))
                    }
                }
        }
    }

    companion object {
        fun factory(reviewRepository: ReviewRepository) = viewModelFactory {
            initializer { GardenViewModel(reviewRepository) }
        }
    }
}
