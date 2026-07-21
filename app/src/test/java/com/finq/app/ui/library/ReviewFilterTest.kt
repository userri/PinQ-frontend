package com.finq.app.ui.library

import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.ReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewFilterTest {

    private fun item(quizId: Long, review: ReviewStatus?) = AttemptItem(
        quizId = quizId, category = Category.selectable.first(), question = "q",
        choices = emptyList(), selectedChoiceId = null, correctChoiceId = 1L,
        correct = false, explanation = "", keyword = null, article = null,
        bookmarked = false, solvedAtIso = null, review = review,
    )

    // review == null: 복습 기능 배포 이전 레거시 오답 (신규 오답은 즉시 복습 큐에 등록됨).
    private val legacy = item(1, review = null)
    private val growing = item(2, ReviewStatus(stage = 1, waterCount = 2, absorbedCount = 1, graduated = false, dueDateIso = null))
    private val graduated = item(3, ReviewStatus(stage = 2, waterCount = 5, absorbedCount = 3, graduated = true, dueDateIso = null))
    private val all = listOf(legacy, growing, graduated)

    @Test
    fun `전체는 그대로 - 레거시 오답 포함`() = assertEquals(all, all.applyReviewFilter(ReviewFilter.ALL))

    @Test
    fun `복습중 - 진입했고 미졸업`() =
        assertEquals(listOf(growing), all.applyReviewFilter(ReviewFilter.GROWING))

    @Test
    fun `졸업 - graduated true`() =
        assertEquals(listOf(graduated), all.applyReviewFilter(ReviewFilter.GRADUATED))
}
