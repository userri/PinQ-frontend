package com.finq.app.ui.quiz

import com.finq.app.data.model.Quiz
import com.finq.app.data.repository.AnswerResult

/**
 * 퀴즈 한 세션(오늘 풀이 1회) 전체의 UI 상태.
 *
 * [quizzes] 는 오늘의 퀴즈 전체를 GET /api/quizzes/today 가 준 순서 그대로 담는다 —
 * 이미 푼 문제(solved=true)도 포함된다. 세션 진입(앱 재시작 포함) 때마다 이 목록을
 * 서버에서 새로 받아오므로, 진행 상태는 로컬에 남은 값이 아니라 항상 서버 응답이
 * 유일한 기준이 된다. 화면은 currentQuiz.solved 를 보고 채점 UI 또는 결과 보기
 * 모드([com.finq.app.ui.screen.SolvedQuizReviewScreen]) 를 고른다.
 */
data class QuizSessionUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    /** 오늘의 퀴즈 전체(서버 순서). 이미 푼 문제도 포함한다. */
    val quizzes: List<Quiz> = emptyList(),

    /** 지금 보고 있는 문제의 index (0-based). */
    val currentIndex: Int = 0,

    /** 사용자가 화면에서 고른 옵션 id. 미풀이 문제에서만 의미가 있다. */
    val selectedOptionId: Long? = null,

    /** 직전에 제출한 답의 채점 결과. answer 화면이 이걸 보고 그린다. */
    val lastAnswer: AnswerResult? = null,

    /** 답 제출 중인지 여부. */
    val isSubmitting: Boolean = false,

    /**
     * 이번 세션에서 "새로" 채점된(라이브 제출) 결과만 누적한다.
     * 이미 푼 문제는 여기 들어오지 않고 quiz.correct(서버가 아는 첫 시도 결과)로만
     * 집계되므로, 같은 문제를 다시 채점하는 경로가 없는 한 결과 요약은 항상
     * 첫 시도 결과와 일치한다.
     */
    val answerHistory: List<AnswerResult> = emptyList(),

    /**
     * 북마크된 퀴즈 id 집합 — /today 의 bookmarked 로 시드되고 토글로 갱신된다.
     * 풀이 화면과 정답 화면이 같은 값을 보므로 상태가 자연스럽게 동기화된다.
     */
    val bookmarkedIds: Set<Long> = emptySet(),
) {
    val currentQuiz: Quiz? get() = quizzes.getOrNull(currentIndex)
    val totalCount: Int get() = quizzes.size

    /** 예전엔 allQuizzes 기준 역산이 필요했지만, 이제 quizzes 가 곧 오늘 전체라 index 그대로 쓴다. */
    val progressIndex: Int get() = currentIndex

    val isLastQuiz: Boolean get() = quizzes.isNotEmpty() && currentIndex >= quizzes.lastIndex
    val canSubmit: Boolean get() = selectedOptionId != null && currentQuiz?.solved == false
    val isCurrentBookmarked: Boolean get() = currentQuiz?.id in bookmarkedIds

    /**
     * 오늘 누적 정답 수 — 서버가 이미 아는 첫 시도 결과(quiz.correct, solved=true 인 것만)
     * 와 이번 세션에서 새로 채점된 것(answerHistory) 을 합친다.
     * 같은 퀴즈가 두 집합에 동시에 걸치는 일은 없다 — 이미 푼 문제는 결과 보기 모드로만
     * 렌더되고 제출 자체가 불가능하기 때문이다.
     */
    val correctCount: Int
        get() = quizzes.count { it.solved && it.correct == true } +
            answerHistory.count { it.isCorrect }
}
