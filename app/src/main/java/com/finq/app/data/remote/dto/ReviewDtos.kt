package com.finq.app.data.remote.dto

/**
 * `GET /api/reviews/today` 응답 — 오늘 복습할 오답들.
 *
 * @param nextDueDate reviews 가 비었을 때 "다음 물 주기" 안내에 쓴다.
 *                    복습할 게 하나도 남지 않았으면 null.
 */
data class ReviewsTodayApiResponse(
    val reviews: List<ReviewApiResponse>,
    val nextDueDate: String?,
)

/**
 * @param stage 복습 단계 0~2 (오답→3일→7일→14일→졸업).
 * @param categoryDisplayName 서버가 준 한글 라벨. 클라 enum 미등록 카테고리 대비로 이걸 그대로 쓴다.
 */
data class ReviewApiResponse(
    val quizId: Long,
    val category: String,
    val categoryDisplayName: String,
    val question: String,
    val choices: List<ReviewChoiceApiResponse>,
    val stage: Int,
    val dueDate: String?,
)

data class ReviewChoiceApiResponse(
    val id: Long,
    val orderNum: Int,
    val content: String,
)

/** `POST /api/reviews/{quizId}/answer` 요청 바디. */
data class ReviewAnswerApiRequest(
    val choiceId: Long,
)

/**
 * `POST /api/reviews/{quizId}/answer` 응답.
 *
 * @param graduated true 면 이 문제를 완전히 익혀 복습 큐에서 빠진다.
 */
data class ReviewAnswerApiResponse(
    val quizId: Long,
    val correct: Boolean,
    val correctChoiceId: Long,
    val explanation: String,
    val keyword: String?,
    val graduated: Boolean,
    val nextDueDate: String?,
)
