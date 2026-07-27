package com.finq.app.ui.library

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
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

// ─────────────────────────────────────────────────────────────────────────────
// 이 카드의 시각 언어 (채점 화면 QuizAnswerScreen 과 같은 규칙, 밀도만 낮춤)
//
// ── 라임의 용도 ──────────────────────────────────────────────────────────────
//   ① 배경 없는 라임 '글자' = 누를 수 있는 것.  (자세히 보기 / 미리 연습 / 다시 연습 / 다시 시도)
//   ② 라임 글자 + Grass1 틴트 면 또는 ✓ 글리프 = 정답. 면·글리프가 늘 같이 온다.
//   상태 정보("오늘 물 줄 수 있어요")는 라임 글자를 쓰지 않는다 → 라임 점(형태) + 중립 글자.
//   즉 "배경 없는 라임 글자"를 보면 언제나 누를 수 있다.
//
// ── 면(surface) 위계 — 펼친 카드 안에서 최대 2종 ─────────────────────────────
//   L1 Grass1 틴트 (카드당 1개, 정답에만)  ·  L2 BgSubtle 중립 (내 답·연습 선지·기사)
//   L3 면 없음, 타이포만 (해설 · 알아두면 좋아요 — 둘은 완전히 같은 모양이어야 한다)
//   오답에는 면을 주지 않는다. 오답 신호는 ✗ 글리프 + "오답" 글자 + Error 색 3중 인코딩.
//
// ── "내 답 / 정답 / 오답" 낱말의 자리와 뜻 (같은 말이 두 번 나오지 않게) ──────
//   왼쪽 소제목   = 이 값이 무엇인가        "내 답" · "정답"
//   오른쪽 마커   = 내 답이 맞았는가        ✓ "정답" · ✗ "오답"  → "내 답" 블록에만 붙는다
//   연습 선지 라벨 = 네 선지 중 무엇인가     "정답" · "내 선택"   (채점 결과가 아니라 지목)
//   따라서 ✓/✗ 글리프는 카드 안에서 언제나 "내 답 채점 결과" 하나만 뜻한다.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 펼친 상세의 단일 좌측선.
 *
 * 소제목 / 값 카드 안의 글자 / 해설 / 키워드 / 기사 제목이 모두 이 x 에서 시작한다.
 * 값 카드·기사 카드는 면을 카드 폭 끝까지 깔고 안쪽 패딩을 같은 값으로 줘서
 * "면의 왼쪽"이 아니라 "글자의 왼쪽"이 맞도록 한다.
 */
private val DetailInset = 12.dp

/** 메타 한 줄의 조각. [strong] 인 조각만 밝기·굵기를 올린다(색은 쓰지 않는다). */
private data class MetaPart(val text: String, val strong: Boolean = false)

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
                    // 전체이력·북마크 — 정답/오답이 항목을 가르는 정보.
                    // 강조(틴트 면)는 정답만 갖는다. 오답은 중립 면 + Error 글자 — 면으로 때리지 않는다.
                    AttemptCardEmphasis.STATUS -> {
                        val (badgeText, badgeBg, badgeFg) = when {
                            item.unsolved -> Triple("아직 안 푼 문제", BgSubtle, TextSecondary)
                            item.correct -> Triple("정답", Grass1, Lime)
                            else -> Triple("오답", BgSubtle, Error)
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
                            if (review.graduated) MetaPart("나무 완성", strong = true)
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
                            // 라임은 '누를 수 있는 것' 전용 — 상태는 밝기·굵기로만 올린다.
                            color = if (part.strong) TextPrimary else TextSecondary,
                            fontWeight = if (part.strong) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }

            if (strip != null) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // "지금 할 수 있다"는 상태 신호 — 라임 '점'(형태)으로 말하고 글자는 중립으로 둔다.
                    // 라임 글자로 쓰면 바로 아래 "자세히 보기"(액션)와 같은 종류로 읽힌다.
                    if (strip.dueToday) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Lime),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = listOfNotNull(strip.stageText, strip.dueText).joinToString("  ·  "),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (strip.dueToday || strip.finalStage) TextPrimary else TextSecondary,
                        fontWeight = if (strip.dueToday) FontWeight.Bold else FontWeight.SemiBold,
                    )
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
                    modifier = Modifier.padding(start = DetailInset),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "다시 시도",
                    modifier = Modifier
                        .padding(start = DetailInset, top = 6.dp)
                        .clickable { retryTick++ },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Lime,
                )
            }

            if (expanded && !showDetailLoading && detailError == null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                // 정답이면 "내 답"이 곧 정답이라 블록 하나로 끝난다 → L1(틴트)은 언제나 카드에 1개.
                // 채점 마커(✓/✗)는 "내 답" 블록에만 — 아래 "정답" 블록은 소제목이 이미 정답이라 말한다.
                AnswerBlock(
                    label = "내 답",
                    value = effective.myAnswerText,
                    correct = effective.correct,
                    verdict = effective.correct,
                )

                if (!effective.correct) {
                    Spacer(Modifier.height(8.dp))
                    AnswerBlock(label = "정답", value = effective.correctAnswerText, correct = true)
                }

                if (effective.explanation.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    SectionLabel("해설")
                    Spacer(Modifier.height(4.dp))
                    // L3 — 면 없이 타이포만. 좌측선은 값 카드 글자와 같다.
                    Text(
                        text = effective.explanation,
                        modifier = Modifier.padding(horizontal = DetailInset),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (!effective.keyword.isNullOrBlank()) {
                    // 서버 keyword 는 "용어 — 설명" 한 필드다. 용어는 소제목 줄에 얹고 설명만
                    // 본문으로 내려서, 해설과 완전히 같은 "소제목 한 줄 + 본문" 모양을 만든다.
                    // (용어를 본문 앞에 붙이면 설명과 뭉쳐서 어디까지가 용어인지 안 보인다.)
                    val (term, description) = splitKeyword(effective.keyword)
                    Spacer(Modifier.height(12.dp))
                    // 채점 화면과 같은 낱말을 쓴다 — 같은 필드를 거기선 "알아두면 좋아요"로 부른다.
                    SectionLabel("알아두면 좋아요", term = term.takeIf { description != null })
                    Spacer(Modifier.height(4.dp))
                    Text(
                        // 설명이 없으면(짧은 용어만 온 데이터) 용어 자체가 본문이 된다.
                        text = description ?: term ?: effective.keyword,
                        modifier = Modifier.padding(horizontal = DetailInset),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // ── 미리 연습 (순수 연습 · 물주기와 무관) ──────────────────
                if (!item.unsolved) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))

                    if (!practiceOpen) {
                        // 진입은 배경 없는 라임 글자 = 누를 수 있는 것. 괄호 부연은 연 뒤에 한 줄로.
                        Text(
                            text = "미리 연습",
                            modifier = Modifier
                                .padding(start = DetailInset)
                                .clickable {
                                    practiceOpen = true
                                    practicePick = null
                                },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Lime,
                        )
                    } else {
                        Text(
                            text = "연습은 나무 성장에 반영되지 않아요",
                            modifier = Modifier.padding(start = DetailInset),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        effective.choices.forEach { option ->
                            PracticeOptionRow(
                                option = option,
                                revealed = practicePick != null,
                                isAnswer = option.id == effective.correctChoiceId,
                                picked = practicePick == option.id,
                                onClick = { practicePick = option.id },
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        if (practicePick != null) {
                            val correct = isPracticeCorrect(practicePick!!, effective.correctChoiceId)
                            Spacer(Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.padding(start = DetailInset),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ResultGlyph(correct)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (correct) "정답이에요" else "오답이에요",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (correct) Lime else Error,
                                )
                            }
                            Text(
                                text = "다시 연습",
                                modifier = Modifier
                                    .padding(start = DetailInset, top = 8.dp)
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
                    // 라벨은 소제목 아래 패턴으로 통일 — 면 밖에 둔다.
                    SectionLabel("관련 기사")
                    Spacer(Modifier.height(4.dp))
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
                        // L2 중립 면 — 값 카드와 같은 한 종류.
                        color = BgSubtle,
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = DetailInset,
                                vertical = 10.dp,
                            ),
                        ) {
                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (article.source.isNotBlank()) {
                                    Text(
                                        text = article.source,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                // 외부 브라우저로 나가는 동작 — 라임 글자로 "누를 수 있음"을 말한다.
                                Text(
                                    text = "기사 열기",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Lime,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 펼친 상세의 조각들 — 모두 [DetailInset] 좌측선을 공유한다.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 소제목. 값·해설·알아두면 좋아요·기사 라벨이 전부 이 하나의 패턴(소제목 → 아래에 내용)을 쓴다.
 *
 * [term] 은 소제목이 가리키는 대상(예: 알아두면 좋아요 · **기준금리**). 밝기 한 단만 올리고
 * 색은 쓰지 않는다 — 여전히 라벨 줄이지 내용이 아니다.
 */
@Composable
private fun SectionLabel(text: String, term: String? = null) {
    Row(
        modifier = Modifier.padding(start = DetailInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
        )
        if (term != null) {
            Text(
                text = "  ·  ",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Text(
                text = term,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
            )
        }
    }
}

/**
 * 채점 글리프 — 색 + 모양 2중. 항상 "정답"/"오답" 글자와 함께 쓴다(색 단독 금지).
 * 뜻은 하나뿐이다: **내가 낸 답이 맞았는가.** 정답을 '지목'하는 자리엔 쓰지 않는다.
 */
@Composable
private fun ResultGlyph(correct: Boolean) {
    Image(
        painter = painterResource(
            if (correct) R.drawable.ic_check_circle else R.drawable.ic_x_circle,
        ),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
    )
}

/**
 * 값 블록 — 소제목 줄(왼쪽) + 채점 마커(오른쪽), 그 아래 값 면.
 *
 * 왼쪽 소제목과 오른쪽 마커는 서로 다른 축이라 같은 낱말이 두 번 나오면 안 된다:
 *   · 왼쪽 소제목 = 이 값이 **무엇인가** ("내 답" / "정답")
 *   · 오른쪽 마커 = 내 답이 **맞았는가** (✓ 정답 / ✗ 오답)
 * "정답" 블록은 정의상 맞은 값이라 마커를 달면 같은 말을 두 번 하는 꼴 →
 * [verdict] 를 null 로 줘서 마커를 생략한다. 그래서 ✓/✗ 글리프는 카드 안에서
 * 언제나 "내 답 채점 결과" 한 가지만 뜻한다.
 *
 * 좌측선: 소제목과 값 글자가 같은 x. 마커는 오른쪽 끝의 별도 열이라 좌측선을 밀지 않는다
 * (채점 화면 보기 행의 "정답"/"내 선택" 배지가 앉는 자리와 같다).
 * 강조([correct] 틴트)는 정답만 가져간다 — 오답은 중립 면에 사실만 적는다.
 */
@Composable
private fun AnswerBlock(label: String, value: String, correct: Boolean, verdict: Boolean? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DetailInset),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
            )
            if (verdict != null) {
                Spacer(Modifier.weight(1f))
                ResultGlyph(verdict)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (verdict) "정답" else "오답",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (verdict) Lime else Error,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            // L1 = 정답만 틴트. 오답은 L2 중립 — 빨강 면을 쓰지 않는다.
            color = if (correct) Grass1 else BgSubtle,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = DetailInset, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (correct) Lime else TextPrimary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** 연습 선지 — 채점 화면 보기 행과 같은 언어(번호 원 + 테두리 + 오른쪽 마커), 밀도만 낮춤. */
@Composable
private fun PracticeOptionRow(
    option: QuizOption,
    revealed: Boolean,
    isAnswer: Boolean,
    picked: Boolean,
    onClick: () -> Unit,
) {
    val showCorrect = revealed && isAnswer
    val showPicked = revealed && picked && !isAnswer
    val accent = when {
        showCorrect -> Lime
        showPicked -> Error
        else -> Outline
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            // 채점 화면과 달리 오답 선지에 면을 깔지 않는다 — 틴트는 정답 하나만.
            .background(if (showCorrect) Grass1 else BgSubtle)
            .border(
                width = if (showCorrect || showPicked) 1.5.dp else 1.dp,
                color = accent,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = !revealed, onClick = onClick)
            .padding(horizontal = DetailInset, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (showCorrect || showPicked) accent else BgElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (showCorrect || showPicked) OnLime else TextMuted,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = option.text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = when {
                showCorrect -> Lime
                revealed && !picked -> TextMuted
                else -> TextPrimary
            },
            fontWeight = if (showCorrect || showPicked) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (showCorrect || showPicked) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (showCorrect) "정답" else "내 선택",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

/**
 * 태그로 두를 만한 '용어'의 최대 길이. 이보다 길면 문장으로 보고 본문으로 흘린다.
 * (실제 데이터의 용어는 "금융통화위원회"(7) · "LTV"(3) 처럼 짧다 — 넉넉히 잡되
 * "환율이 오르면 수입 물가가 함께 오른다"(23) 같은 절이 태그가 되지 않을 만큼만.)
 */
private const val KEYWORD_TERM_MAX = 16

/**
 * 서버 keyword("용어 — 설명")를 (용어, 설명) 로 가른다.
 *
 *  - 대시(— – -) 첫 개를 구분자로 본다. 앞이 짧으면 용어, 나머지는 설명.
 *  - 구분자가 없으면 짧을 땐 용어만, 길면 설명만(문장을 태그로 두르지 않는다).
 *  - 앞이 길면 가르지 않고 통째로 설명 — "용어"라 부르기 어려운 문장이라서.
 */
internal fun splitKeyword(keyword: String): Pair<String?, String?> {
    val raw = keyword.trim()
    if (raw.isEmpty()) return null to null

    val cut = raw.indexOfFirst { it == '—' || it == '–' }
        .takeIf { it > 0 }
        ?: raw.indexOf(" - ").takeIf { it > 0 }
    if (cut != null) {
        val term = raw.take(cut).trim()
        val desc = raw.drop(cut + 1).trimStart(' ', '-').trim()
        if (term.isNotEmpty() && term.length <= KEYWORD_TERM_MAX) {
            return term to desc.ifEmpty { null }
        }
        return null to raw
    }
    return if (raw.length <= KEYWORD_TERM_MAX) raw to null else null to raw
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
