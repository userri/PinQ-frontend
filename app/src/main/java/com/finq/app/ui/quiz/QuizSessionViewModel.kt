package com.finq.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.repository.LibraryRepository
import com.finq.app.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 퀴즈 세션 전체를 책임지는 ViewModel.
 *
 * [loadQuizzesIfNeeded] — quizzes 가 비어있을 때만 서버에서 로드한다.
 * 세션을 나갔다 다시 들어오면(앱 재시작 포함) Navigation 이 ViewModel 을 새로 만들고,
 * 매번 GET /api/quizzes/today 를 다시 불러 진행 상태를 서버 기준으로 복원한다 —
 * 로컬에 남은 값은 전혀 신뢰하지 않는다.
 *
 * 이미 푼 문제(solved=true)도 [QuizSessionUiState.quizzes] 에 그대로 포함된다.
 * 화면(FinQNavigation 의 QuizRoute)이 currentQuiz.solved 를 보고 채점 UI 대신
 * 결과 보기 모드를 그리므로, 여기서는 "이미 푼 문제를 다시 채점하는" 진입점을
 * 아예 만들지 않는다 — 공식 재도전은 오답노트 → 복습(ReviewSessionViewModel) 경로뿐이다.
 */
class QuizSessionViewModel(
    private val quizRepository: QuizRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizSessionUiState())
    val uiState: StateFlow<QuizSessionUiState> = _uiState.asStateFlow()

    /** 북마크 토글 실패 안내 — 1회성 스낵바 메시지. */
    private val _bookmarkErrors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val bookmarkErrors: SharedFlow<String> = _bookmarkErrors.asSharedFlow()

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
                    // 이미 푼 문제가 있으면 그 다음 미풀이부터 이어서 보여준다.
                    // 전부 풀었으면(재진입 안전망) 0부터 — 결과 보기 모드로만 훑고 지나간다.
                    val startIndex = quizzes.indexOfFirst { quiz -> !quiz.solved }
                        .let { if (it == -1) 0 else it }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quizzes = quizzes,
                            currentIndex = startIndex,
                            selectedOptionId = null,
                            lastAnswer = null,
                            isSubmitting = false,
                            answerHistory = emptyList(),
                            bookmarkedIds = quizzes
                                .filter { quiz -> quiz.bookmarked }
                                .map { quiz -> quiz.id }
                                .toSet(),
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
        }
    }

    /**
     * 현재 문제 북마크 토글 — 낙관적 업데이트.
     * 아이콘을 즉시 뒤집고 API 를 쏜 뒤, 실패하면 되돌리고 스낵바 이벤트를 낸다.
     */
    fun toggleBookmark() {
        val quizId = _uiState.value.currentQuiz?.id ?: return
        val wasBookmarked = quizId in _uiState.value.bookmarkedIds

        _uiState.update { it.copy(bookmarkedIds = it.bookmarkedIds.toggled(quizId, !wasBookmarked)) }

        viewModelScope.launch {
            runCatching {
                if (wasBookmarked) libraryRepository.removeBookmark(quizId)
                else libraryRepository.addBookmark(quizId)
            }.onFailure {
                // 롤백
                _uiState.update { it.copy(bookmarkedIds = it.bookmarkedIds.toggled(quizId, wasBookmarked)) }
                _bookmarkErrors.tryEmit("북마크 저장에 실패했어요")
            }
        }
    }

    private fun Set<Long>.toggled(id: Long, add: Boolean): Set<Long> =
        if (add) this + id else this - id

    fun selectOption(optionId: Long) {
        // 이미 푼 문제(결과 보기 모드)에서는 선택지가 잠겨 있어야 한다.
        if (_uiState.value.currentQuiz?.solved == true) return
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val quiz = state.currentQuiz ?: return
        // 방어: 이미 푼 문제는 절대 다시 채점하지 않는다 — 결과 보기 모드는
        // moveToNext() 로만 다음 문제로 넘어가고 이 함수를 호출하지 않아야 정상이다.
        if (quiz.solved) return
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

    companion object {
        fun factory(
            repository: QuizRepository,
            libraryRepository: LibraryRepository,
        ) = viewModelFactory {
            initializer { QuizSessionViewModel(repository, libraryRepository) }
        }
    }
}
