package com.finq.app.ui.library

import com.finq.app.data.model.AttemptItem

/**
 * 오답노트 복습 상태 필터 — 옛 정원 목록 기능의 이관처.
 *
 *  - 전체: 모든 오답 (배포 이전 레거시 오답 포함)
 *  - 복습중: 물 주는 중 (자라는 새싹/풀/나무직전)
 *  - 졸업: 다 키운 나무 — 복습 큐에 다시 나오지 않는다
 *
 * 신규 오답은 채점 즉시 복습 큐에 등록되므로(백엔드 enqueueWrongAnswer),
 * "복습 큐에 아직 없는 오답"(review == null)은 배포 이전 레거시뿐이라
 * 별도 필터로 두지 않는다 — '전체'에 포함해 보여준다.
 */
enum class ReviewFilter(val label: String, val iconRes: Int? = null) {
    ALL("전체"),
    GROWING("복습중"),
    GRADUATED("졸업", com.finq.app.R.drawable.ic_stage_tree),
}

fun List<AttemptItem>.applyReviewFilter(filter: ReviewFilter): List<AttemptItem> = when (filter) {
    ReviewFilter.ALL -> this
    ReviewFilter.GROWING -> filter { it.review != null && !it.review.graduated }
    ReviewFilter.GRADUATED -> filter { it.review?.graduated == true }
}
