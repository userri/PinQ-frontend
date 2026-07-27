package com.finq.app.ui.library

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.finq.app.R
import androidx.compose.ui.tooling.preview.Preview
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.ReviewStatus
import com.finq.app.ui.theme.FinQTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextSecondary

/**
 * 카드 상단에서 배지 형태로 강조할 요소 — 화면당 정확히 하나.
 *
 * 카테고리·상태·복습이 모두 같은 pill 이면 위계가 0이 되므로, 화면에서 판별력을 갖는
 * 하나만 배지로 올리고 나머지는 질문 아래 메타 한 줄(배경 없는 보조 텍스트)로 내린다.
 */
enum class AttemptCardEmphasis {
    /** 정답·오답이 섞여 상태가 판별 정보인 화면(전체이력·북마크). 카테고리는 메타로 강등. */
    STATUS,

    /** 모든 항목이 오답인 화면(오답노트) — "오답" 배지는 정보량이 0이라 뺀다. */
    CATEGORY,
}

/** 메타 한 줄의 조각. [accent] 인 조각만 Lime 포인트를 받는다(카드당 최대 1개). */
private data class MetaPart(val text: String, val accent: Boolean = false)

/**
 * 오답노트 / 북마크 / 전체이력 화면이 공통으로 사용하는 항목 카드.
 *
 *  - 클릭 시 펼침/접힘 토글
 *  - 우상단에 북마크 ⭐ 토글 버튼
 *  - 펼쳤을 때 내 답 / 정답 / 해설 / 키워드 / 관련 기사 노출
 */
@Composable
fun AttemptItemCard(
    item: AttemptItem,
    onToggleBookmark: () -> Unit,
    /** 상단 배지로 무엇을 강조할지 — 호출 화면이 결정한다. [AttemptCardEmphasis] */
    emphasis: AttemptCardEmphasis = AttemptCardEmphasis.STATUS,
    /** 미풀이 북마크를 탭했을 때 풀이 화면으로 보내는 콜백. null 이면 탭해도 아무 일 없음. */
    onStartQuiz: (() -> Unit)? = null,
    /** 정원 딥링크로 진입한 카드 — 처음부터 펼쳐 보여준다. */
    initialExpanded: Boolean = false,
    /**
     * 상세 지연 로드. 목록이 요약(선택지·해설·기사 없음)만 줄 때, 펼치는 순간
     * 이 콜백으로 단건 상세를 가져온다. null 이면 [item] 이 이미 전체 데이터라
     * 보고 곧바로 렌더한다(프리뷰·Showcase·구서버 전체응답 경로).
     */
    onLoadDetail: (suspend (Long) -> AttemptItem)? = null,
) {
    // rememberSaveable — LazyColumn 이 카드를 스크롤로 폐기했다 되살려도 펼침 상태 보존
    // (items(key = quizId) 덕에 항목별로 저장/복원된다).
    var expanded by rememberSaveable(item.quizId) { mutableStateOf(initialExpanded) }
    // 미리 연습 로컬 상태 — 서버/졸업과 완전 분리. 선택한 선지 id, 없으면 미채점.
    var practiceOpen by rememberSaveable(item.quizId) { mutableStateOf(false) }
    var practicePick by rememberSaveable(item.quizId) { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    val dateStr = remember(item.solvedAtIso) { formatSolvedDate(item.solvedAtIso) }

    // 상세 지연 로드 상태 — quizId 로 키잉해 카드 재사용 시 초기화.
    var detail by remember(item.quizId) { mutableStateOf<AttemptItem?>(null) }
    var detailLoading by remember(item.quizId) { mutableStateOf(false) }
    var detailError by remember(item.quizId) { mutableStateOf<String?>(null) }
    var retryTick by remember(item.quizId) { mutableStateOf(0) }

    // 지연 로드가 필요한 경로인지 — 로더가 있고, 푼 문제이고, 목록이 요약(선택지 없음)일 때만.
    // 목록이 아직 무거운 필드를 통째로 주는 시기엔 이미 item 에 상세가 있으므로 재요청하지 않는다
    // (백엔드가 목록에서 무거운 필드를 제거하면 choices 가 비어 자동으로 지연 로드가 켜진다).
    val needsDetailLoad = onLoadDetail != null && !item.unsolved && item.choices.isEmpty()
    // 펼쳤을 때 heavy 필드는 상세를 우선 사용(없으면 요약 item — Showcase/구서버 경로).
    val effective = detail ?: item
    // 상세를 아직 받는 중이라 본문을 아직 못 그리는 상태.
    val showDetailLoading = detailLoading ||
        (needsDetailLoad && detail == null && detailError == null)

    LaunchedEffect(expanded, item.quizId, retryTick) {
        if (expanded && needsDetailLoad && detail == null && onLoadDetail != null) {
            detailLoading = true
            detailError = null
            runCatching { onLoadDetail(item.quizId) }
                .onSuccess { detail = it; detailLoading = false }
                .onFailure {
                    detailError = it.message ?: "불러오지 못했어요"
                    detailLoading = false
                }
        }
    }

    val hasArticle = effective.article != null
        && !effective.article.url.isBlank()
        && !effective.article.title.isBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    // 미풀이 문제는 펼칠 내용(정답/해설)이 서버에서 마스킹돼 없다 → 풀이로 보낸다.
                    if (item.unsolved) onStartQuiz?.invoke()
                    else expanded = !expanded
                },
        ) {
            // 상단 행: 강조 배지 하나  ····  날짜 + ⭐ 북마크
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (emphasis) {
                    // 오답노트 — 카테고리가 유일한 판별 정보. 분류이므로 중립 태그 스타일.
                    AttemptCardEmphasis.CATEGORY -> Surface(
                        shape = RoundedCornerShape(50),
                        color = BgSubtle,
                    ) {
                        Text(
                            text = item.categoryDisplay,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    // 전체이력·북마크 — 정답/오답이 항목을 가르는 정보. 상태이므로 유채색 배지.
                    AttemptCardEmphasis.STATUS -> {
                        val (badgeText, badgeBg, badgeFg) = when {
                            item.unsolved -> Triple("아직 안 푼 문제", BgSubtle, TextSecondary)
                            item.correct -> Triple("정답", Grass1, Lime)
                            else -> Triple(
                                "오답",
                                MaterialTheme.colorScheme.errorContainer,
                                MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        Surface(shape = RoundedCornerShape(50), color = badgeBg) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeFg,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                if (dateStr != null) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(2.dp))
                }

                // 북마크 버튼 — 카드 클릭과 분리
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(32.dp),
                ) {
                    Image(
                        painter = painterResource(
                            if (item.bookmarked) R.drawable.ic_bookmark_star_filled
                            else R.drawable.ic_bookmark_star,
                        ),
                        contentDescription = "북마크",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )

            // 성장 근접 스트립 — 복습중(자라는) 오답만. 졸업/legacy 는 growthStrip 이 null.
            // 밀도를 줄이려 "관련될 때만" 노출한다: 오늘 물 줄 수 있거나(dueToday)
            // 졸업 임박(finalStage)일 때만. 중간 단계·먼 미래는 메타 줄의 "물 N/3" 로 갈음하고 숨김.
            val strip = item.review?.let { review ->
                growthStrip(
                    stage = review.stage,
                    graduated = review.graduated,
                    dueDateIso = review.dueDateIso,
                    today = LocalDate.now(),
                )
            }?.takeIf { it.dueToday || it.finalStage }

            // 메타 한 줄 — 배경 없는 보조 텍스트, 가운뎃점 구분. 배지로 올라간 요소는 여기서 뺀다.
            val metaParts = buildList {
                // 상태를 강조한 화면에선 카테고리를 테두리·배경 없는 회색 텍스트로 강등.
                if (emphasis == AttemptCardEmphasis.STATUS) add(MetaPart(item.categoryDisplay))
                // 오답노트에선 모든 항목이 오답이라 "오답"은 정보량 0 — 미풀이만 알린다.
                if (emphasis == AttemptCardEmphasis.CATEGORY && item.unsolved) {
                    add(MetaPart("아직 안 푼 문제"))
                }
                // 스트립이 뜨는 카드는 "N/3단계"가 같은 진행을 이미 말한다 → 중복 제거.
                if (strip == null) {
                    item.review?.let { review ->
                        // "물 N번"은 진행감이 없다(좋은 건지 나쁜 건지 모름) → 분모로 목표를 보여준다.
                        // 3번 맞히면 졸업(ReviewRepository) — WaterGrassCard 문구와 같은 3.
                        add(
                            if (review.graduated) MetaPart("나무 완성", accent = true)
                            else MetaPart("물 ${review.waterCount.coerceAtMost(3)}/3"),
                        )
                    }
                }
            }

            if (metaParts.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    metaParts.forEachIndexed { index, part ->
                        if (index > 0) {
                            Text(
                                text = " · ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                        Text(
                            text = part.text,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (part.accent) Lime else TextSecondary,
                            fontWeight = if (part.accent) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    }
                }
            }

            if (strip != null) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strip.stageText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        // 마지막 단계만 Lime 포인트, 그 외 중립.
                        color = if (strip.finalStage) Lime else TextSecondary,
                    )
                    if (strip.dueText != null) {
                        Text(
                            text = "  ·  ${strip.dueText}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (strip.dueToday) Lime else TextMuted,
                            fontWeight = if (strip.dueToday) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }

            if (!expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (item.unsolved) "풀러 가기" else "자세히 보기",
                    style = MaterialTheme.typography.labelSmall,
                    color = Lime,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (expanded && showDetailLoading) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (expanded && !showDetailLoading && detailError != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "자세히 불러오지 못했어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "다시 시도",
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable { retryTick++ },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Lime,
                )
            }

            if (expanded && !showDetailLoading && detailError == null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))

                AnswerRow(
                    label = if (effective.correct) "내 답 (정답)" else "내 답",
                    text = effective.myAnswerText,
                    isCorrect = effective.correct,
                )

                if (!effective.correct) {
                    Spacer(Modifier.height(6.dp))
                    AnswerRow(label = "정답", text = effective.correctAnswerText, isCorrect = true)
                }

                if (effective.explanation.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "해설",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = effective.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (!effective.keyword.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "키워드  ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BgSubtle,
                        ) {
                            Text(
                                text = effective.keyword,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Lime,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // ── 미리 연습 (순수 연습 · 물주기와 무관) ──────────────────
                if (!item.unsolved) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))

                    if (!practiceOpen) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BgSubtle,
                            modifier = Modifier.clickable {
                                practiceOpen = true
                                practicePick = null
                            },
                        ) {
                            Text(
                                text = "미리 연습 (물주기 아님)",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Lime,
                            )
                        }
                    } else {
                        Text(
                            text = "연습은 나무 성장에 반영되지 않아요. 물은 예정일에 복습으로 줄 수 있어요.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        effective.choices.forEach { option ->
                            val picked = practicePick == option.id
                            val isAnswer = option.id == effective.correctChoiceId
                            // 선택 후에만 정답/오답 색을 드러낸다.
                            val bg = when {
                                practicePick == null -> BgSubtle
                                isAnswer -> Grass1
                                picked -> MaterialTheme.colorScheme.errorContainer
                                else -> BgSubtle
                            }
                            val fg = when {
                                practicePick == null -> MaterialTheme.colorScheme.onSurface
                                isAnswer -> Lime
                                picked -> MaterialTheme.colorScheme.onErrorContainer
                                else -> TextMuted
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .clickable(enabled = practicePick == null) { practicePick = option.id },
                            ) {
                                Text(
                                    text = option.text,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = fg,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        if (practicePick != null) {
                            val correct = isPracticeCorrect(practicePick!!, effective.correctChoiceId)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (correct) "정답이에요 (연습이라 물은 안 줬어요)"
                                       else "오답이에요 · 예정일에 복습으로 다시 만나요",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (correct) Lime else MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = "다시 연습",
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable { practicePick = null },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Lime,
                            )
                        }
                    }
                }

                if (hasArticle) {
                    val article = effective.article!!
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        context,
                                        "기사를 열 수 있는 앱이 없어요",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "관련 기사",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Lime,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (article.source.isNotBlank()) {
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = article.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerRow(label: String, text: String, isCorrect: Boolean) {
    val bgColor = if (isCorrect) Grass1 else MaterialTheme.colorScheme.errorContainer
    val textColor = if (isCorrect) Lime else MaterialTheme.colorScheme.onErrorContainer

    Row(verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(
                if (isCorrect) R.drawable.ic_check_circle else R.drawable.ic_x_circle,
            ),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 1.dp, end = 6.dp)
                .size(16.dp),
        )
        Text(
            text = "$label  ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp),
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bgColor,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * 오답노트 카드 3상태 미리보기.
 *  - 복습중(오답노트 강조=카테고리): stage 1, 미래 due → 메타 "물 1/3"
 *  - 졸업(전체이력 강조=상태): graduated=true → 메타 "카테고리 · 나무 완성"
 *  - legacy: review=null → 복습 메타 없음
 */
@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun AttemptItemCardPreview() {
    // 인라인 더미 선지 (미리 연습·정답 판별용)
    val dummyChoices = listOf(
        QuizOption(id = 1L, optionNumber = 1, text = "기준금리를 올린다"),
        QuizOption(id = 2L, optionNumber = 2, text = "기준금리를 내린다"),
        QuizOption(id = 3L, optionNumber = 3, text = "지급준비율을 낮춘다"),
        QuizOption(id = 4L, optionNumber = 4, text = "국채를 매입한다"),
    )
    fun dummy(review: ReviewStatus?) = AttemptItem(
        quizId = review?.stage?.toLong() ?: 99L,
        category = Category.selectable.first(),
        question = "물가가 계속 오를 때 중앙은행이 취하는 대표적 정책은?",
        choices = dummyChoices,
        selectedChoiceId = 2L,
        correctChoiceId = 1L,
        correct = false,
        explanation = "물가 상승을 억제하려면 기준금리를 올려 시중 유동성을 줄인다.",
        keyword = "기준금리",
        article = null,
        bookmarked = true,
        solvedAtIso = null,
        review = review,
    )

    FinQTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 복습중 — stage 1, 미래 due
            AttemptItemCard(
                item = dummy(
                    ReviewStatus(
                        stage = 1,
                        waterCount = 1,
                        absorbedCount = 1,
                        graduated = false,
                        dueDateIso = LocalDate.now().plusDays(3).toString(),
                    ),
                ),
                emphasis = AttemptCardEmphasis.CATEGORY,
                onToggleBookmark = {},
            )
            // 졸업 — 다 키운 나무
            AttemptItemCard(
                item = dummy(
                    ReviewStatus(
                        stage = 3,
                        waterCount = 3,
                        absorbedCount = 3,
                        graduated = true,
                        dueDateIso = null,
                    ),
                ),
                onToggleBookmark = {},
            )
            // legacy — 복습 이력 없음
            AttemptItemCard(
                item = dummy(null),
                onToggleBookmark = {},
            )
        }
    }
}

/** "2026-05-18T14:23:00" → "5/18" (오늘이면 "오늘"). 파싱 실패 시 null. */
private fun formatSolvedDate(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        val dt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (dt.toLocalDate().isEqual(LocalDate.now())) "오늘"
        else dt.format(DateTimeFormatter.ofPattern("M/d"))
    }.getOrNull()
}
