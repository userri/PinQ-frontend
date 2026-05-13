package com.example.pinq_frontend.data.remote.dto

/**
 * `POST /api/quizzes/{id}/answer` 요청 바디.
 * 백엔드는 choiceId 를 받는다.
 */
data class AnswerApiRequest(
    val choiceId: Long,
)
