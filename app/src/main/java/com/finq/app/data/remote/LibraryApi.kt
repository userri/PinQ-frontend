package com.finq.app.data.remote

import com.finq.app.data.remote.dto.AttemptItemApiResponse
import com.finq.app.data.remote.dto.BookmarkToggleApiResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * "내 문제함" — 풀이 이력 / 오답노트 / 북마크 API.
 *
 * 백엔드 라우트:
 *  - GET    /api/me/attempts          → 전체 풀이 이력 (최신순)
 *  - GET    /api/me/wrong-notes       → 오답 (첫 시도 실패만, 최신순)
 *  - GET    /api/bookmarks            → 북마크 목록 (최신 북마크순)
 *  - POST   /api/bookmarks/{quizId}   → 북마크 추가 (idempotent)
 *  - DELETE /api/bookmarks/{quizId}   → 북마크 해제 (idempotent)
 */
interface LibraryApi {

    @GET("api/me/attempts")
    suspend fun getMyAttempts(): List<AttemptItemApiResponse>

    /** 단건 상세 — 목록이 요약만 줄 때 카드 펼침 시 호출. 선택지·해설·기사 포함. */
    @GET("api/me/attempts/{quizId}")
    suspend fun getAttemptDetail(@Path("quizId") quizId: Long): AttemptItemApiResponse

    @GET("api/me/wrong-notes")
    suspend fun getMyWrongNotes(): List<AttemptItemApiResponse>

    @GET("api/bookmarks")
    suspend fun getMyBookmarks(): List<AttemptItemApiResponse>

    @POST("api/bookmarks/{quizId}")
    suspend fun addBookmark(@Path("quizId") quizId: Long): BookmarkToggleApiResponse

    @DELETE("api/bookmarks/{quizId}")
    suspend fun removeBookmark(@Path("quizId") quizId: Long): BookmarkToggleApiResponse
}
