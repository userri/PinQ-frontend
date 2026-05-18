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

            // 퀴즈 로드: 실패 시 홈 화면 전체 에러
            val quizzes = try {
                quizRepository.getTodayQuizzes()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "알 수 없는 오류") }
                return@launch
            }

            // 통계 로드: 부가 정보이므로 실패해도 기본값으로 폴백, 퀴즈 진입은 막지 않음
            val stats = try {
                statsRepository.getStats()
            } catch (e: Exception) {
                null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    nickname = stats?.nickname ?: "",
                    quizCount = quizzes.size,
                    streak = stats?.streak ?: 0,
                    activityGrid = stats?.activityGrid ?: emptyList(),
                )
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
