package com.finq.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Quiz
import com.finq.app.data.repository.AnswerResult
import com.finq.app.data.repository.LibraryRepository
import com.finq.app.data.repository.QuizRepository
import com.finq.app.ui.userErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 단건 풀이 상태 — 미풀이 북마크 "풀러 가기" 진입 경로.
 *
 * 오늘 세트가 아닌 지난 문제도 quizId 만으로 풀 수 있다:
 *  - 로드: GET /api/me/attempts/{quizId} — 미풀이면 정답/해설이 서버에서 마스킹돼
 *    null 로 오지만 question·choices 는 온전해 풀이 화면을 그릴 수 있다.
 *  - 채점: POST /api/quizzes/{quizId}/answer — 오늘 문제와 완전히 동일한 경로.
 *    attempt 는 제출한 오늘 날짜로 기록돼 잔디·스트릭에 정상 반영된다.
 */
data class SoloQuizUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val item: AttemptItem? = null,
    val selectedOptionId: Long? = null,
    val isSubmitting: Boolean = false,
    val lastAnswer: AnswerResult? = null,
    /** 삭제된 문제(404) — 안내 후 북마크 목록으로 복귀한다. */
    val notFound: Boolean = false,
    /**
     * 목록이 낡아 미풀이인 줄 알고 들어왔지만 서버 기준 이미 푼 문제.
     * 재제출 대신 안내 후 복귀한다(목록 재로드로 해설 카드가 열린다).
     */
    val alreadySolved: Boolean = false,
) {
    val canSubmit: Boolean get() = selectedOptionId != null && item != null && !isSubmitting
}

class SoloQuizViewModel(
    private val quizId: Long,
    private val libraryRepository: LibraryRepository,
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoloQuizUiState())
    val uiState: StateFlow<SoloQuizUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { libraryRepository.getAttemptDetail(quizId) }
                .onSuccess { item ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            item = item,
                            alreadySolved = !item.unsolved,
                        )
                    }
                }
                .onFailure { e ->
                    if (e is retrofit2.HttpException && e.code() == 404) {
                        _uiState.update { it.copy(isLoading = false, notFound = true) }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, error = userErrorMessage(e, "문제를 불러오지 못했어요"))
                        }
                    }
                }
        }
    }

    fun selectOption(optionId: Long) {
        // 채점이 끝난 뒤에는 선택을 바꿀 수 없다.
        if (_uiState.value.lastAnswer != null) return
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val choiceId = state.selectedOptionId ?: return
        if (state.item == null || state.isSubmitting || state.lastAnswer != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching { quizRepository.submitAnswer(quizId, choiceId) }
                .onSuccess { answer ->
                    _uiState.update { it.copy(isSubmitting = false, lastAnswer = answer) }
                }
                .onFailure { e ->
                    if (e is retrofit2.HttpException && e.code() == 404) {
                        _uiState.update { it.copy(isSubmitting = false, notFound = true) }
                    } else {
                        _uiState.update {
                            it.copy(isSubmitting = false, error = userErrorMessage(e, "채점에 실패했어요"))
                        }
                    }
                }
        }
    }

    companion object {
        fun factory(
            quizId: Long,
            libraryRepository: LibraryRepository,
            quizRepository: QuizRepository,
        ) = viewModelFactory {
            initializer { SoloQuizViewModel(quizId, libraryRepository, quizRepository) }
        }
    }
}

/**
 * 보관함 항목을 기존 퀴즈 화면이 먹는 [Quiz] 로 변환한다.
 * 미풀이 항목은 정답/해설이 서버에서 마스킹돼 있지만, 풀이 화면은 question·choices 만
 * 사용하므로 문제없다 (정답·해설은 채점 응답 [AnswerResult] 로만 들어온다).
 */
fun AttemptItem.toSoloQuiz(): Quiz = Quiz(
    id = quizId,
    category = category,
    question = question,
    options = choices,
    bookmarked = bookmarked,
)
