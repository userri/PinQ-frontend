package com.finq.app.ui.quiz

import com.finq.app.data.model.Quiz
import com.finq.app.data.repository.AnswerResult

/**
 * 퀴즈 한 세션(오늘 풀이 1회) 전체의 UI 상태.
 *
 * 변경 가능성이 거의 없는 단일 data class 로 표현해 View 쪽에서 다루기 쉽게 한다.
 */
data class QuizSessionUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    /** 오늘의 퀴즈 전체. 진행도 표시 기준으로 사용한다. */
    val allQuizzes: List<Quiz> = emptyList(),

    /** 이번 세션에서 실제로 풀 미풀이 퀴즈 목록. */
    val quizzes: List<Quiz> = emptyList(),

    /** 지금 사용자가 풀고 있는 문제의 index (0-based). */
    val currentIndex: Int = 0,

    /** 사용자가 화면에서 고른 옵션 id. 정답 확인 전까지는 임시 값. */
    val selectedOptionId: Long? = null,

    /** 직전에 제출한 답의 채점 결과. answer 화면이 이걸 보고 그린다. */
    val lastAnswer: AnswerResult? = null,

    /** 답 제출 중인지 여부. */
    val isSubmitting: Boolean = false,

    /** 누적 정답 개수. 결과 리포트 화면에서 사용. */
    val correctCount: Int = 0,

    /**
     * 제출 완료된 문제들의 채점 결과 리스트 (제출 순서대로 누적).
     * 결과 리포트 화면에서 문제별 정오 표시에 사용한다.
     * 이번 세션의 quizzes[i] 와 answerHistory[i] 가 같은 문제에 대응된다.
     */
    val answerHistory: List<AnswerResult> = emptyList(),

    /**
     * 북마크된 퀴즈 id 집합 — /today 의 bookmarked 로 시드되고 토글로 갱신된다.
     * 풀이 화면과 정답 화면이 같은 값을 보므로 상태가 자연스럽게 동기화된다.
     */
    val bookmarkedIds: Set<Long> = emptySet(),
) {
    val currentQuiz: Quiz? get() = quizzes.getOrNull(currentIndex)
    val totalCount: Int get() = allQuizzes.ifEmpty { quizzes }.size
    val progressIndex: Int
        get() {
            val quiz = currentQuiz ?: return currentIndex
            val originalIndex = allQuizzes.indexOfFirst { it.id == quiz.id }
            return if (originalIndex >= 0) originalIndex else currentIndex
        }
    val isLastQuiz: Boolean get() = quizzes.isNotEmpty() && currentIndex >= quizzes.size - 1
    val canSubmit: Boolean get() = selectedOptionId != null && currentQuiz != null
    val isCurrentBookmarked: Boolean get() = currentQuiz?.id in bookmarkedIds
}
