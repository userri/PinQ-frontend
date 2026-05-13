package com.example.pinq_frontend.data.repository

import com.example.pinq_frontend.data.model.Category
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.QuizOption
import com.example.pinq_frontend.data.model.RelatedArticle
import com.example.pinq_frontend.data.remote.QuizApi
import com.example.pinq_frontend.data.remote.dto.AnswerApiRequest
import com.example.pinq_frontend.data.remote.dto.AnswerApiResponse
import com.example.pinq_frontend.data.remote.dto.ArticleApiResponse
import com.example.pinq_frontend.data.remote.dto.ChoiceApiResponse
import com.example.pinq_frontend.data.remote.dto.QuizApiResponse

/**
 * 백엔드 API 를 사용하는 [QuizRepository] 구현체.
 *
 * Retrofit suspend 함수는 OkHttp 의 내부 디스패처(IO 스레드풀)에서 실행되므로
 * 추가로 withContext(Dispatchers.IO) 로 감쌀 필요 없다.
 *
 * 서버 응답 DTO → 도메인 모델 매핑은 이 클래스 안에서만 일어난다.
 * 상위 계층(ViewModel/Screen)은 도메인 모델만 알면 된다.
 */
class ApiQuizRepository(
    private val api: QuizApi,
) : QuizRepository {

    override suspend fun getTodayQuizzes(): List<Quiz> {
        return api.getTodayQuizzes().map { it.toDomain() }
    }

    override suspend fun submitAnswer(
        quizId: Long,
        selectedOptionId: Long,
    ): AnswerResult {
        val response = api.submitAnswer(quizId, AnswerApiRequest(choiceId = selectedOptionId))
        return response.toDomain()
    }

    // ─── DTO → 도메인 매퍼들 ───────────────────────────────────────────────────

    private fun QuizApiResponse.toDomain(): Quiz = Quiz(
        id = id,
        category = parseCategory(category),
        question = question,
        options = choices.map { it.toDomain() },
        // /today 응답에는 정답 정보가 빠져있다 (보안). placeholder 만 채워둠.
        // 실제 정답/해설/기사는 submitAnswer 의 AnswerResult 로만 전달된다.
        correctOptionId = 0L,
        explanation = "",
        relatedArticle = RelatedArticle.EMPTY,
    )

    private fun ChoiceApiResponse.toDomain(): QuizOption = QuizOption(
        id = id,
        optionNumber = orderNum,
        text = content,
    )

    private fun AnswerApiResponse.toDomain(): AnswerResult = AnswerResult(
        quizId = quizId,
        selectedOptionId = selectedChoiceId,
        isCorrect = correct,
        correctOptionId = correctChoiceId,
        explanation = explanation,
        keyword = keyword,
        relatedArticle = article?.toDomain() ?: RelatedArticle.EMPTY,
    )

    private fun ArticleApiResponse.toDomain(): RelatedArticle = RelatedArticle(
        id = id,
        title = title,
        url = url,
        source = source,
        category = category,
        categoryDisplayName = categoryDisplayName,
        publishedAt = publishedAt,
    )

    private fun parseCategory(raw: String): Category = runCatching {
        Category.valueOf(raw)
    }.getOrElse {
        // 서버가 알 수 없는 카테고리를 내려보낼 경우 안전한 폴백.
        Category.STOCK
    }
}
