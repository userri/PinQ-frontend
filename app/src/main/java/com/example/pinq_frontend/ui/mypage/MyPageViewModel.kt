package com.example.pinq_frontend.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinq_frontend.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPageUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val streak: Int = 0,
    val totalSolved: Int = 0,
    val correctRate: Float = 0f,
    val activityGrid: List<Boolean> = emptyList(),
)

class MyPageViewModel(private val statsRepository: UserStatsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { statsRepository.getStats() }
                .onSuccess { stats ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            streak = stats.streak,
                            totalSolved = stats.totalSolved,
                            correctRate = stats.correctRate,
                            activityGrid = stats.activityGrid,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "통계를 불러오지 못했어요")
                    }
                }
        }
    }

    companion object {
        fun factory(repository: UserStatsRepository) = viewModelFactory {
            initializer { MyPageViewModel(repository) }
        }
    }
}
