package com.finq.app.ui.review

import com.finq.app.data.model.Category
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.repository.AnswerResult
import com.finq.app.data.repository.ReviewAnswer
import com.finq.app.data.repository.ReviewItem

/**
 * 복습 항목을 기존 퀴즈 화면이 먹는 [Quiz] 로 변환한다.
 *
 * ⚠️ [Quiz.category] 는 화면에 쓰이지 않는다 — 복습 화면은 서버가 준
 * [ReviewItem.categoryLabel] 을 `categoryLabel` 파라미터로 직접 넘기기 때문이다.
 * (그래서 여기서는 enum 파싱을 시도하지 않고 placeholder 를 둔다.)
 */
fun ReviewItem.toQuiz(): Quiz = Quiz(
    id = quizId,
    category = Category.STOCK,
    question = question,
    options = options,
)

/**
 * 복습 채점 결과를 기존 정답 화면이 먹는 [AnswerResult] 로 변환한다.
 * 복습에는 관련 기사가 없으므로 [RelatedArticle.EMPTY] — 정답 화면이 기사 섹션을 자동으로 숨긴다.
 */
fun ReviewAnswer.toAnswerResult(selectedOptionId: Long): AnswerResult = AnswerResult(
    quizId = quizId,
    selectedOptionId = selectedOptionId,
    isCorrect = isCorrect,
    correctOptionId = correctOptionId,
    explanation = explanation,
    keyword = keyword,
    relatedArticle = RelatedArticle.EMPTY,
)
