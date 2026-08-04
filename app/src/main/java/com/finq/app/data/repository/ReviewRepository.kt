package com.finq.app.data.repository

import com.finq.app.R
import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.RelatedArticle
import java.time.LocalDate

/**
 * 복습 성장 단계 — 오답을 3일 → 7일 → 14일 간격으로 다시 만나고, 맞히면 졸업한다.
 *
 * 서버의 stage(0~2)를 잔디 컨셉으로 표현한다.
 */
enum class ReviewStage(val label: String, val iconRes: Int) {
    SPROUT("새싹", R.drawable.ic_stage_sprout),
    GRASS("풀", R.drawable.ic_stage_grass),
    ALMOST_TREE("나무 직전", R.drawable.ic_stage_almost_tree),
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
    /** 물 준 총 횟수. */
    val waterCount: Int = 0,
    /** 흡수(정답) 횟수. */
    val absorbedCount: Int = 0,
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
    val stage: Int = 0,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    /** 졸업 시에만 값. "당신의 N번째 나무" 연출용. */
    val totalGraduatedTrees: Int? = null,
    /** 채점 후 관련 기사. 구서버/기사 없음이면 null. */
    val article: RelatedArticle? = null,
)

/** 정원의 나무/새싹 한 그루. */
data class GardenItem(
    val quizId: Long,
    /** 서버가 준 한글 라벨. */
    val categoryLabel: String,
    val question: String,
    val keyword: String?,
    val stage: ReviewStage,
    val dueDate: LocalDate?,
    val waterCount: Int,
    val absorbedCount: Int,
    /** 졸업 시각 ISO-8601. 자라는 중이면 null. */
    val graduatedAtIso: String?,
)

/**
 * 정원 전체. [graduatedTrees] 는 카운터가 진실 —
 * 배포 이전 졸업분은 [graduated] 목록에 없어 목록 길이와 다를 수 있다.
 */
data class ReviewGarden(
    val growing: List<GardenItem>,
    val graduated: List<GardenItem>,
    val graduatedTrees: Int,
    /**
     * 오늘 실제로 물 줄 수 있는 개수 — 서버가 하루 큐 캡을 적용해 내려준다.
     * [growing] 의 dueDate 로 직접 세지 말 것: 캡에 잘린 백로그까지 세어
     * 복습 세션에 실제로 들어오는 개수와 어긋난다.
     */
    val todayQueueSize: Int = 0,
) {
    companion object {
        val EMPTY = ReviewGarden(
            growing = emptyList(),
            graduated = emptyList(),
            graduatedTrees = 0,
            todayQueueSize = 0,
        )
    }
}

interface ReviewRepository {
    suspend fun getTodayReviews(): ReviewsToday
    suspend fun submitAnswer(quizId: Long, choiceId: Long): ReviewAnswer
    suspend fun getGarden(): ReviewGarden
}
