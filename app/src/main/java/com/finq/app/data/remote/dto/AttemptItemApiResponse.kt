package com.finq.app.data.remote.dto

/**
 * `GET /api/me/attempts`, `GET /api/me/wrong-notes`, `GET /api/bookmarks`
 * 응답 항목.
 *
 * 백엔드 com.example.pinq_backend.user.dto.AttemptItemResponse 와 매핑.
 */
data class AttemptItemApiResponse(
    val quizId: Long,
    val category: String,
    val categoryDisplayName: String,
    val question: String,
    val choices: List<ChoiceSummaryApi>,
    val selectedChoiceId: Long?,
    // 미풀이 북마크는 치팅 방지를 위해 정답 정보가 null 로 마스킹돼 온다.
    // (selectedChoiceId == null 또는 solvedAt == null 로 미풀이를 판별한다.)
    val correctChoiceId: Long? = null,
    val correct: Boolean = false,
    val explanation: String? = null,
    val keyword: String? = null,
    val article: ArticleApiResponse?,
    val bookmarked: Boolean,
    val solvedAt: String?,
    /** 복습(물 주기) 상태. 복습 큐에 오른 적 없는 문제면 null. 구서버도 null. */
    val review: ReviewStatusApi? = null,
)

data class ChoiceSummaryApi(
    val id: Long,
    val orderNum: Int,
    val content: String,
)

data class ReviewStatusApi(
    val stage: Int = 0,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    val graduated: Boolean = false,
    val dueDate: String? = null,
)
