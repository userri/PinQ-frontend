package com.finq.app.ui.library

import com.finq.app.data.repository.ReviewStage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 오답노트 카드 "성장 스트립" 표시 데이터.
 *
 *  - [stageIconRes]/[stageText] : 단계 아이콘 + "2/3단계" — 나무까지 근접성(goal-gradient).
 *  - [dueText]    : "오늘 물 줄 수 있어요" / "D-3 · 7/25 물 주기" / null(시점 불명).
 *  - [dueToday]   : due 가 오늘 이하 — 오늘의 복습으로 물 줄 수 있음(CTA 강조용).
 *  - [finalStage] : 다음에 맞히면 졸업하는 마지막 단계 — Lime 포인트 1개만.
 */
data class GrowthStrip(
    val stageIconRes: Int,
    val stageText: String,
    val dueText: String?,
    val dueToday: Boolean,
    val finalStage: Boolean,
)

private val DUE_MONTH_DAY = DateTimeFormatter.ofPattern("M/d")

/**
 * 복습중(자라는) 오답의 성장 스트립. 졸업이면 null(스트립 없음).
 *
 * 실제 간격을 앞당기는 정보는 담지 않는다 — 근접성과 "예정된 다음 시점"만 보여준다.
 */
fun growthStrip(stage: Int, graduated: Boolean, dueDateIso: String?, today: LocalDate): GrowthStrip? {
    if (graduated) return null

    val clamped = stage.coerceIn(0, 2)
    val stageIconRes = ReviewStage.of(clamped).iconRes
    val stageText = "${clamped + 1}/3단계"
    val finalStage = clamped == 2

    val due = dueDateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val (dueText, dueToday) = when {
        due == null -> null to false
        !due.isAfter(today) -> "오늘 물 줄 수 있어요" to true
        else -> {
            val days = ChronoUnit.DAYS.between(today, due)
            "D-$days · ${due.format(DUE_MONTH_DAY)} 물 주기" to false
        }
    }
    return GrowthStrip(stageIconRes = stageIconRes, stageText = stageText, dueText = dueText, dueToday = dueToday, finalStage = finalStage)
}

/** 미리 연습 로컬 채점. 정답 정보가 마스킹(null)이면 채점 불가 → 오답 취급(연습 버튼 자체가 숨겨짐). */
fun isPracticeCorrect(selectedChoiceId: Long, correctChoiceId: Long?): Boolean =
    correctChoiceId != null && selectedChoiceId == correctChoiceId
