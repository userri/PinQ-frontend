package com.example.pinq_frontend.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinq_frontend.data.repository.QuizRepository
import com.example.pinq_frontend.data.repository.UserStatsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val quizRepository: QuizRepository,
    private val statsRepository: UserStatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadQuizInfo()
    }

    fun loadQuizInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 퀴즈 목록과 통계를 병렬로 호출
                val quizzesDeferred = async { quizRepository.getTodayQuizzes() }
                val statsDeferred   = async { statsRepository.getStats() }

                val quizzes = quizzesDeferred.await()
                val stats   = statsDeferred.await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        quizCount = quizzes.size,
                        streak = stats.streak,
                        activityGrid = stats.activityGrid,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "알 수 없는 오류") }
            }
        }
    }

    companion object {
        fun factory(
            quizRepository: QuizRepository,
            statsRepository: UserStatsRepository,
        ) = viewModelFactory {
            initializer { HomeViewModel(quizRepository, statsRepository) }
        }
    }
}
