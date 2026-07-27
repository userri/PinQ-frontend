package com.finq.app.ui.components.garden

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary

/**
 * 기록 밴드 배경 — 카드 안에 또 카드를 넣지 않고 톤 차이로만 영역을 가른다.
 * 새 색을 만들지 않고 기존 배경 토큰 두 개를 섞은 값이다.
 */
val RecordBandColor = lerp(BgSurface, BgElevated, 0.45f)

/**
 * 복습 나무 **기록 블록** — 정원으로 가는 링크가 아니라 "누적 성취"를 읽는 자리.
 *
 * 홈 하단이 이미 정원 씬(탐색·감상)을 맡고 있으므로, 마이페이지는 같은 냄새를
 * 반복하지 않고 프로필 화면의 관례대로 **누적 숫자의 집** 역할만 한다.
 * 정원 화면으로 가는 길은 조용한 텍스트 링크로 남긴다(블록 전체도 탭 가능).
 *
 * 위계는 일부러 비대칭이다 — `키운 나무`만 크게(라임), 나머지는 작고 중립 톤.
 *
 * @param graduatedTrees 키운 나무 수. 카운터가 항상 진실이므로 정원 목록이 아직
 *   없어도(=[garden] null) 이 값만으로 주역 숫자를 그린다(stale flash 방지).
 * @param garden 정원 데이터. null 이면 "자라는 중" 줄만 생략한다.
 */
@Composable
fun TreeRecordBlock(
    graduatedTrees: Int,
    garden: ReviewGarden?,
    onOpenGarden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val growing = garden?.growing
    val almostTrees = growing?.count { it.stage == ReviewStage.ALMOST_TREE } ?: 0

    // 자체 패딩을 두지 않는다 — 박스였을 때의 잔재다. 지금은 잔디밭·개념별 정답률과
    // 같은 급의 섹션이라 페이지 패딩만 쓰고, 좌우 기준선을 다른 섹션과 맞춘다.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenGarden),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 잔디밭·개념별 정답률과 같은 급의 섹션 제목 — 나무는 잔디의 하위가 아니라
            // 별도 축(잔디=일일 활동, 나무=복습)이라 동급으로 선다.
            Text(
                text = "복습 나무",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            // 화살표는 글자(→)가 아니라 아이콘으로 — 글리프는 베이스라인에 어정쩡하게 걸려
            // 폰트마다 굵기·크기가 달라진다. 셰브론은 획 2개짜리 단순 글리프라
            // 성장 아이콘의 20dp 하한과 달리 16dp에서도 뭉개지지 않는다.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "내 정원",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Lime,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = Lime,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            // 주역 숫자는 "가장 큰 참말"을 고른다. 나무가 0인데 49개가 자라는 중일 때
            // "첫 나무를 키워보세요"를 크게 띄우면, 실제로 벌어지는 일을 부정하는 문구가 된다.
            Column(modifier = Modifier.weight(1f)) {
                val growingCount = growing?.size ?: 0
                when {
                    graduatedTrees > 0 -> HeroCount(value = graduatedTrees, unit = "그루")
                    growingCount > 0 -> HeroCount(value = growingCount, unit = "자라는 중")
                    else -> Text(
                        text = "아직 나무가 없어요",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }

                Spacer(Modifier.height(4.dp))
                // 보조는 딱 한 줄. 크기·굵기를 섞지 않고 숫자만 색으로 한 톤 올린다.
                Text(
                    text = supportLine(graduatedTrees, growingCount, almostTrees),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.width(12.dp))
            PlantStrip(graduatedTrees = graduatedTrees, growing = growing)
        }
    }
}

/** 주역 숫자 + 단위. 숫자만 라임, 단위는 중립 — 베이스라인 정렬. */
@Composable
private fun HeroCount(value: Int, unit: String) {
    Row {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Lime,
            modifier = Modifier.alignByBaseline(),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

/**
 * 보조 한 줄. 주역이 무엇이 됐는지에 따라 남은 사실만 말한다 —
 * 성장 규칙("세 번 맞히면 나무") 설명은 개념 시트가 맡는다.
 */
@Composable
private fun supportLine(trees: Int, growing: Int, almost: Int) = buildAnnotatedString {
    when {
        trees > 0 && growing > 0 -> {
            append("자라는 중 ")
            withStyle(SpanStyle(color = TextSecondary)) { append("$growing") }
            if (almost > 0) {
                append("  ·  곧 나무 ")
                withStyle(SpanStyle(color = TextSecondary)) { append("$almost") }
            }
        }
        trees > 0 -> append("키운 나무")
        growing > 0 && almost > 0 -> {
            append("곧 나무 ")
            withStyle(SpanStyle(color = TextSecondary)) { append("$almost") }
        }
        growing > 0 -> append("복습할 때마다 자라요")
        else -> append("오답을 복습하면 자라기 시작해요")
    }
}

private const val STRIP_MAX = 5

/**
 * 크기·들림을 미세하게 다르게 준 배치표(오른쪽 = 가장 최근 = 가장 크고 지면에 붙음).
 * 균일 간격으로 나열하면 대시보드 아이콘 줄이 되므로, 겹치고 흔들리게 둔다.
 */
private val STRIP_SIZES = listOf(24.dp, 27.dp, 24.dp, 26.dp, 28.dp)
private val STRIP_LIFT = listOf(3.dp, 0.dp, 4.dp, 1.dp, 0.dp)

/**
 * 이 블록의 시각 앵커 — 정원 씬의 유기적 느낌을 축소해 담은 아이콘 줄.
 *
 * 나무가 있으면 나무를, 아직 없으면 자라는 중인 단계 아이콘을 흐리게 보여준다
 * (0그루 상태가 텅 비어 초라해지지 않게). 완전 신규(둘 다 0)면 나무 실루엣 한 그루로
 * "여기가 자랄 자리"라는 것만 예고한다 — 없는 기록을 지어내는 게 아니라 빈 자리 표시다.
 */
@Composable
private fun PlantStrip(graduatedTrees: Int, growing: List<GardenItem>?) {
    val treeCount = graduatedTrees.coerceAtMost(STRIP_MAX)
    // 나무가 하나도 없을 때만 자라는 중인 식물로 대체한다.
    val sprouts = if (treeCount > 0) emptyList() else growing.orEmpty().take(STRIP_MAX)
    val count = if (treeCount > 0) treeCount else sprouts.size
    if (count == 0) {
        // 정원 데이터를 아직 못 받았을 뿐일 수도 있으니(null) 그때는 자리만 비워둔다.
        if (growing != null) {
            StripIcon(
                iconRes = R.drawable.ic_stage_tree,
                size = STRIP_SIZES.last(),
                lift = 0.dp,
                alpha = GHOST_ALPHA,
            )
        }
        return
    }

    // 오른쪽 끝이 가장 크고 지면에 붙도록 배치표의 뒤쪽부터 쓴다.
    val sizes = STRIP_SIZES.takeLast(count)
    val lifts = STRIP_LIFT.takeLast(count)

    Row(
        // 음수 간격 = 살짝 겹침. 뒤에 그려지는(오른쪽 = 최근) 항목이 위로 올라온다.
        horizontalArrangement = Arrangement.spacedBy((-5).dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(count) { i ->
            val iconRes = if (treeCount > 0) R.drawable.ic_stage_tree else sprouts[i].stage.iconRes
            StripIcon(
                iconRes = iconRes,
                size = sizes[i],
                lift = lifts[i],
                alpha = if (treeCount > 0) 1f else GROWING_ALPHA,
            )
        }
    }
}

/** 자라는 중 아이콘 — 나무보다 한 단계 물러나 보이게. */
private const val GROWING_ALPHA = 0.55f

/** 아직 아무것도 없는 자리 표시 — 있는 척하지 않을 만큼만 흐리게. */
private const val GHOST_ALPHA = 0.18f

@Composable
private fun StripIcon(iconRes: Int, size: Dp, lift: Dp, alpha: Float) {
    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier
            .padding(bottom = lift)
            .size(size)
            .alpha(alpha),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0E2540, widthDp = 360)
@Composable
private fun TreeRecordBlockPreview() {
    fun g(id: Long, stage: ReviewStage) = GardenItem(
        quizId = id, categoryLabel = "경제", question = "q$id", keyword = null,
        stage = stage, dueDate = null, waterCount = 2, absorbedCount = 1, graduatedAtIso = null,
    )
    FinQTheme {
        Column {
            TreeRecordBlock(
                graduatedTrees = 12,
                garden = ReviewGarden(
                    growing = listOf(
                        g(1, ReviewStage.SPROUT), g(2, ReviewStage.GRASS),
                        g(3, ReviewStage.ALMOST_TREE), g(4, ReviewStage.SPROUT),
                    ),
                    graduated = emptyList(),
                    graduatedTrees = 12,
                ),
                onOpenGarden = {},
            )
            TreeRecordBlock(
                graduatedTrees = 0,
                garden = ReviewGarden(
                    growing = listOf(g(1, ReviewStage.SPROUT), g(2, ReviewStage.GRASS)),
                    graduated = emptyList(),
                    graduatedTrees = 0,
                ),
                onOpenGarden = {},
            )
            TreeRecordBlock(graduatedTrees = 0, garden = ReviewGarden.EMPTY, onOpenGarden = {})
        }
    }
}
