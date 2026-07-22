package com.finq.app.data.repository

import com.finq.app.data.model.AttemptItem

/**
 * "내 문제함" — 풀이 이력 / 오답노트 / 북마크 데이터 접근 추상화.
 *
 * ViewModel 은 이 인터페이스에만 의존한다.
 * 구현체:
 *  - [ApiLibraryRepository] : 서버 호출 (운영)
 *
 * 모든 메서드는 suspend — 네트워크 호출.
 */
interface LibraryRepository {

    /** 전체 풀이 이력 (최신순). */
    suspend fun getAttempts(): List<AttemptItem>

    /** 오답노트 — 첫 시도에서 틀린 문제만 (최신순). */
    suspend fun getWrongNotes(): List<AttemptItem>

    /** 북마크 목록 (최신 북마크순). */
    suspend fun getBookmarks(): List<AttemptItem>

    /** 단건 상세 (선택지·해설·기사 포함) — 카드 펼침 시 지연 로드. */
    suspend fun getAttemptDetail(quizId: Long): AttemptItem

    /** 북마크 추가. 토글 결과(bookmarked=true) 반환. */
    suspend fun addBookmark(quizId: Long): Boolean

    /** 북마크 해제. 토글 결과(bookmarked=false) 반환. */
    suspend fun removeBookmark(quizId: Long): Boolean
}
