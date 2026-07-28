package com.finq.app.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.ReviewStatus
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 행 왼쪽 첫 조각으로 무엇을 세울지 — 화면당 정확히 하나.
 *
 * 카테고리·상태가 모두 같은 세기로 붙으면 위계가 0이 되므로, 그 화면에서 판별력을
 * 갖는 쪽을 앞에 세운다.
 */
enum class AttemptCardEmphasis {
    /** 정답·오답이 섞여 상태가 판별 정보인 화면(전체이력·북마크). 카테고리는 뒤로. */
    STATUS,

    /** 모든 항목이 오답인 화면(오답노트) — "오답"은 정보량이 0이라 카테고리만 세운다. */
    CATEGORY,
}

// ─────────────────────────────────────────────────────────────────────────────
// 이 행의 시각 언어
//
// 목록은 색인이지 콘텐츠 컨테이너가 아니다. 카드(면 + 테두리 + 그림자)를 씌우면
// 한 화면에 한두 개밖에 못 들어와 훑을 수가 없다 → 면 없이 구분선으로만 가른다.
//
// 여기 들어오는 것: 무엇에 관한 문제인가(카테고리 또는 정답/오답) · 문제 · 언제 · 북마크.
// 여기 들어오지 않는 것: 진척(단계·물 준 횟수·예정일). 얼마나 자랐는지는 정원이 보여준다.
// 졸업만 나무 아이콘 하나로 구분한다 — "끝난 것"은 훑을 때 걸러야 하는 정보라서.
// ─────────────────────────────────────────────────────────────────────────────

/** 졸업 나무 아이콘 크기 — 20dp 미만은 획이 뭉개져 형태를 못 읽는다(Material 광학 최소). */
private val GraduatedIconSize = 20.dp

/**
 * 오답노트 / 북마크 / 전체이력 목록의 항목 한 줄.
 *
 * 행 전체가 상세 화면 진입 대상이다("자세히 보기" 같은 별도 링크를 두지 않는다).
 * 아직 안 푼 북마크만 예외로 풀이 화면([onStartQuiz])으로 보낸다 — 정답·해설이
 * 서버에서 마스킹돼 상세에 보여줄 게 없기 때문이다.
 */
@Composable
fun AttemptItemRow(
    item: AttemptItem,
    onToggleBookmark: () -> Unit,
    /** 행 탭 → 상세 화면. 미풀이 항목은 [onStartQuiz] 가 우선한다. */
    onOpenDetail: () -> Unit,
    /** 무엇을 앞에 세울지 — 호출 화면이 결정한다. [AttemptCardEmphasis] */
    emphasis: AttemptCardEmphasis = AttemptCardEmphasis.STATUS,
    /** 미풀이 북마크를 탭했을 때 풀이 화면으로 보내는 콜백. null 이면 상세로 간다. */
    onStartQuiz: (() -> Unit)? = null,
) {
    val dateStr = remember(item.solvedAtIso) { formatSolvedDate(item.solvedAtIso) }
    val graduated = item.review?.graduated == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.unsolved && onStartQuiz != null) onStartQuiz() else onOpenDetail()
            }
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (graduated) {
                    Image(
                        painter = painterResource(R.drawable.ic_stage_tree),
                        contentDescription = "나무 완성",
                        modifier = Modifier.size(GraduatedIconSize),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                LeadLabel(item = item, emphasis = emphasis)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(8.dp))

        if (dateStr != null) {
            Text(
                text = dateStr,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }

        // 북마크 버튼 — 행 클릭과 분리
        IconButton(
            onClick = onToggleBookmark,
            modifier = Modifier.size(32.dp),
        ) {
            Image(
                painter = painterResource(
                    if (item.bookmarked) R.drawable.ic_bookmark_star_filled
                    else R.drawable.ic_bookmark_star,
                ),
                contentDescription = if (item.bookmarked) "북마크 해제" else "북마크",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * 행 첫 줄의 라벨 — 면 없는 글자 한 줄.
 *
 * 상태 화면에선 "정답 · 금리"처럼 판별 정보를 앞에, 분류를 뒤에 둔다. 색은 글자가
 * 이미 말하는 것을 되풀이하는 보조 신호일 뿐이라, 색을 못 봐도 뜻이 통한다.
 */
@Composable
private fun LeadLabel(item: AttemptItem, emphasis: AttemptCardEmphasis) {
    when (emphasis) {
        AttemptCardEmphasis.CATEGORY -> Text(
            text = item.categoryDisplay,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
        )

        AttemptCardEmphasis.STATUS -> Row(verticalAlignment = Alignment.CenterVertically) {
            val (statusText, statusColor) = when {
                item.unsolved -> "아직 안 푼 문제" to TextSecondary
                item.correct -> "정답" to Lime
                else -> "오답" to Error
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor,
            )
            Text(
                text = "  ·  ",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Text(
                text = item.categoryDisplay,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
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

/** "2026-05-18T14:23:00" → "5/18" (오늘이면 "오늘"). 파싱 실패 시 null. */
internal fun formatSolvedDate(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        val dt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (dt.toLocalDate().isEqual(LocalDate.now())) "오늘"
        else dt.format(DateTimeFormatter.ofPattern("M/d"))
    }.getOrNull()
}

/**
 * 목록 행 미리보기 — 밀도(한 화면에 몇 개 들어오는가)를 눈으로 확인하는 용도.
 */
@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun AttemptItemRowPreview() {
    val dummyChoices = listOf(
        QuizOption(id = 1L, optionNumber = 1, text = "기준금리를 올린다"),
        QuizOption(id = 2L, optionNumber = 2, text = "기준금리를 내린다"),
    )
    fun dummy(review: ReviewStatus?, correct: Boolean = false) = AttemptItem(
        quizId = (review?.stage?.toLong() ?: 99L) + if (correct) 100L else 0L,
        category = Category.selectable.first(),
        question = "물가가 계속 오를 때 중앙은행이 취하는 대표적 정책은 무엇인가?",
        choices = dummyChoices,
        selectedChoiceId = 2L,
        correctChoiceId = 1L,
        correct = correct,
        explanation = "물가 상승을 억제하려면 기준금리를 올려 시중 유동성을 줄인다.",
        keyword = "기준금리",
        article = null,
        bookmarked = true,
        solvedAtIso = null,
        review = review,
    )

    FinQTheme {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            AttemptItemRow(
                item = dummy(ReviewStatus(1, 1, 1, false, null)),
                emphasis = AttemptCardEmphasis.CATEGORY,
                onToggleBookmark = {},
                onOpenDetail = {},
            )
            AttemptItemRow(
                item = dummy(ReviewStatus(3, 3, 3, true, null), correct = true),
                onToggleBookmark = {},
                onOpenDetail = {},
            )
            AttemptItemRow(
                item = dummy(null),
                onToggleBookmark = {},
                onOpenDetail = {},
            )
        }
    }
}
