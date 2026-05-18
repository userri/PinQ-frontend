package com.example.pinq_frontend.data.remote.dto

/**
 * `POST /api/bookmarks/{quizId}` 또는 `DELETE /api/bookmarks/{quizId}` 응답.
 *
 * 서버는 토글 결과를 (quizId, bookmarked) 로 idempotent 하게 반환한다.
 */
data class BookmarkToggleApiResponse(
    val quizId: Long,
    val bookmarked: Boolean,
)
