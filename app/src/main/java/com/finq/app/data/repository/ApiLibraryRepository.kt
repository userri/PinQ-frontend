package com.finq.app.data.repository

import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.remote.LibraryApi
import com.finq.app.data.remote.dto.ArticleApiResponse
import com.finq.app.data.remote.dto.AttemptItemApiResponse
import com.finq.app.data.remote.dto.ChoiceSummaryApi

/**
 * 백엔드 API 를 사용하는 [LibraryRepository] 구현체.
 *
 * DTO → 도메인 매핑은 이 클래스 안에서만 일어난다.
 */
class ApiLibraryRepository(
    private val api: LibraryApi,
) : LibraryRepository {

    override suspend fun getAttempts(): List<AttemptItem> =
        api.getMyAttempts().map { it.toDomain() }

    override suspend fun getWrongNotes(): List<AttemptItem> =
        api.getMyWrongNotes().map { it.toDomain() }

    override suspend fun getBookmarks(): List<AttemptItem> =
        api.getMyBookmarks().map { it.toDomain() }

    override suspend fun addBookmark(quizId: Long): Boolean =
        api.addBookmark(quizId).bookmarked

    override suspend fun removeBookmark(quizId: Long): Boolean =
        api.removeBookmark(quizId).bookmarked

    // ─── DTO → 도메인 매퍼 ───────────────────────────────────────────────────

    private fun AttemptItemApiResponse.toDomain(): AttemptItem = AttemptItem(
        quizId = quizId,
        category = Category.fromServer(category),
        question = question,
        choices = choices.map { it.toDomain() },
        selectedChoiceId = selectedChoiceId,
        correctChoiceId = correctChoiceId,
        correct = correct,
        explanation = explanation.orEmpty(),
        keyword = keyword,
        article = article?.toDomain(),
        bookmarked = bookmarked,
        solvedAtIso = solvedAt,
    )

    private fun ChoiceSummaryApi.toDomain(): QuizOption = QuizOption(
        id = id,
        optionNumber = orderNum,
        text = content,
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

}
