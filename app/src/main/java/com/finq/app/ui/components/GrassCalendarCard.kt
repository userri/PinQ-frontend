package com.finq.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.ui.components.garden.RecordBandColor
import com.finq.app.data.repository.GrassDay
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import com.finq.app.ui.theme.streakColor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val CELL = 13.dp
private val GAP = 3.dp
private val DAY_LABEL_WIDTH = 22.dp
private const val DAYS_PER_WEEK = 7

/** 세로축 라벨 — GitHub 처럼 격주로만 표기해 빽빽함을 줄인다. */
private val DAY_LABELS = listOf("월", "", "수", "", "금", "", "일")

/**
 * 연간 잔디밭 — GitHub contribution graph 스타일.
 *
 * 한 열 = 한 주(월~일 세로 7칸), 가로로 스크롤하며 **가장 최근 주가 오른쪽**이다.
 * 최초 컴포지션 시 오늘(맨 오른쪽)로 스크롤한다.
 *
 * 색은 [streakColor] 만 사용한다 — 잔디 램프 외의 초록/파랑을 새로 만들지 않는다.
 *
 * 나무 카운터는 여기 헤더가 아니라 마이페이지 카드 하단의 기록 밴드가 맡는다
 * (com.finq.app.ui.components.garden.TreeRecordBlock).
 */
@Composable
fun GrassCalendarCard(
    grass: GrassCalendar,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "잔디밭",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))

            GrassCalendarBody(grass = grass)
        }
    }
}

/**
 * 잔디밭 카드 본문 — 요약칩 + 격자 + 범례 + 상세.
 * 카드 프레임(Card/헤더) 없이 본문만 그리므로, 통합 정원 카드가 헤더를 따로 얹어 재사용한다.
 */
@Composable
fun GrassCalendarBody(grass: GrassCalendar) {
    // 그리드는 항상 "주의 시작(월요일)"에 정렬돼야 열이 어긋나지 않는다.
    val gridStart = remember(grass.from) { grass.from.startOfWeek() }
    val weeks = remember(gridStart, grass.to) {
        (ChronoUnit.DAYS.between(gridStart, grass.to) / DAYS_PER_WEEK).toInt() + 1
    }

    val scrollState = rememberScrollState()
    // 최근이 오른쪽이므로 진입 시 끝으로 보낸다.
    LaunchedEffect(weeks) { scrollState.scrollTo(scrollState.maxValue) }

    // 셀은 탭할 수 없다 — 잔디는 "행동하라"가 아니라 조용히 쌓이는 앰비언트 층이다.
    // 하루치 상세("7/12에 3문제")는 알아도 다음 행동이 없어 인사이트가 아니었고,
    // 행동을 부르는 일은 복습 나무(오늘 물 줄 것)와 개념별 정답률(약한 개념)이 맡는다.
    // 탭이 사라지면서 12dp 셀의 터치 타깃 문제와 안내 문구도 함께 정리된다.
    Column {
        GrassSummaryRow(grass)
        Spacer(Modifier.height(14.dp))

        Row {
            // 요일 라벨 열 — 월 라벨 높이만큼 내려서 그리드와 행을 맞춘다.
            Column(modifier = Modifier.width(DAY_LABEL_WIDTH)) {
                Spacer(Modifier.height(MONTH_LABEL_HEIGHT))
                DAY_LABELS.forEach { label ->
                    Box(
                        modifier = Modifier.height(CELL).padding(bottom = 0.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                    }
                    Spacer(Modifier.height(GAP))
                }
            }

            Column(modifier = Modifier.horizontalScroll(scrollState)) {
                MonthLabels(gridStart = gridStart, weeks = weeks)
                Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
                    repeat(weeks) { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                            repeat(DAYS_PER_WEEK) { dayOfWeek ->
                                val date = gridStart.plusDays((week.toLong() * DAYS_PER_WEEK) + dayOfWeek)
                                GrassCell(date = date, grass = grass)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        GrassLegend()
    }
}

@Composable
private fun GrassCell(
    date: LocalDate,
    grass: GrassCalendar,
) {
    // 집계 범위 밖(from 이전 / 오늘 이후)은 빈 자리로 남겨 격자만 유지한다.
    val outOfRange = date < grass.from || date > grass.to
    if (outOfRange) {
        Box(Modifier.size(CELL))
        return
    }

    // 오늘만 테두리로 표시한다 — 선택 개념이 없어졌으므로 "지금 어디"만 남긴다.
    val isToday = date == grass.to
    Box(
        modifier = Modifier
            .size(CELL)
            .clip(RoundedCornerShape(3.dp))
            .background(streakColor(grass.levelAt(date)))
            .then(
                if (isToday) Modifier.border(1.5.dp, TextPrimary, RoundedCornerShape(3.dp))
                else Modifier
            ),
    )
}

private val MONTH_LABEL_HEIGHT = 16.dp

/**
 * 월 라벨 — 각 월의 첫 주 열 위에만 찍는다.
 *
 * 열 하나 폭이 CELL+GAP 이므로, 라벨을 붙이지 않는 주는 그만큼 빈 칸으로 밀어준다.
 */
@Composable
private fun MonthLabels(gridStart: LocalDate, weeks: Int) {
    Row(modifier = Modifier.height(MONTH_LABEL_HEIGHT)) {
        var lastMonth = -1
        repeat(weeks) { week ->
            val weekStart = gridStart.plusWeeks(week.toLong())
            val month = weekStart.monthValue
            val isNewMonth = month != lastMonth
            if (isNewMonth) lastMonth = month

            // 열 폭(16dp)보다 라벨이 넓으므로 줄바꿈을 막고 옆으로 흘려보낸다.
            // Box 는 기본적으로 클립하지 않으므로 다음 열 위로 자연스럽게 겹쳐 그려진다.
            Box(modifier = Modifier.width(CELL + GAP)) {
                if (isNewMonth) {
                    Text(
                        text = "${month}월",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

/**
 * 활동 통계 4종 — 이 화면에서 `연속`/`최고`를 말하는 **유일한 자리**다.
 * (예전엔 마이페이지 상단 StatCard 로우가 같은 값을 한 번 더 말했다.)
 *
 * 칩 배경을 두르지 않는다 — 잔디밭이 카드에서 섹션으로 내려온 뒤로는
 * 이 줄에 박스를 주면 화면에 다시 테두리 겹이 생긴다. 구분은 간격으로만 한다.
 */
@Composable
private fun GrassSummaryRow(grass: GrassCalendar) {
    // 히트맵 격자는 좌측 요일 라벨(DAY_LABEL_WIDTH) 만큼 안으로 밀려 있다.
    // 같은 만큼 들여써야 섹션 안에 왼쪽 기준선이 두 개 생기지 않는다.
    Row(modifier = Modifier.fillMaxWidth().padding(start = DAY_LABEL_WIDTH)) {
        SummaryStat(value = "${grass.totalActiveDays}일", label = "활동", modifier = Modifier.weight(1f))
        SummaryStat(value = "${grass.perfectDays}일", label = "만점", modifier = Modifier.weight(1f))
        SummaryStat(value = "${grass.currentStreak}일", label = "연속", modifier = Modifier.weight(1f))
        SummaryStat(value = "${grass.maxStreak}일", label = "최고", modifier = Modifier.weight(1f))
    }
}

/**
 * 값+라벨 두 줄. 전부 중립 톤 — 화면의 라임 포인트는 프로필 헤더가 가져갔다.
 *
 * 값은 섹션 헤더("잔디밭", titleSmall)보다 한 단 크게 둔다. 데이터가 그것을 가리키는
 * 라벨보다 작으면 위계가 뒤집힌다. 위로는 헤더의 정답률(headlineMedium)과 충분히 벌어진다.
 */
@Composable
private fun SummaryStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun GrassLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "적게", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.width(6.dp))
        (0..4).forEach { level ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(CELL)
                    .clip(RoundedCornerShape(3.dp))
                    .background(streakColor(level)),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(text = "많이", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

/**
 * 잔디밭 로딩 스켈레톤 — fetch 완료 전 첫 프레임에 옛 데이터를 그리지 않기 위한 자리표시자.
 *
 * 섹션 헤더("잔디밭")는 [com.finq.app.ui.components.garden.GardenSection] 이 상태와 무관하게
 * 항상 그리므로 여기선 본문 자리만 채운다. 실제 본문과 높이를 맞춰 레이아웃 점프를 줄인다.
 */
@Composable
fun GrassCalendarSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { SkeletonBlock(modifier = Modifier.weight(1f), height = 34.dp) }
        }
        Spacer(Modifier.height(14.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 128.dp)
        Spacer(Modifier.height(16.dp))
        // 기록 밴드 자리 — 톤 밴드는 로딩 중에도 유일한 면으로 남는다.
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(RecordBandColor)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            SkeletonBlock(width = 88.dp, height = 14.dp)
            Spacer(Modifier.height(16.dp))
            SkeletonBlock(width = 120.dp, height = 32.dp)
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null,
    height: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(height)
            .clip(RoundedCornerShape(10.dp))
            .background(BgElevated),
    )
}

/** 잔디밭 첫 로드 실패 — 스켈레톤 대신 재시도 안내. 섹션 헤더는 호출부가 이미 그렸다. */
@Composable
fun GrassCalendarError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "잔디밭을 불러오지 못했어요",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "다시 시도",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Lime,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

/** 그 주의 월요일. */
private fun LocalDate.startOfWeek(): LocalDate =
    minusDays((dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun GrassCalendarCardPreview() {
    val today = LocalDate.of(2026, 7, 8)
    val from = today.minusDays(364)
    val dayMap = (0..364).mapNotNull { offset ->
        val date = from.plusDays(offset.toLong())
        val level = (offset * 7) % 6   // 0~4 + 빈날
        if (level == 0 || level > 4) null
        else date to GrassDay(level = level, solved = level, reviewed = if (level == 1) 2 else 0)
    }.toMap()

    FinQTheme {
        GrassCalendarCard(
            grass = GrassCalendar(
                from = from,
                to = today,
                totalActiveDays = dayMap.size,
                perfectDays = dayMap.count { it.value.level == 4 },
                currentStreak = 7,
                maxStreak = 15,
                graduatedTrees = 4,
                dayByDate = dayMap,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
