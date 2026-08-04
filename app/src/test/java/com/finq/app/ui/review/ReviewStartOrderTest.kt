package com.finq.app.ui.review

import com.finq.app.data.repository.ReviewItem
import com.finq.app.data.repository.ReviewStage
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 정원에서 빛나는 식물을 눌러 들어오면 그 문제부터 시작해야 한다.
 * 큐에 없을 때(자정을 넘겼거나 다른 기기에서 먼저 푼 경우) 순서를 망가뜨리지 않는 것도 함께 본다.
 */
class ReviewStartOrderTest {

    private fun item(id: Long) = ReviewItem(
        quizId = id,
        categoryLabel = "금리",
        question = "q$id",
        options = emptyList(),
        stage = ReviewStage.SPROUT,
        dueDate = null,
    )

    private val queue = listOf(item(10), item(20), item(30))

    @Test
    fun `지정한 문제를 맨 앞으로 옮기고 나머지 순서는 유지한다`() {
        val r = queue.startingWith(30L)
        assertEquals(listOf(30L, 10L, 20L), r.map { it.quizId })
    }

    @Test
    fun `이미 맨 앞이면 그대로 둔다`() {
        assertEquals(listOf(10L, 20L, 30L), queue.startingWith(10L).map { it.quizId })
    }

    @Test
    fun `큐에 없으면 큐 처음부터 - 순서를 건드리지 않는다`() {
        assertEquals(listOf(10L, 20L, 30L), queue.startingWith(999L).map { it.quizId })
    }

    @Test
    fun `지정이 없으면 원본 그대로`() {
        assertEquals(listOf(10L, 20L, 30L), queue.startingWith(null).map { it.quizId })
    }

    @Test
    fun `빈 큐는 빈 채로 - 호출부가 상세 열람으로 되돌린다`() {
        assertEquals(emptyList<Long>(), emptyList<ReviewItem>().startingWith(30L).map { it.quizId })
    }
}
