package com.example.pinq_frontend.data.remote

import com.example.pinq_frontend.data.remote.dto.AttemptItemApiResponse
import com.example.pinq_frontend.data.remote.dto.BookmarkToggleApiResponse
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

    @GET("api/me/wrong-notes")
    suspend fun getMyWrongNotes(): List<AttemptItemApiResponse>

    @GET("api/bookmarks")
    suspend fun getMyBookmarks(): List<AttemptItemApiResponse>

    @POST("api/bookmarks/{quizId}")
    suspend fun addBookmark(@Path("quizId") quizId: Long): BookmarkToggleApiResponse

    @DELETE("api/bookmarks/{quizId}")
    suspend fun removeBookmark(@Path("quizId") quizId: Long): BookmarkToggleApiResponse
}
