package com.example.pinq_frontend.ui.quiz

import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.repository.AnswerResult

/**
 * 퀴즈 한 세션(오늘 풀이 1회) 전체의 UI 상태.
 *
 * 변경 가능성이 거의 없는 단일 data class 로 표현해 View 쪽에서 다루기 쉽게 한다.
 * (Phase 2 에서 좀 더 복잡해지면 sealed interface 로 쪼개도 늦지 않음)
 */
data class QuizSessionUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    /** 오늘의 퀴즈 전체. 로딩 끝나면 채워진다. */
    val quizzes: List<Quiz> = emptyList(),

    /** 지금 사용자가 풀고 있는 문제의 index (0-based). */
    val currentIndex: Int = 0,

    /** 사용자가 화면에서 고른 옵션 id. 정답 확인 전까지는 임시 값. */
    val selectedOptionId: Long? = null,

    /** 직전에 제출한 답의 채점 결과. answer 화면이 이걸 보고 그린다. */
    val lastAnswer: AnswerResult? = null,

    /** 답 제출 중인지 여부. */
    val isSubmitting: Boolean = false,

    /** 누적 정답 개수. done 화면에서 사용. */
    val correctCount: Int = 0,
) {
    val currentQuiz: Quiz? get() = quizzes.getOrNull(currentIndex)
    val totalCount: Int get() = quizzes.size
    val isLastQuiz: Boolean get() = quizzes.isNotEmpty() && currentIndex >= quizzes.size - 1
    val canSubmit: Boolean get() = selectedOptionId != null && currentQuiz != null
}
