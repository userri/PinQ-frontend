package com.finq.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.repository.ReviewAnswer
import com.finq.app.data.repository.ReviewItem
import com.finq.app.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 복습 세션 상태.
 *
 * 퀴즈 세션(QuizSessionUiState)과 같은 규약: 파생 프로퍼티로 화면이 필요한 값을 계산한다.
 */
data class ReviewSessionUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<ReviewItem> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionId: Long? = null,
    val lastAnswer: ReviewAnswer? = null,
    val isSubmitting: Boolean = false,
    /** 이번 세션에서 졸업(완전 습득)한 문제 수. */
    val graduatedCount: Int = 0,
    val correctCount: Int = 0,
    /** 세션의 모든 문제를 다 푼 상태. */
    val isFinished: Boolean = false,
    /**
     * **사용자 단위** 다음 물 주기 — `GET /api/reviews/today` 가 준 값만 쓴다.
     * 채점 응답에도 같은 이름 필드가 있지만 그건 그 문제 하나의 예정일이라 여기 넣으면 안 된다.
     */
    val nextDueDate: LocalDate? = null,
    /** 일회성 안내(스낵바용). 예: 이미 졸업한 문제 404. */
    val notice: String? = null,
    /**
     * **오늘** 물 준 개수 / 그중 자란 개수. 완료 화면은 세션이 아니라 이 값을 쓴다 —
     * 정원에서 1개 + 세션에서 4개면 사용자 머릿속 단위는 "오늘 5개"다.
     */
    val todayReviewed: Int = 0,
    val todayCorrect: Int = 0,
) {
    val currentItem: ReviewItem? get() = items.getOrNull(currentIndex)
    val totalCount: Int get() = items.size
    val isLastItem: Boolean get() = items.isNotEmpty() && currentIndex >= items.size - 1
    val canSubmit: Boolean get() = selectedOptionId != null && currentItem != null && !isSubmitting
}

class ReviewSessionViewModel(
    private val reviewRepository: ReviewRepository,
    /** 이 문제부터 시작(정원 탭 진입). 큐에 없으면 무시된다. */
    private val startQuizId: Long? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewSessionUiState())
    val uiState: StateFlow<ReviewSessionUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { reviewRepository.getTodayReviews() }
                .onSuccess { today ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // 정원에서 특정 식물을 눌러 들어왔으면 그 문제를 맨 앞으로.
                            // 큐에 없으면(자정을 넘겼거나 다른 기기에서 먼저 푼 경우)
                            // 순서를 건드리지 않고 큐 처음부터 간다.
                            items = today.items.startingWith(startQuizId),
                            nextDueDate = today.nextDueDate,
                            todayReviewed = today.todayReviewed,
                            todayCorrect = today.todayCorrect,
                            currentIndex = 0,
                            selectedOptionId = null,
                            lastAnswer = null,
                            // 복습할 게 아예 없으면 곧장 완료 상태로 둔다.
                            isFinished = today.items.isEmpty(),
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "복습 문제를 불러오지 못했어요")
                    }
                }
        }
    }

    /**
     * 세션을 마친 뒤 사용자 단위 상태를 다시 받는다.
     *
     * 세션 시작 때 받은 `nextDueDate` 와 오늘 집계는 "오늘 몫이 남아 있던" 시점의 값이라,
     * 다 풀고 난 지금의 답이 아니다. 캡에 잘린 백로그가 남았으면 서버가 오늘+1 을 준다.
     * 목록(items)은 건드리지 않는다 — 완료 화면이 세션 결과를 계속 보여줘야 한다.
     */
    fun refreshNextDueDate() {
        viewModelScope.launch {
            runCatching { reviewRepository.getTodayReviews() }
                .onSuccess { today ->
                    _uiState.update {
                        it.copy(
                            nextDueDate = today.nextDueDate,
                            todayReviewed = today.todayReviewed,
                            todayCorrect = today.todayCorrect,
                        )
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
        val item = state.currentItem ?: return
        val choiceId = state.selectedOptionId ?: return
        if (state.isSubmitting || state.lastAnswer != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching { reviewRepository.submitAnswer(item.quizId, choiceId) }
                .onSuccess { answer ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            lastAnswer = answer,
                            correctCount = it.correctCount + if (answer.isCorrect) 1 else 0,
                            graduatedCount = it.graduatedCount + if (answer.graduated) 1 else 0,
                            // nextDueDate 를 채점 응답으로 덮지 않는다. 채점 응답의 값은
                            // **방금 푼 그 문제**의 다음 예정일(stage 2 면 +14일)이고,
                            // 완료 화면이 물어야 할 건 "내가 언제 또 복습하나"라는
                            // **사용자 단위** 날짜다. 둘을 섞어 "다음 물주기 8월 18일" 이
                            // 뜨는데 실제로는 백로그가 남아 내일 또 해야 하는 상태였다.
                        )
                    }
                }
                .onFailure { e ->
                    if (e is retrofit2.HttpException && e.code() == 404) {
                        // 이미 졸업한 문제(캐시된 화면에서 낡은 요청) — 목록을 재동기화한다.
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                notice = "이미 졸업한 문제예요 — 복습 목록을 새로 불러올게요",
                            )
                        }
                        loadReviews()
                    } else {
                        _uiState.update {
                            it.copy(isSubmitting = false, error = e.message ?: "채점에 실패했어요")
                        }
                    }
                }
        }
    }

    fun clearNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    /** 정답 화면에서 "다음" — 마지막 문제였으면 세션을 끝낸다. */
    fun moveToNext() {
        _uiState.update {
            if (it.isLastItem) {
                it.copy(isFinished = true, selectedOptionId = null, lastAnswer = null)
            } else {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOptionId = null,
                    lastAnswer = null,
                )
            }
        }
    }

    companion object {
        fun factory(reviewRepository: ReviewRepository, startQuizId: Long? = null) =
            viewModelFactory {
                initializer { ReviewSessionViewModel(reviewRepository, startQuizId) }
            }
    }
}

/** [startQuizId] 를 맨 앞으로 옮긴 목록. 없으면 원본 그대로. */
internal fun List<ReviewItem>.startingWith(startQuizId: Long?): List<ReviewItem> {
    if (startQuizId == null) return this
    val i = indexOfFirst { it.quizId == startQuizId }
    if (i <= 0) return this
    return listOf(this[i]) + filterIndexed { idx, _ -> idx != i }
}
