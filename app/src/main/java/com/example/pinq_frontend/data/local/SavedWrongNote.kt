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
)
