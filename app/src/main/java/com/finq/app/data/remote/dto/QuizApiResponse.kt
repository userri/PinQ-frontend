package com.finq.app.data.remote.dto

/**
 * `GET /api/quizzes/today` 응답의 한 원소.
 *
 * 백엔드 com.example.pinq_backend.quiz.dto.QuizResponse 와 1:1 매칭.
 * Moshi 가 필드 이름 그대로 JSON 키를 매핑한다.
 */
data class QuizApiResponse(
    val id: Long,
    val category: String,
    val categoryDisplayName: String,
    val question: String,
    val choices: List<ChoiceApiResponse>,
    val solved: Boolean = false,
    val correct: Boolean? = null,
    /** 북마크 토글 초기 상태. 구버전 서버 응답엔 없을 수 있어 기본 false. */
    val bookmarked: Boolean = false,
)

data class ChoiceApiResponse(
    val id: Long,
    val orderNum: Int,
    val content: String,
)
