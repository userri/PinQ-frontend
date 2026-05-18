package com.example.pinq_frontend.data.remote.dto

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
    val correctChoiceId: Long,
    val correct: Boolean,
    val explanation: String,
    val keyword: String?,
    val article: ArticleApiResponse?,
    val bookmarked: Boolean,
    val solvedAt: String?,
)

data class ChoiceSummaryApi(
    val id: Long,
    val orderNum: Int,
    val content: String,
)
