package com.finq.app.data.remote.dto

/**
 * `POST /api/quizzes/{id}/answer` 응답.
 *
 * 백엔드 com.example.pinq_backend.quiz.dto.AnswerResponse 와 매핑.
 *  - article: nullable 로 둠 (백엔드 NewsArticle 이 NULL 일 가능성에 대비)
 *  - publishedAt: ISO-8601 문자열 (Asia/Seoul 기준 — application.properties 에서 설정됨)
 */
data class AnswerApiResponse(
    val quizId: Long,
    val selectedChoiceId: Long,
    val correct: Boolean,
    val correctChoiceId: Long,
    val explanation: String,
    val keyword: String?,
    val article: ArticleApiResponse?,
)

data class ArticleApiResponse(
    val id: Long,
    val title: String,
    val url: String,
    val source: String,
    val category: String,
    val categoryDisplayName: String,
    val publishedAt: String?,
)
