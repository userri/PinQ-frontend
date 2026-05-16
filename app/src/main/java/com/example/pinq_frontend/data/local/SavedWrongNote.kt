package com.example.pinq_frontend.data.local

import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.repository.AnswerResult

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
) {
    companion object {
        /**
         * Quiz + AnswerResult 쌍으로부터 SavedWrongNote 를 생성하는 단일 매핑 헬퍼.
         *
         * [com.example.pinq_frontend.ui.quiz.QuizSessionViewModel.saveWrongNotes] 와
         * [com.example.pinq_frontend.ui.screen.WrongNoteScreen] 양쪽에서 이 함수를 호출해
         * SavedWrongNote 의 필드 구성이 항상 동일하게 유지되도록 한다.
         */
        fun from(quiz: Quiz, answer: AnswerResult): SavedWrongNote {
            val article = answer.relatedArticle
            return SavedWrongNote(
                quizId = quiz.id,
                question = quiz.question,
                categoryName = quiz.category.name,
                categoryDisplay = quiz.category.displayName,
                myAnswerText = quiz.options.find { it.id == answer.selectedOptionId }?.text ?: "-",
                correctAnswerText = quiz.options.find { it.id == answer.correctOptionId }?.text ?: "-",
                explanation = answer.explanation,
                keyword = answer.keyword,
                relatedArticleTitle = article.title.takeIf { it.isNotBlank() },
                relatedArticleUrl = article.url.takeIf { it.isNotBlank() },
                relatedArticleSource = article.source.takeIf { it.isNotBlank() },
            )
        }
    }
}
