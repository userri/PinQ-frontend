package com.finq.app.ui.library

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.repository.ReviewStage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextSecondary

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
    /** 미풀이 북마크를 탭했을 때 풀이 화면으로 보내는 콜백. null 이면 탭해도 아무 일 없음. */
    onStartQuiz: (() -> Unit)? = null,
    /** 정원 딥링크로 진입한 카드 — 처음부터 펼쳐 보여준다. */
    initialExpanded: Boolean = false,
) {
    var expanded by remember(item.quizId) { mutableStateOf(initialExpanded) }
    val context = LocalContext.current
    val dateStr = remember(item.solvedAtIso) { formatSolvedDate(item.solvedAtIso) }

    val hasArticle = item.article != null
        && !item.article.url.isBlank()
        && !item.article.title.isBlank()

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
            // 상단 행: 카테고리 뱃지 + 날짜  ····  ⭐ 북마크
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 카테고리 태그는 중립 — 유채색은 포인트/정답/오답에만 쓴다.
                    Surface(
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
                    if (item.unsolved) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = BgSubtle,
                        ) {
                            Text(
                                text = "아직 안 푼 문제",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else if (!item.correct) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Text(
                                text = "오답",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    // 복습(물 주기) 상태 뱃지 — 중립 톤, 다 키운 나무만 Lime 포인트.
                    item.review?.let { review ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = BgSubtle,
                        ) {
                            Text(
                                text = if (review.graduated) "🌳 나무 완성"
                                       else "${ReviewStage.of(review.stage).emoji} 물 ${review.waterCount}번",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (review.graduated) Lime else TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    if (dateStr != null) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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

            Spacer(Modifier.height(10.dp))

            // 성장 근접 스트립 — 복습중(자라는) 오답만. 졸업/legacy 는 growthStrip 이 null.
            item.review?.let { review ->
                growthStrip(
                    stage = review.stage,
                    graduated = review.graduated,
                    dueDateIso = review.dueDateIso,
                    today = LocalDate.now(),
                )?.let { strip ->
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
                    Spacer(Modifier.height(8.dp))
                }
            }

            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (item.unsolved) "풀러 가기" else "자세히 보기",
                    style = MaterialTheme.typography.labelSmall,
                    color = Lime,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))

                AnswerRow(
                    label = if (item.correct) "내 답 (정답)" else "내 답",
                    text = item.myAnswerText,
                    isCorrect = item.correct,
                )

                if (!item.correct) {
                    Spacer(Modifier.height(6.dp))
                    AnswerRow(label = "정답", text = item.correctAnswerText, isCorrect = true)
                }

                if (item.explanation.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "해설",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (!item.keyword.isNullOrBlank()) {
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
                                text = item.keyword,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Lime,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                if (hasArticle) {
                    val article = item.article!!
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

/** "2026-05-18T14:23:00" → "5/18" (오늘이면 "오늘"). 파싱 실패 시 null. */
private fun formatSolvedDate(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        val dt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (dt.toLocalDate().isEqual(LocalDate.now())) "오늘"
        else dt.format(DateTimeFormatter.ofPattern("M/d"))
    }.getOrNull()
}
