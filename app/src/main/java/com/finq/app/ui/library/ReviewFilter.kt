package com.finq.app.ui.library

import com.finq.app.data.model.AttemptItem

/**
 * 오답노트 복습 상태 필터 — 옛 정원 목록 기능의 이관처.
 *
 *  - 오답만: 아직 복습 큐에 오르지 않은 오답 (review == null)
 *  - 복습중: 물 주는 중 (자라는 새싹/풀/나무직전)
 *  - 졸업: 다 키운 나무 — 복습 큐에 다시 나오지 않는다
 */
enum class ReviewFilter(val label: String) {
    ALL("전체"),
    NOT_STARTED("오답만"),
    GROWING("복습중"),
    GRADUATED("졸업🌳"),
}

fun List<AttemptItem>.applyReviewFilter(filter: ReviewFilter): List<AttemptItem> = when (filter) {
    ReviewFilter.ALL -> this
    ReviewFilter.NOT_STARTED -> filter { it.review == null }
    ReviewFilter.GROWING -> filter { it.review != null && !it.review.graduated }
    ReviewFilter.GRADUATED -> filter { it.review?.graduated == true }
}
