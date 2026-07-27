package com.finq.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Grass2
import com.finq.app.ui.theme.Grass3
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextSecondary

// ─────────────────────────────────────────────────────────────────────────────
// 복습 단계 히어로 — 개념 시트와 온보딩이 공유하는 시각 컴포넌트.
//
// 진행 신호는 새 색을 만들지 않고 Grass 램프를 그대로 쓴다:
// 연결선이 Grass1 → Grass2 → Grass3 → Lime 으로 차오르며 "성장"을 표현하고,
// 마지막(졸업) 노드만 Lime 링을 얻는다.
// ─────────────────────────────────────────────────────────────────────────────

/** 노드 원 지름. 연결선이 여기서 끝나 스텝퍼처럼 이어 보인다. */
private val NodeSize = 52.dp
/** 노드 안 아이콘 — 리스트의 24dp 와 달리 여기가 단계 아이콘의 진짜 무대다. */
private val NodeIconSize = 40.dp
/** 연결선이 노드 원에 닿기까지 비우는 거리(반지름 + 여백). */
private val ConnectorInset = 28.dp
private val ConnectorWidth = 1.5.dp

/** 단계별 간격 라벨. 실제 값은 ReviewRepository KDoc(3일 → 7일 → 14일) 과 같다. */
private val StageDayLabels = listOf("시작", "+3일", "+7일")

private data class StageNode(
    @DrawableRes val iconRes: Int,
    val label: String,
    val subLabel: String,
    val graduated: Boolean = false,
)

/**
 * enum 은 3단계(SPROUT/GRASS/ALMOST_TREE) + 졸업 구조다.
 * 라벨·아이콘을 enum 에서 끌어와 단계 이름이 두 군데서 갈라지지 않게 한다.
 */
private val StageNodes: List<StageNode> =
    ReviewStage.entries.mapIndexed { i, stage ->
        StageNode(stage.iconRes, stage.label, StageDayLabels[i])
    } + StageNode(R.drawable.ic_stage_tree, "나무", "+14일 · 졸업", graduated = true)

/** 연결선이 차오르는 순서 — 노드 4개 사이 연결선 3개가 이 램프를 따라 그라데이션 된다. */
private val ProgressRamp = listOf(Grass1, Grass2, Grass3, Lime)

/**
 * 새싹 → 풀 → 나무 직전 → 나무(졸업) 가로 4노드 스텝퍼.
 *
 * 하단 소라벨에 `+3일 / +7일 / +14일` 을 그대로 노출한다 — 진행 기준을 감추면
 * "언제 물을 줄 수 있는지"가 불투명해져 오히려 불신을 낳는다.
 * 졸업 노드는 색 하나에 기대지 않도록 링·배경·라벨 굵기·"졸업" 단어까지 다중 신호로 구분한다.
 */
@Composable
fun ReviewStageTimeline(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(NodeSize)) {
            // 연결선 — 노드 중심(폭의 (i+0.5)/4)을 잇는다. 아이콘 뒤에 깔린다.
            Canvas(Modifier.fillMaxWidth().height(NodeSize)) {
                val step = size.width / StageNodes.size
                val cy = size.height / 2f
                val inset = ConnectorInset.toPx()
                for (i in 0 until StageNodes.lastIndex) {
                    val from = step * (i + 0.5f) + inset
                    val to = step * (i + 1.5f) - inset
                    if (to <= from) continue
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ProgressRamp[i], ProgressRamp[i + 1]),
                            startX = from,
                            endX = to,
                        ),
                        start = Offset(from, cy),
                        end = Offset(to, cy),
                        strokeWidth = ConnectorWidth.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                StageNodes.forEach { node ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StageNodeCircle(node)
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth()) {
            StageNodes.forEach { node ->
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = node.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (node.graduated) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (node.graduated) Lime else TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = node.subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** 노드 원 — 어두운 우물에 아이콘을 앉혀 연결선이 원 테두리에서 끝나 보이게 한다. */
@Composable
private fun StageNodeCircle(node: StageNode) {
    Box(
        modifier = Modifier
            .size(NodeSize)
            .clip(CircleShape)
            .background(if (node.graduated) Lime.copy(alpha = 0.12f) else BgSurface)
            .border(
                width = if (node.graduated) 2.dp else 1.dp,
                color = if (node.graduated) Lime else Outline,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ScaledVectorIcon(node.iconRes, NodeIconSize)
    }
}

/**
 * 첫 오답 직후 축하 히어로 — 방금 태어난 새싹만 크게 세우고,
 * 앞으로 만날 단계는 작게 예고한다. 참조형 타임라인 대신 이 자리에 들어간다.
 */
@Composable
fun ReviewStageBirthHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Lime.copy(alpha = 0.12f))
                .border(1.5.dp, Lime.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            ScaledVectorIcon(R.drawable.ic_stage_sprout, 66.dp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "지금 · ${ReviewStage.SPROUT.label}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Lime,
        )

        Spacer(Modifier.height(16.dp))

        // 다음 단계 예고 — 아이콘만 작게, 라벨은 아래 한 줄로 묶는다.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(CircleShape)
                .background(BgSurface)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = "다음",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            listOf(
                ReviewStage.GRASS.iconRes,
                ReviewStage.ALMOST_TREE.iconRes,
                R.drawable.ic_stage_tree,
            ).forEachIndexed { i, res ->
                if (i > 0) {
                    Box(
                        Modifier
                            .size(width = 8.dp, height = ConnectorWidth)
                            .background(ProgressRamp[i + 1]),
                    )
                }
                ScaledVectorIcon(res, 24.dp)
            }
            // 도착지를 이름으로 못박는다 — 타임라인의 "+14일 · 졸업" 과 같은 말.
            Text(
                text = "졸업",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Lime,
            )
        }
    }
}

/**
 * 벡터 아이콘 — painter 는 항상 고유 크기(intrinsicSize)로만 그리고
 * 확대·축소는 캔버스 transform 으로 처리한다.
 *
 * ⚠️ painterResource 는 같은 drawable 에 대해 VectorPainter 를 공유하므로,
 *    한 프레임에서 같은 아이콘을 서로 다른 size 로 draw 하면 내부 캐시 비트맵이
 *    꼬여 중심부가 잘려 그려진다(커밋 7409132 에서 겪은 버그).
 *    개념 시트는 ic_stage_tree 를 히어로 40dp·리스트 24dp 로 동시에 그리므로
 *    이 우회가 반드시 필요하다.
 */
@Composable
internal fun ScaledVectorIcon(
    @DrawableRes res: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    contentDescription: String? = null,
) {
    val painter = painterResource(res)
    val filter = tint?.let { ColorFilter.tint(it) }
    Canvas(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics {
                if (contentDescription != null) this.contentDescription = contentDescription
            },
    ) {
        val intrinsic = painter.intrinsicSize
        val factor = this.size.width / intrinsic.width
        scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) {
            with(painter) { draw(size = intrinsic, colorFilter = filter) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF16385C, widthDp = 380)
@Composable
private fun ReviewStageTimelinePreview() {
    FinQTheme {
        Column(Modifier.background(BgElevated).padding(20.dp)) {
            ReviewStageTimeline()
            Spacer(Modifier.height(28.dp))
            ReviewStageBirthHero()
        }
    }
}
