package com.example.pinq_frontend.data.local

data class SavedWrongNote(
    val quizId: Long,
    val question: String,
    val categoryName: String,
    val categoryDisplay: String,
    val myAnswerText: String,
    val correctAnswerText: String,
    val explanation: String,
    val keyword: String?,
    val savedDateMillis: Long = System.currentTimeMillis(),
    // 관련 기사 정보 (없으면 null)
    val relatedArticleTitle: String? = null,
    val relatedArticleUrl: String? = null,
    val relatedArticleSource: String? = null,
)
