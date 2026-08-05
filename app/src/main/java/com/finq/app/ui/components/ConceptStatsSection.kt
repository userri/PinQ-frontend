package com.finq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

private const val BAR_MIN_FRACTION = 0.02f

/**
 * 카테고리별 정답률 + 취약 개념 진단 **섹션**.
 *
 * [ConceptStats.weakest] 가 null 이면(표본 부족) 진단 배너 대신 안내 문구를 띄운다.
 * 카테고리 목록 자체가 비면 아무것도 그리지 않는다 — 호출부가 판단한다.
 *
 * 카드 래퍼를 쓰지 않는다 — 마이페이지의 다른 섹션과 같은 이유다([GardenSection] 주석 참고).
 * 취약 개념도 면(배경)을 두지 않고 좌측 액센트 바로만 경고 톤을 싣는다.
 */
@Composable
fun ConceptStatsSection(
    stats: ConceptStats,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "개념별 정답률",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        val weakGroup = weakConceptGroup(stats)
        // 배너도 안내 문구도 없는 경우(지목 과다)가 있다. 그때 여백만 남으면 제목이 뜬금없이
        // 떠 보이므로, 사이 간격은 실제로 뭔가 그릴 때만 넣는다.
        if (weakGroup.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            WeakestConceptBanner(weakGroup)
            Spacer(Modifier.height(14.dp))
        } else if (stats.weakest == null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "조금 더 풀면 취약 개념을 진단해드려요",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(14.dp))
        } else {
            Spacer(Modifier.height(14.dp))
        }

        val weakCategories = weakGroup.map { it.category }.toSet()
        stats.categories.forEachIndexed { index, stat ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            ConceptBar(stat = stat, isWeak = stat.category in weakCategories)
        }
    }
}

/** 진단에 쓰는 최소 표본. 서버 `UserStatsService.weakest` 와 같은 값이어야 한다. */
private const val MIN_SAMPLE = 3

/** 이보다 많이 걸리면 특정 개념이 약한 게 아니라 전반이 낮은 것이라 배너를 숨긴다. */
private const val WEAK_GROUP_MAX = 3

/**
 * 화면에 "흔들린다"고 지목할 개념들. 비어 있으면 배너를 그리지 않는다.
 *
 * 서버의 `weakest` 는 `min()` 이라 **동률이어도 항상 하나**만 내려온다(동률 시 표본 많은 쪽).
 * 그 결과 같은 58% 인데 환율만 빨갛고 부동산은 라임이 되는 일이 실제로 났다 — 사용자는
 * 빨강을 "나쁘다"로 읽는데 실제 의미는 "최저 하나"라 색이 거짓말을 한 것이다.
 * 그래서 **화면에 보이는 값(반올림 %) 기준으로** 동률을 전부 묶는다. 판정 자체는
 * 서버와 같은 규칙(표본 [MIN_SAMPLE] 이상 중 최저)이라 백엔드 변경이 필요 없다.
 *
 * 다만 전부 비슷하게 낮으면 지목이 아니라 잔소리가 되므로 [WEAK_GROUP_MAX] 를 넘으면 숨긴다.
 */
internal fun weakConceptGroup(stats: ConceptStats): List<ConceptStat> {
    if (stats.weakest == null) return emptyList() // 표본 부족 — 서버 판단을 따른다.
    val eligible = stats.categories.filter { it.total >= MIN_SAMPLE }
    val lowest = eligible.minOfOrNull { it.correctRate.toPercent() } ?: return emptyList()
    val group = eligible.filter { it.correctRate.toPercent() == lowest }
    return if (group.size > WEAK_GROUP_MAX) emptyList() else group
}

/**
 * "{개념} 개념이 흔들려요" — 오답 계열이므로 Error 톤을 쓴다.
 *
 * "요즘"을 붙이지 않는다: 서버 `countByCategory` 에 기간 필터가 없어 **전체 기간 누적**이다.
 * 최근 추세가 아닌데 최근인 척하면 거짓이 된다.
 *
 * 예전엔 📉 이모지 + ErrorFaint 채운 박스였다. 이모지는 정책 위반이고, 채운 박스는
 * 페이지가 전부 평평해진 뒤로 혼자 카드처럼 떠 보였다. 지금은 좌측 3dp 액센트만 남긴다.
 */
@Composable
private fun WeakestConceptBanner(group: List<ConceptStat>) {
    // 채운 박스를 쓰지 않는다 — 페이지의 다른 섹션이 전부 평평해진 뒤로는
    // 이것만 카드가 되어 혼자 떠 보인다. 경고 톤은 좌측 액센트 바가 담당한다.
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Error),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "${group.joinToString(" · ") { it.displayName }} 개념이 흔들려요",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            val head = group.first()
            Text(
                text = if (group.size == 1) {
                    "정답률 ${head.correctRate.toPercent()}% · ${head.correct}/${head.total}문제"
                } else {
                    // 묶인 것들은 정의상 표시 정답률이 같다. 문제 수까지 나열하면 줄이 길어져
                    // 진단이 아니라 표가 된다.
                    "모두 정답률 ${head.correctRate.toPercent()}%"
                },
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
    }
}

/**
 * 경고색은 **배너가 지목한 것과 정확히 같은 집합**에만 쓴다.
 *
 * 종전엔 서버 `weakest` 하나에만 칠했다. 그래서 화면상 같은 58% 인데 환율만 빨갛고
 * 부동산은 라임이 되어 **색이 거짓말**을 했다. 색을 빼는 것도 방법이지만, 어느 개념이
 * 약한지는 막대 줄에서 바로 보이는 게 낫다 — 배너와 같은 집합을 칠하면 거짓말은
 * 사라지고 신호는 남는다("한 신호 = 한 역할" — 여기서 빨강의 역할은 '배너가 지목한 것').
 *
 * 지목이 과다해 배너를 숨긴 경우엔 빨강도 없다. 전부 빨개지면 진단이 아니다.
 */
@Composable
private fun ConceptBar(stat: ConceptStat, isWeak: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stat.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = "${stat.correctRate.toPercent()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isWeak) Error else TextSecondary,
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BgElevated),
        ) {
            // 정답률 0% 도 막대가 보이도록 최소 폭을 준다.
            val fraction = stat.correctRate.coerceAtLeast(BAR_MIN_FRACTION)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isWeak) Error else Lime),
            )
        }
    }
}

private fun Float.toPercent(): Int = (this * 100).roundToInt()

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun ConceptStatsSectionPreview() {
    val categories = listOf(
        ConceptStat("INTEREST_RATE", "금리", 20, 16, 0.8f),
        ConceptStat("EXCHANGE_RATE", "환율", 12, 4, 0.33f),
        ConceptStat("STOCK", "증시", 18, 13, 0.72f),
        ConceptStat("REAL_ESTATE", "부동산", 9, 6, 0.67f),
    )
    // 실사용에서 문제가 된 모양: 환율·부동산이 같은 58% 인데 하나만 빨갰다.
    val tied = listOf(
        ConceptStat("INTEREST_RATE", "금리", 20, 17, 0.84f),
        ConceptStat("EXCHANGE_RATE", "환율", 43, 25, 0.5814f),
        ConceptStat("REAL_ESTATE", "부동산", 12, 7, 0.5833f),
    )
    FinQTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            ConceptStatsSection(ConceptStats(categories, weakest = categories[1]))
            ConceptStatsSection(ConceptStats(tied, weakest = tied[1]))
            ConceptStatsSection(ConceptStats(categories, weakest = null))
        }
    }
}
