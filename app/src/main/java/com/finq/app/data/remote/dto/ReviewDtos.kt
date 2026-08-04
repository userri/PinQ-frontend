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
    /** 물 준 총 횟수 (복습 채점 총 시도 수). 구서버엔 없음 → 기본 0. */
    val waterCount: Int = 0,
    /** 그중 맞힌 횟수. waterCount ≥ absorbedCount. */
    val absorbedCount: Int = 0,
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
    /** 채점 반영 후 단계. 구서버엔 없음 → 기본 0. */
    val stage: Int = 0,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    /** 졸업 시에만 숫자, 비졸업이면 null. */
    val totalGraduatedTrees: Int? = null,
    /** 일반 채점 화면과 동일 구조의 관련 기사. 구서버엔 없음 → null. */
    val article: ArticleApiResponse? = null,
)

/** `GET /api/reviews/garden` 응답 — 복습 나무 현황. */
data class GardenApiResponse(
    val growing: List<GardenItemApiResponse> = emptyList(),
    val graduated: List<GardenItemApiResponse> = emptyList(),
    /**
     * 나무 총계 카운터. 기능 배포 이전 졸업분은 graduated 목록에 없으므로
     * "총 몇 그루"는 항상 이 값을 신뢰한다 (graduated.size 와 다를 수 있음).
     */
    val graduatedTrees: Int = 0,
    /**
     * "오늘 물 줄 잔디 N개" 배지의 **유일한 원천** — `min(하루 큐 캡, due 개수)`.
     * 복습 큐가 하루 5개로 제한되므로 [growing] 의 dueDate 를 클라가 직접 세면
     * 캡에 잘린 백로그까지 세서 실제 큐 개수와 어긋난다. 구서버엔 없음 → 기본 0.
     */
    val todayQueueSize: Int = 0,
)

data class GardenItemApiResponse(
    val quizId: Long,
    val category: String,
    val categoryDisplayName: String,
    val question: String,
    val keyword: String? = null,
    val stage: Int,
    val dueDate: String? = null,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    val graduatedAt: String? = null,
)
