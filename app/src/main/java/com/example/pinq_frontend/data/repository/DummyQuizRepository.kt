package com.example.pinq_frontend.data.repository

import com.example.pinq_frontend.data.DummyQuizData
import com.example.pinq_frontend.data.model.Quiz

/**
 * Phase 1 더미 구현.
 *
 * - 로컬 더미 데이터([DummyQuizData])를 그대로 반환한다.
 * - suspend 로 선언했지만 실제 일시정지/딜레이는 없어 즉시 반환된다.
 *   (네트워크 지연 시뮬레이션이 필요하면 delay(300) 등을 추가하면 된다.)
 */
class DummyQuizRepository : QuizRepository {

    override suspend fun getTodayQuizzes(): List<Quiz> = DummyQuizData.todayQuizzes

    override suspend fun submitAnswer(
        quizId: Long,
        selectedOptionId: Long,
    ): AnswerResult {
        val quiz = DummyQuizData.findById(quizId)
            ?: throw NoSuchElementException("Quiz not found: id=$quizId")
        return AnswerResult(
            quizId = quiz.id,
            selectedOptionId = selectedOptionId,
            isCorrect = quiz.correctOptionId == selectedOptionId,
            correctOptionId = quiz.correctOptionId,
            explanation = quiz.explanation,
            keyword = null, // 더미 모드엔 keyword 없음. API 모드에서만 내려온다.
            relatedArticle = quiz.relatedArticle,
        )
    }
}
