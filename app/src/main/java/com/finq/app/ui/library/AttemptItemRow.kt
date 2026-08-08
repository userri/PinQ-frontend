package com.finq.app.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.QuizOption
import androidx.compose.ui.graphics.ColorFilter
import com.finq.app.data.model.ReviewStatus
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 날짜 열이 무엇을 말하는가 — **화면당 하나**로 고정한다.
 *
 * 목록의 날짜는 그 화면의 **정렬 축과 같은 값**이어야 한다. 다르면 순서가 설명되지
 * 않는다 — 북마크 탭이 실제로 그랬다. 서버 정렬은 담은 순 내림차순인데 화면은
 * 푼 날짜를 찍고 있어서 `8/6 → 5/28 → 5/21 → 7/16` 처럼 뒤죽박죽으로 보였고,
 * 미풀이 항목은 푼 날짜가 없어 아예 빈칸이었다(그 항목들이 맨 위에 몰려 있는 이유도
 * 화면이 말하지 못했다).
 */
enum class AttemptDateAxis {
    /** 푼 날짜(`solvedAtIso`). 오답노트·전체이력. 미풀이 항목은 빈칸이 된다. */
    SOLVED,

    /** 북마크에 담은 날짜(`bookmarkedAtIso`). 북마크 탭 — 서버 정렬 축과 같다. */
    BOOKMARKED,
}

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
// 여기 들어오는 것: 단계 아이콘 · 개념어(제목) · 카테고리 · 날짜 · 북마크 · 셰브론.
//
// 오답노트에서 단계 아이콘을 선두에 세우는 이유는 둘이다.
//  (1) 한 화면에 같은 카테고리가 연속으로 오면(부동산 4연속) 글자만으로는 행이 안
//      갈린다. 단계는 행마다 값이 달라 실제로 변별되는 유일한 축이다. "오답" 뱃지는
//      오답노트에선 전부 같은 값이라 정보량이 0이다.
//  (2) 왼쪽에 아이콘 열이 생기면 목록이 "글자 벽"에서 "물건들의 목록"으로 읽힌다.
// 단계 이름을 글자로 또 쓰지는 않는다 — 아이콘이 이미 말한 걸 되풀이하면 아이콘이
// 장식이 된다(이 앱의 아이콘 규칙). 그래서 날짜를 지울 이유도 없어진다.
//
// 셰브론은 **주 동작에 붙이는 표시**다. 종전엔 행에서 유일하게 눌려 보이는 게 별
// (북마크)이었는데 그건 부차 동작이고, 정작 주 동작인 "탭해서 상세 열기"는 아무
// 표시가 없었다 — "클릭할 수 있어 보이지 않는다"는 실사용 보고의 출처다.
// 마이페이지 NavRow 가 이미 쓰는 글리프라 새 어휘가 아니다.
//
// 여기 들어오지 않는 것: 물 준 횟수·예정일 같은 수치. 얼마나 자랐는지의 세부는
// 정원이 보여준다. 이 목록엔 "지금 어느 단계"만 있으면 된다.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 단계 아이콘 크기. 20dp 는 "이 아래로는 뭉갠다"는 하한이지 적정값이 아니다 —
 * 이 행은 메타줄 + 문제 2줄로 60dp 를 넘으므로, 하한에 붙여 두면 아이콘이 곁다리로
 * 밀린다. 행 높이의 절반 가까이 줘야 목록의 왼쪽 열로 읽힌다.
 */
private val StageIconSize = 30.dp

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
    /** 날짜 열이 말하는 값 — 그 화면의 정렬 축과 같아야 한다. [AttemptDateAxis] */
    dateAxis: AttemptDateAxis = AttemptDateAxis.SOLVED,
) {
    // 북마크 화면에서 담은 날짜가 없는 건 구서버 응답뿐이다. 그때만 푼 날짜로 물러선다
    // — 빈칸보다는 낫고, 두 축이 섞이는 건 그 경우에만이다.
    val dateIso = when (dateAxis) {
        AttemptDateAxis.SOLVED -> item.solvedAtIso
        AttemptDateAxis.BOOKMARKED -> item.bookmarkedAtIso ?: item.solvedAtIso
    }
    val dateStr = remember(dateIso) { formatSolvedDate(dateIso) }
    val graduated = item.review?.graduated == true

    // 단계 아이콘은 오답노트(카테고리 강조) 화면에서만 세운다. 북마크·전체이력은
    // 정답/오답이 실제 변별축이고 미풀이 항목엔 단계 자체가 없다.
    val showsStage = emphasis == AttemptCardEmphasis.CATEGORY
    val stage = if (showsStage) item.review?.stageIcon() else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.unsolved && onStartQuiz != null) onStartQuiz() else onOpenDetail()
            }
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        // 아이콘·셰브론은 행 높이의 세로 가운데에 온다. 문제가 1줄이든 2줄이든
        // 좌우 끝의 두 글리프가 같은 눈높이에 있어야 목록에 가로 기준선이 선다.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 아이콘 자리는 이 화면에서 **항상** 잡아 둔다. review 가 없는 항목만 그림을
        // 비우고 폭은 남긴다 — 있다 없다 하면 행마다 텍스트 시작점이 달라져
        // 목록 왼쪽에 기준선이 서지 않는다(들쭉날쭉하게 읽힘).
        if (showsStage) {
            if (stage != null) {
                Image(
                    painter = painterResource(stage.iconRes),
                    contentDescription = stage.label,
                    modifier = Modifier.size(StageIconSize),
                )
            } else {
                Spacer(Modifier.size(StageIconSize))
            }
            Spacer(Modifier.width(12.dp))
        }

        // 글자 블록의 높이를 **최소치로 고정**한다. 제목만 있는 행(아래 hidesCategory)이
        // 생기면 그 행만 키가 작아져 목록에 계단이 생기는데, 그렇다고 빈 메타줄을
        // 그리면 글자가 위로 치우쳐 보인다(그 행만 위쪽 정렬처럼 읽힌다). 자리를 잡고
        // **가운데 정렬**하면 둘 다 없다.
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = RowTextMinHeight),
            verticalArrangement = Arrangement.Center,
        ) {
            // 제목은 개념어(keyword) 한 줄이다. 질문을 제목으로 쓰면 2줄로도 안 끝나
            // "…"로 잘리는데, 뇌는 잘린 문장을 계속 파싱하려 든다 — 그게 한 화면에
            // 일곱 번 있는 게 "정보가 너무 많이 들어온다"의 실체였다. 문제 자립성
            // 규칙이 들어간 뒤 question 은 앞으로도 계속 길어지므로 시간이 지나도
            // 나아지지 않는다. 개념어는 짧고, 끝나 있고, "뭘 틀렸나"에 곧장 답한다.
            val title = keywordTitle(item.keyword)
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    // 굵기를 뺀다 — 최대 대비(16.3:1) 굵은 글자가 여러 줄 쌓이면
                    // halation 이 생긴다. 한 줄이면 대비는 그대로 둬도 덩어리가 안 진다.
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                // keyword 가 없는 경우는 미풀이 북마크뿐이다 — 안 푼 문제의 개념이
                // 새면 목록이 치팅 경로가 되므로 서버가 마스킹한다. 이때만 질문을
                // 쓰되 한 줄로 자르고 제목보다 한 단계 낮춘다.
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // 제목이 카테고리와 같은 낱말이면("환율" / "환율") 메타줄을 **그리지 않는다** —
            // 같은 말이 두 줄에 겹쳐 찍히면 정보가 아니라 잡음이다.
            //
            // 종전엔 빈 글자로 줄 자리를 남겼는데, 그러면 글자가 위로 치우쳐 그 행만
            // 위쪽 정렬처럼 보였다(실기기 지적). 높이는 위 [RowTextMinHeight] 가 잡고
            // 가운데 정렬이 받으므로, 줄을 없애도 계단이 생기지 않는다.
            //
            // 백엔드가 8/5 에 저장 전 폐기(`73fc2a5`)를 넣어 신규 발행분엔 안 생긴다.
            // 남은 건 과거 발행분뿐이라 이 분기는 시간이 지나면 안 타게 된다.
            val hidesCategory = title != null &&
                emphasis == AttemptCardEmphasis.CATEGORY &&
                title == item.categoryDisplay
            if (!hidesCategory) {
                Spacer(Modifier.height(3.dp))
                LeadLabel(item = item, emphasis = emphasis)
            }
        }

        // ── 날짜는 오른쪽 고정 열 ──────────────────────────────
        //
        // 종전엔 메타줄 안에 `카테고리 · 5/18` 로 붙어 있었다. 그러면 카테고리를 뺀 행만
        // 메타가 짧아져 **날짜가 행마다 다른 x 에 찍혔고**, 목록 전체가 어긋나 보였다.
        // 날짜는 모든 행에 있는 같은 종류의 값이므로 세로로 줄을 세우는 게 맞다.
        //
        // 폭을 고정하는 이유: `오늘`(2자)과 `12/31`(5자)의 폭이 달라서, 오른쪽 정렬만
        // 해서는 왼쪽 끝이 들쭉날쭉해진다. 자리를 잡아두고 그 안에서 오른쪽 정렬한다.
        // 날짜가 없는 항목(미풀이 북마크)도 폭은 남긴다 — 있다 없다 하면 별·셰브론이
        // 행마다 밀린다.
        Spacer(Modifier.width(8.dp))
        Text(
            text = dateStr.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = TextMuted,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.width(DateColumnWidth),
        )

        Spacer(Modifier.width(4.dp))

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

        // ── 주 동작 표시 ─────────────────────────────────────
        //
        // 이 행을 누르면 어디로 가는지를 말한다. 대부분은 상세(셰브론)인데,
        // **미풀이 북마크만 그 문제 풀이로 간다** — 같은 목록에서 행 하나만 동작이
        // 다르므로 글리프도 달라야 한다. 셰브론은 "앱 안으로 들어간다"는 뜻이라
        // 풀이 진입에는 약하다.
        //
        // 자리를 고정폭으로 잡는다. `풀기`(12sp Bold)가 셰브론(16dp)보다 넓어서,
        // 폭을 안 잡으면 미풀이 행에서만 별·날짜가 왼쪽으로 밀린다 — 날짜 열을
        // 고정 열로 세운 이유와 같다.
        Box(
            modifier = Modifier.width(ActionColumnWidth),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (item.unsolved && onStartQuiz != null) {
                Text(
                    text = "풀기",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Lime,
                    maxLines = 1,
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(TextMuted),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * 복습 상태 → 목록에 세울 단계. 졸업이면 나무, 아니면 서버 stage 그대로.
 * 나무는 [ReviewStage] 에 없는 끝점이라 라벨·아이콘을 여기서 짝지어 준다.
 */
private fun ReviewStatus.stageIcon(): StageGlyph =
    if (graduated) StageGlyph(R.drawable.ic_stage_tree, "나무 완성")
    else ReviewStage.of(stage).let { StageGlyph(it.iconRes, it.label) }

private data class StageGlyph(val iconRes: Int, val label: String)

/**
 * 행 둘째 줄의 라벨 — 면 없는 글자 한 줄.
 *
 * 상태 화면에선 "정답 · 금리"처럼 판별 정보를 앞에, 분류를 뒤에 둔다. 색은 글자가
 * 이미 말하는 것을 되풀이하는 보조 신호일 뿐이라, 색을 못 봐도 뜻이 통한다.
 *
 * 글자 크기는 `labelSmall`(11sp) → `labelMedium`(12sp). 작다는 지적을 받은 자리인데,
 * 색(TextMuted)과 굵기가 위계를 이미 지키고 있어서 한 단계는 올려도 제목을 넘지
 * 않는다(제목은 15sp).
 */
@Composable
private fun LeadLabel(
    item: AttemptItem,
    emphasis: AttemptCardEmphasis,
) {
    when (emphasis) {
        // 메타줄은 제목(개념어) 아래 층이다 — 굵게·밝게 두면 위계가 뒤집혀
        // 눈이 카테고리를 먼저 읽는다.
        AttemptCardEmphasis.CATEGORY -> Text(
            text = item.categoryDisplay,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = TextMuted,
        )

        AttemptCardEmphasis.STATUS -> Row(verticalAlignment = Alignment.CenterVertically) {
            val (statusText, statusColor) = when {
                item.unsolved -> "아직 안 푼 문제" to TextSecondary
                item.correct -> "정답" to Lime
                else -> "오답" to Error
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor,
            )
            Text(
                text = "  ·  ",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
            )
            Text(
                text = item.categoryDisplay,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
            )
        }
    }
}

/**
 * 날짜 열의 폭. `12/31`(5자)이 12sp 에서 들어가는 최소치보다 조금 넉넉하게 —
 * 큰 글꼴 설정에서 잘리지 않아야 하고, 그렇다고 제목이 먹을 폭을 크게 뺏어도 안 된다.
 */
private val DateColumnWidth = 42.dp

/**
 * 주 동작 표시 열의 폭. `풀기`(12sp Bold, 2글자)가 들어가고 셰브론(16dp)도 같은 자리에
 * 오른쪽 정렬된다. 행마다 값이 달라도 별·날짜가 안 밀리게 하는 것이 목적이다.
 */
private val ActionColumnWidth = 30.dp

/**
 * 행 글자 블록의 최소 높이 — 제목(bodyLarge 22sp) + 간격 3 + 메타(labelMedium 16sp).
 * 메타줄이 없는 행도 이 높이를 지켜야 목록에 계단이 안 생긴다.
 */
private val RowTextMinHeight = 41.dp

/**
 * `keyword` → 행 제목으로 쓸 **용어**만 뽑는다.
 *
 * 실제 데이터가 세 가지 형태로 온다(2026-08-04 실서버 30건 측정):
 *  - `"종합부동산세: 주택 보유 시 부과되는…"` — 콜론형(28/30). 현재 생성 프롬프트가 강제.
 *  - `"국제유가, 원·달러 환율, 수입물가, …"` — 쉼표 나열형(2/30). 구버전 산출물이라
 *    형식 오류가 아니다. 첫 항목이 그대로 쓸 만한 용어다.
 *  - `"금융통화위원회 — 한국은행의…"` — 대시형. 과거 데이터 대비.
 *
 * 어느 쪽도 아니고 길이가 [KEYWORD_TERM_MAX] 를 넘으면 null 을 돌려 호출부가
 * 질문으로 되돌아가게 한다 — 장문을 제목 자리에 세우면 고치려던 문제가 그대로 남는다.
 */
internal fun keywordTitle(keyword: String?): String? {
    val raw = keyword?.trim().orEmpty()
    if (raw.isEmpty()) return null
    val cut = raw.indexOfFirst { it == ':' || it == '：' || it == '—' || it == '–' || it == ',' }
        .takeIf { it > 0 }
        ?: raw.indexOf(" - ").takeIf { it > 0 }
    val term = (if (cut != null) raw.take(cut) else raw).trim()
    return term.takeIf { it.isNotEmpty() && it.length <= KEYWORD_TERM_MAX }
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
