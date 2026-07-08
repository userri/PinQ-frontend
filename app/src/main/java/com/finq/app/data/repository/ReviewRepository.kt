package com.finq.app.data.repository

import com.finq.app.data.model.QuizOption
import java.time.LocalDate

/**
 * 복습 성장 단계 — 오답을 3일 → 7일 → 14일 간격으로 다시 만나고, 맞히면 졸업한다.
 *
 * 서버의 stage(0~2)를 잔디 컨셉으로 표현한다.
 */
enum class ReviewStage(val label: String, val emoji: String) {
    SPROUT("새싹", "🌱"),
    GRASS("풀", "🌿"),
    ALMOST_TREE("나무 직전", "🪴"),
    ;

    /** 이번에 맞히면 졸업(나무)하는 마지막 단계인가. */
    val isFinalStage: Boolean get() = this == ALMOST_TREE

    companion object {
        /** 서버 stage 가 범위를 벗어나면 가장 가까운 단계로 클램프한다. */
        fun of(stage: Int): ReviewStage = entries[stage.coerceIn(0, entries.lastIndex)]
    }
}

/** 복습 대기 중인 오답 한 건. */
data class ReviewItem(
    val quizId: Long,
    /** 서버가 준 한글 라벨을 그대로 쓴다. */
    val categoryLabel: String,
    val question: String,
    val options: List<QuizOption>,
    val stage: ReviewStage,
    val dueDate: LocalDate?,
)

/** [nextDueDate] 는 [items] 가 비었을 때 "다음 물 주기" 안내에 쓴다. */
data class ReviewsToday(
    val items: List<ReviewItem>,
    val nextDueDate: LocalDate?,
) {
    companion object {
        val EMPTY = ReviewsToday(items = emptyList(), nextDueDate = null)
    }
}

/** 복습 채점 결과. [graduated] 면 이 문제는 복습 큐에서 빠진다. */
data class ReviewAnswer(
    val quizId: Long,
    val isCorrect: Boolean,
    val correctOptionId: Long,
    val explanation: String,
    val keyword: String?,
    val graduated: Boolean,
    val nextDueDate: LocalDate?,
)

interface ReviewRepository {
    suspend fun getTodayReviews(): ReviewsToday
    suspend fun submitAnswer(quizId: Long, choiceId: Long): ReviewAnswer
}
