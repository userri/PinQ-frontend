package com.finq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
    /**
     * 누적 정답률 0.0~1.0 과 표본. 종전엔 프로필 헤더가 큰 라임 `82%` 로 소유했는데,
     * 개념별 정답률과 **세 섹션 떨어져 있어 같은 축인 줄 읽히지 않았다** — 헤더의
     * 하나짜리 평균은 아래 목록의 요약도 아니다(가중치가 다르다). 여기로 내리면
     * "전체 82%, 그런데 부동산이 58%" 라는 관계가 한 눈에 잡힌다.
     *
     * null 이면 줄을 그리지 않는다 — 아직 한 문제도 안 푼 사용자에게 `0%` 를 띄우지
     * 않기 위해서다(프로필 헤더의 종전 규칙을 그대로 가져왔다).
     */
    overallRate: Float? = null,
    totalSolved: Int = 0,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "개념별 정답률",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        if (overallRate != null && totalSolved > 0) {
            Spacer(Modifier.height(4.dp))
            // 라임을 쓰지 않는다 — 이 섹션의 색은 빨강(기준 미달) 하나가 소유한다.
            // 위에 큰 라임 값을 얹으면 "전체는 좋음 / 개념은 나쁨"을 두 색이 동시에
            // 외쳐 어느 쪽을 봐야 할지 흐려진다. 여기선 기준선 역할이면 충분하다.
            //
            // 문제 수를 붙이는 이유: 이 숫자의 유일한 쓸모가 **정답률의 표본**이다.
            // 헤더에 혼자 `212문제 풀이` 로 떠 있을 땐 무엇에 쓰는 값인지 알 수 없었다.
            Text(
                text = "전체 ${overallRate.toPercent()}% · ${totalSolved}문제",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
        val diagnosis = conceptDiagnosis(stats)
        Spacer(Modifier.height(12.dp))
        when (diagnosis) {
            is ConceptDiagnosis.Weak -> WeakConceptBanner(diagnosis.concepts)
            // 지목이 없는 상태들은 **같은 슬롯·같은 스타일**로 둔다. 여기에 경고 액센트를
            // 남기면 색만 뺀 경고가 되어 애매해진다.
            else -> Text(
                text = diagnosis.mutedLine(),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        Spacer(Modifier.height(14.dp))

        val weak = (diagnosis as? ConceptDiagnosis.Weak)?.concepts.orEmpty().toSet()
        stats.categories.forEachIndexed { index, stat ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            ConceptBar(stat = stat, isWeak = stat in weak)
        }
    }
}

/** 진단에 쓰는 최소 표본. 서버 `UserStatsService.weakest` 와 같은 값이어야 한다. */
private const val MIN_SAMPLE = 3

/** 이보다 많이 지목되면 이름을 나열하지 않고 개수로 말한다 — 줄이 넘치면 표가 된다. */
private const val WEAK_NAME_MAX = 3

/**
 * 막대를 빨갛게 칠하는 절대 기준 — **표시 정답률 60% 미만**.
 *
 * 종전엔 기준이 상대적이었다("이 목록에서 최저"). 그러면 전부 잘해도 하나는 빨갛고,
 * 전부 못해도 하나만 빨갛다 — 색이 사용자의 실력이 아니라 목록 안 순위를 말한 것이다.
 *
 * 60% 인 이유: 10문제 중 4문제 이상 틀린 수준이고, 하루 5문제 기준으로는 2문제 이상이다.
 * 50% 는 찍어도 나오는 값이라 관대하고, 70% 는 대부분이 빨개져 신호가 죽는다.
 */
private const val WEAK_BAR_PERCENT = 60

/**
 * 표본이 [MIN_SAMPLE] 미만이면 칠하지 않는다 — 2문제 틀렸다고 0% 로 빨갛게 두면
 * 시작하자마자 질책이 된다. 배너 지목 기준과 같은 하한이다.
 */
private fun ConceptStat.isBelowBar(): Boolean =
    total >= MIN_SAMPLE && correctRate.toPercent() < WEAK_BAR_PERCENT

/**
 * 개념 목록 하나에 대한 진단. **막대의 빨강과 배너 문구는 같은 집합에서 나온다.**
 *
 * 종전엔 빨강은 "기준 미달 전부", 배너는 "표시값 동률 최저"를 말했다. 서버 `weakest` 가
 * `min()` 이라 하나만 내려주던 시절의 흔적인데, 절대 기준을 도입한 뒤로는 존재 이유가
 * 사라졌다. 그동안 구멍이 둘 있었다 — 동률이 넷 이상이면 빨강 넷에 **아무 말이 없었고**,
 * 값이 제각각이면 빨강 다섯 중 **하나만 이름이 불렸다**(실사용 지적).
 */
internal sealed interface ConceptDiagnosis {
    /** 표본 [MIN_SAMPLE] 이상인 개념이 아직 없다. */
    data object NotEnough : ConceptDiagnosis

    /**
     * 표본이 있는 개념이 **전부** 기준 미달.
     *
     * 화면이 통째로 붉어지면 빨강이 아무것도 구별해주지 못하고 질책만 남는다(실기기 확인).
     * 이때만 경고색을 완전히 끄고 문구 한 줄로 받는다.
     */
    data object AllBelow : ConceptDiagnosis

    /** 기준 미달이 있고 기준 이상도 있다 — 빨강이 아직 구별해주므로 남긴다. */
    data class Weak(val concepts: List<ConceptStat>) : ConceptDiagnosis

    /** 표본이 있는 개념이 전부 기준 이상. */
    data object AllGood : ConceptDiagnosis
}

/** 배너를 그리지 않는 상태들의 한 줄. [ConceptDiagnosis.Weak] 은 배너가 대신 말한다. */
private fun ConceptDiagnosis.mutedLine(): String = when (this) {
    ConceptDiagnosis.NotEnough -> "조금 더 풀면 취약 개념을 진단해드려요"
    ConceptDiagnosis.AllBelow -> "아직 익숙해지는 중이에요"
    ConceptDiagnosis.AllGood -> "모든 개념이 안정적이에요"
    is ConceptDiagnosis.Weak -> ""
}

/** 표본 [MIN_SAMPLE] 이상인 개념을 [WEAK_BAR_PERCENT] 로 갈라 상태를 정한다. */
internal fun conceptDiagnosis(stats: ConceptStats): ConceptDiagnosis {
    val samples = stats.categories.filter { it.total >= MIN_SAMPLE }
    if (samples.isEmpty()) return ConceptDiagnosis.NotEnough
    val below = samples.filter { it.isBelowBar() }.sortedBy { it.correctRate }
    return when (below.size) {
        0 -> ConceptDiagnosis.AllGood
        samples.size -> ConceptDiagnosis.AllBelow
        else -> ConceptDiagnosis.Weak(below)
    }
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
private fun WeakConceptBanner(group: List<ConceptStat>) {
    // 채운 박스를 쓰지 않는다 — 페이지의 다른 섹션이 전부 평평해진 뒤로는
    // 이것만 카드가 되어 혼자 떠 보인다. 경고 톤은 좌측 액센트 바가 담당한다.
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 높이를 고정하지 않는다 — 둘째 줄이 없는 경우(복수 지목)에도 액센트가 남지 않도록.
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(Error),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                // 넷 이상이면 이름 대신 개수로 말한다 — 줄이 넘치면 진단이 아니라 표가 되고,
                // 어느 개념인지는 바로 아래 막대 목록이 빨강으로 이미 보여준다.
                text = if (group.size > WEAK_NAME_MAX) {
                    "${group.size}개 개념이 흔들려요"
                } else {
                    "${group.joinToString(" · ") { it.displayName }} 개념이 흔들려요"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            // 정답률은 바로 아래 막대 줄이 이미 말한다. 여기 다시 쓰면 정보가 아니라 소음이다.
            // 남길 만한 건 막대에 없는 **문제 수** 뿐. 복수 지목이면 어느 수가 어느 개념인지
            // 모호해지므로(`25/43 · 7/12`) 아예 붙이지 않고 제목 한 줄로 둔다.
            if (group.size == 1) {
                val head = group.first()
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${head.total}문제 중 ${head.correct}개 정답",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

/**
 * 빨강의 역할은 하나 — **[WEAK_BAR_PERCENT] 기준 미달**([isBelowBar]).
 *
 * 종전엔 서버 `weakest` 하나에만 칠했다. 상대 기준이라 같은 58% 인데 환율만 빨갛고
 * 부동산은 라임이 되어 **색이 거짓말**을 했다. 절대 기준으로 바꾸면 순위와 무관하게
 * 같은 값은 항상 같은 색이 된다.
 *
 * 빨강 집합과 배너가 말하는 집합은 **항상 같다**([ConceptDiagnosis.Weak]). 다르게 두었더니
 * 이름 없이 빨간 줄이 남았다.
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
