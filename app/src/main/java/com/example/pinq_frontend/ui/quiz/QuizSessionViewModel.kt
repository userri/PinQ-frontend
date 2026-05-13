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
 * 책임:
 *  - 오늘의 퀴즈 로딩
 *  - 사용자 옵션 선택 / 정답 제출 / 다음 문제 이동 / 재시작
 *  - 누적 정답 개수 관리
 *
 * 책임에서 명시적으로 제외:
 *  - 화면 전환 (NavController 가 담당)
 *  - UI 렌더링 (Composable 이 담당)
 */
class QuizSessionViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizSessionUiState())
    val uiState: StateFlow<QuizSessionUiState> = _uiState.asStateFlow()

    init {
        loadQuizzes()
    }

    /** 오늘의 퀴즈를 불러온다. 실패 시 error 메시지를 상태에 담아둔다. */
    fun loadQuizzes() {
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

    /** 사용자가 옵션을 탭했을 때 호출. */
    fun selectOption(optionId: Long) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    /**
     * 현재 선택된 옵션으로 정답 제출.
     * 결과는 [QuizSessionUiState.lastAnswer] 로 흘러간다.
     * 정답이면 [QuizSessionUiState.correctCount] 가 1 증가.
     */
    fun submitAnswer() {
        val state = _uiState.value
        val quiz = state.currentQuiz ?: return
        val selected = state.selectedOptionId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) } // 제출 시작
            runCatching { quizRepository.submitAnswer(quiz.id, selected) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            lastAnswer = result,
                            correctCount = if (result.isCorrect) it.correctCount + 1
                            else it.correctCount,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e.message ?: "Submit failed"
                        )
                    }
                }
        }
    }

    /**
     * 다음 문제로 이동.
     * 마지막 문제에서는 인덱스를 유지한다 (화면 전환은 NavController 가 isLastQuiz 로 판단).
     */
    fun moveToNext() {
        _uiState.update {
            if (it.isLastQuiz) {
                it
            } else {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOptionId = null,
                    lastAnswer = null,
                )
            }
        }
    }

    /** 처음부터 다시 풀기. 로드된 퀴즈 목록은 유지. */
    fun restart() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                selectedOptionId = null,
                lastAnswer = null,
                correctCount = 0,
            )
        }
    }

    companion object {
        /**
         * Repository 를 주입할 수 있는 팩토리.
         *
         *   val factory = QuizSessionViewModel.factory(DummyQuizRepository())
         *   val vm: QuizSessionViewModel = viewModel(factory = factory)
         */
        fun factory(repository: QuizRepository) = viewModelFactory {
            initializer { QuizSessionViewModel(repository) }
        }
    }
}
