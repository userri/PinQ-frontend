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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.GrassDay
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
import java.time.format.DateTimeFormatter
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
 */
@Composable
fun GrassCalendarCard(
    grass: GrassCalendar,
    modifier: Modifier = Modifier,
) {
    // 그리드는 항상 "주의 시작(월요일)"에 정렬돼야 열이 어긋나지 않는다.
    val gridStart = remember(grass.from) { grass.from.startOfWeek() }
    val weeks = remember(gridStart, grass.to) {
        (ChronoUnit.DAYS.between(gridStart, grass.to) / DAYS_PER_WEEK).toInt() + 1
    }

    val scrollState = rememberScrollState()
    // 최근이 오른쪽이므로 진입 시 끝으로 보낸다.
    LaunchedEffect(weeks) { scrollState.scrollTo(scrollState.maxValue) }

    // 셀 탭 시 그날 상세("N문제 풀이 · M문제 복습")를 그리드 아래에 띄운다.
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "잔디밭",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                // 복습으로 졸업한 문제 = 키운 나무.
                Text(
                    text = "🌳 키운 나무 ${grass.graduatedTrees}그루",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(12.dp))

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
                                    GrassCell(
                                        date = date,
                                        grass = grass,
                                        isSelected = date == selectedDate,
                                        onClick = { selectedDate = date },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            GrassLegend()

            // 선택한 날짜 상세 — 없으면 안내 문구.
            Spacer(Modifier.height(10.dp))
            GrassDayDetail(date = selectedDate, day = selectedDate?.let(grass::dayAt))
        }
    }
}

@Composable
private fun GrassCell(
    date: LocalDate,
    grass: GrassCalendar,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // 집계 범위 밖(from 이전 / 오늘 이후)은 빈 자리로 남겨 격자만 유지한다.
    val outOfRange = date < grass.from || date > grass.to
    if (outOfRange) {
        Box(Modifier.size(CELL))
        return
    }

    val isToday = date == grass.to
    val borderColor = when {
        isSelected -> Lime
        isToday -> TextPrimary
        else -> null
    }
    Box(
        modifier = Modifier
            .size(CELL)
            .clip(RoundedCornerShape(3.dp))
            .background(streakColor(grass.levelAt(date)))
            .then(
                if (borderColor != null) Modifier.border(1.5.dp, borderColor, RoundedCornerShape(3.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
    )
}

private val DETAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("M월 d일")

/** 선택한 날짜의 풀이/복습 상세. 복습만 한 날은 그 맥락을 함께 보여준다. */
@Composable
private fun GrassDayDetail(date: LocalDate?, day: GrassDay?) {
    val text = when {
        date == null -> "날짜를 눌러 그날의 활동을 확인해요"
        day == null -> "${date.format(DETAIL_DATE_FORMAT)} · 활동 없음"
        day.reviewOnly -> "${date.format(DETAIL_DATE_FORMAT)} · 복습만 한 날 — 연한 잔디 (복습 ${day.reviewed}문제)"
        else -> "${date.format(DETAIL_DATE_FORMAT)} · ${day.solved}문제 풀이 · ${day.reviewed}문제 복습"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = if (date == null) TextMuted else TextSecondary,
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

@Composable
private fun GrassSummaryRow(grass: GrassCalendar) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryChip(value = "${grass.totalActiveDays}일", label = "활동", modifier = Modifier.weight(1f))
        SummaryChip(value = "${grass.perfectDays}일", label = "만점", modifier = Modifier.weight(1f), highlight = true)
        SummaryChip(value = "${grass.currentStreak}일", label = "연속", modifier = Modifier.weight(1f))
        SummaryChip(value = "${grass.maxStreak}일", label = "최고", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SummaryChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            // 만점일만 라임으로 강조 — 화면당 포인트를 하나로 유지한다.
            color = if (highlight) Lime else TextPrimary,
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
