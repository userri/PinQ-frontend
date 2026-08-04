package com.finq.app.data.repository

import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.remote.ReviewApi
import com.finq.app.data.remote.dto.GardenApiResponse
import com.finq.app.data.remote.dto.GardenItemApiResponse
import com.finq.app.data.remote.dto.ReviewAnswerApiRequest
import com.finq.app.data.remote.dto.ReviewAnswerApiResponse
import com.finq.app.data.remote.dto.ReviewApiResponse
import com.finq.app.data.remote.dto.ReviewsTodayApiResponse
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ApiReviewRepository(private val api: ReviewApi) : ReviewRepository {

    override suspend fun getTodayReviews(): ReviewsToday = api.getTodayReviews().toDomain()

    override suspend fun submitAnswer(quizId: Long, choiceId: Long): ReviewAnswer =
        api.submitReviewAnswer(quizId, ReviewAnswerApiRequest(choiceId)).toDomain()

    override suspend fun getGarden(): ReviewGarden = api.getGarden().toDomain()
}

// ── DTO → 도메인 ─────────────────────────────────────────────────────────────

private fun ReviewsTodayApiResponse.toDomain(): ReviewsToday = ReviewsToday(
    items = reviews.map { it.toDomain() },
    nextDueDate = nextDueDate?.let(::parseDate),
)

private fun ReviewApiResponse.toDomain(): ReviewItem = ReviewItem(
    quizId = quizId,
    categoryLabel = categoryDisplayName,
    question = question,
    // 서버가 순서를 보장하지 않을 수 있으므로 orderNum 으로 정렬해 보기 번호를 안정시킨다.
    options = choices.sortedBy { it.orderNum }.map {
        QuizOption(id = it.id, optionNumber = it.orderNum, text = it.content)
    },
    stage = ReviewStage.of(stage),
    dueDate = dueDate?.let(::parseDate),
    waterCount = waterCount,
    absorbedCount = absorbedCount,
)

private fun ReviewAnswerApiResponse.toDomain(): ReviewAnswer = ReviewAnswer(
    quizId = quizId,
    isCorrect = correct,
    correctOptionId = correctChoiceId,
    explanation = explanation,
    keyword = keyword,
    graduated = graduated,
    nextDueDate = nextDueDate?.let(::parseDate),
    stage = stage,
    waterCount = waterCount,
    absorbedCount = absorbedCount,
    totalGraduatedTrees = totalGraduatedTrees,
    article = article?.let {
        RelatedArticle(
            title = it.title,
            url = it.url,
            source = it.source,
            id = it.id,
            category = it.category,
            categoryDisplayName = it.categoryDisplayName,
            publishedAt = it.publishedAt,
        )
    },
)

private fun GardenApiResponse.toDomain(): ReviewGarden = ReviewGarden(
    growing = growing.map { it.toDomain() },
    graduated = graduated.map { it.toDomain() },
    graduatedTrees = graduatedTrees,
    todayQueueSize = todayQueueSize,
)

private fun GardenItemApiResponse.toDomain(): GardenItem = GardenItem(
    quizId = quizId,
    categoryLabel = categoryDisplayName,
    question = question,
    keyword = keyword,
    stage = ReviewStage.of(stage),
    dueDate = dueDate?.let(::parseDate),
    waterCount = waterCount,
    absorbedCount = absorbedCount,
    graduatedAtIso = graduatedAt,
)

/** ISO-8601 `yyyy-MM-dd`. 파싱 실패 시 null — 안내 문구를 생략한다. */
private fun parseDate(raw: String): LocalDate? =
    try {
        LocalDate.parse(raw)
    } catch (e: DateTimeParseException) {
        null
    }
