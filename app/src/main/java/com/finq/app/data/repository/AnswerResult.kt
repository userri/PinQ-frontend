package com.finq.app.data.repository

import com.finq.app.data.model.RelatedArticle

/**
 * 정답 채점 결과.
 *
 * 백엔드 `POST /api/quizzes/{id}/answer` 응답과 1:1 매칭.
 *  - keyword: 꼭 알아둘 단어 · 설명 (Phase 2 신규)
 *  - relatedArticle: 정답과 함께 나오는 기사 정보
 */
data class AnswerResult(
    val quizId: Long,
    val selectedOptionId: Long,
    val isCorrect: Boolean,
    val correctOptionId: Long,
    val explanation: String,
    val keyword: String?,
    val relatedArticle: RelatedArticle,
)
