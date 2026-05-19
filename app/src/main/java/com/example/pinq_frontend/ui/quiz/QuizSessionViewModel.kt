package com.example.pinq_frontend.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinq_frontend.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 퀴즈 세션 전체를 책임지는 ViewModel.
 *
 * [loadQuizzesIfNeeded] — quizzes 가 비어있을 때만 서버에서 로드한다.
 * 세션 중간에 홈으로 나갔다 돌아와도 ViewModel 인스턴스가 살아있으면
 * 이미 로드된 quizzes/answerHistory 를 그대로 유지한다.
 * (init 에서 무조건 loadQuizzes() 를 호출하지 않는 이유)
 */
class QuizSessionViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizSessionUiState())
    val uiState: StateFlow<QuizSessionUiState> = _uiState.asStateFlow()

    init {
        loadQuizzesIfNeeded()
    }

    /**
     * quizzes 가 이미 로드되어 있으면 아무것도 하지 않는다.
     * 세션 재진입(홈 → 풀기) 시 문제 수가 줄어드는 현상을 방지한다.
     */
    fun loadQuizzesIfNeeded() {
        if (_uiState.value.quizzes.isNotEmpty()) return
        fetchQuizzes()
    }

    /** 네트워크 오류 후 사용자가 명시적으로 재시도할 때만 호출. */
    fun loadQuizzes() {
        fetchQuizzes()
    }

    /** 실제 네트워크 요청과 상태 갱신을 담당하는 내부 헬퍼. */
    private fun fetchQuizzes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { quizRepository.getTodayQuizzes() }
                .onSuccess { quizzes ->
                    _uiState.update { it.copy(isLoading = false, quizzes = quizzes) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
        }
    }

    fun selectOption(optionId: Long) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val quiz = state.currentQuiz ?: return
        val selected = state.selectedOptionId ?: return
        if (state.lastAnswer != null || state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            runCatching { quizRepository.submitAnswer(quiz.id, selected) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            lastAnswer = result,
                            correctCount = if (result.isCorrect) it.correctCount + 1
                            else it.correctCount,
                            answerHistory = it.answerHistory + result,
                            error = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e.message ?: "Submit failed",
                        )
                    }
                }
        }
    }

    fun moveToNext() {
        _uiState.update {
            if (it.isLastQuiz) it
            else it.copy(
                currentIndex = it.currentIndex + 1,
                selectedOptionId = null,
                lastAnswer = null,
            )
        }
    }

    fun restart() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                selectedOptionId = null,
                lastAnswer = null,
                correctCount = 0,
                answerHistory = emptyList(),
            )
        }
    }

    companion object {
        fun factory(repository: QuizRepository) = viewModelFactory {
            initializer { QuizSessionViewModel(repository) }
        }
    }
}
