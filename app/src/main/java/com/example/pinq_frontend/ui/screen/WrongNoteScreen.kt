package com.example.pinq_frontend.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.pinq_frontend.data.local.SavedWrongNote
import com.example.pinq_frontend.ui.theme.PinQBlue
import com.example.pinq_frontend.ui.theme.PinQLightBlue
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme
import com.example.pinq_frontend.ui.wrongnote.WrongNoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 누적 오답노트 화면 (하단 탭용).
 * 탭: 오늘 / 전체  +  카테고리 필터칩
 */
@Composable
fun WrongNoteTabScreen(
    viewModel: WrongNoteViewModel,
    modifier: Modifier = Modifier,
) {
    val allNotes by viewModel.allNotes.collectAsState()
    WrongNoteTabContent(
        allNotes = allNotes,
        modifier = modifier,
    )
}

@Composable
fun WrongNoteTabContent(
    allNotes: List<SavedWrongNote>,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val todayStartMillis = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val todayNotes = remember(allNotes, todayStartMillis) {
        allNotes.filter { it.savedDateMillis >= todayStartMillis }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // ── 헤더 ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "오답노트",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // ── 탭 ───────────────────────────────────────────────────
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PinQBlue,
        ) {
            listOf("오늘", "전체 (${allNotes.size})").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        // ── 탭 컨텐츠 ─────────────────────────────────────────────
        when (selectedTab) {
            0 -> WrongNoteList(
                notes = todayNotes,
                emptyMessage = "오늘 틀린 문제가 없어요 🎉",
            )
            1 -> WrongNoteList(
                notes = allNotes,
                emptyMessage = "오답노트가 비어있어요 🏆",
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 오답 목록 + 카테고리 필터
// ─────────────────────────────────────────────────────────────────────────────

private val categoryFilters = listOf("전체", "금리", "환율", "증시", "부동산")

@Composable
private fun WrongNoteList(
    notes: List<SavedWrongNote>,
    emptyMessage: String,
) {
    var selectedCategory by remember { mutableStateOf("전체") }

    val filtered = remember(notes, selectedCategory) {
        val byCategory = if (selectedCategory == "전체") notes
        else notes.filter { it.categoryDisplay == selectedCategory }
        // 최신 풀이가 위에 오도록 날짜 내림차순 정렬
        byCategory.sortedByDescending { it.savedDateMillis }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 카테고리 필터칩
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categoryFilters) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) PinQBlue else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📭", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            ) {
                items(filtered, key = { it.quizId }) { note ->
                    WrongNoteCard(note = note)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 오답 카드
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WrongNoteCard(note: SavedWrongNote) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dateStr = remember(note.savedDateMillis) {
        SimpleDateFormat("M/d", Locale.KOREAN).format(Date(note.savedDateMillis))
    }

    // 기사 URL이 유효한지 여부
    val hasArticle = !note.relatedArticleUrl.isNullOrBlank()
        && !note.relatedArticleTitle.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clickable { expanded = !expanded },
        ) {
            // 상단 행: 카테고리 뱃지 + 날짜
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PinQLightBlue,
                ) {
                    Text(
                        text = note.categoryDisplay,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = PinQBlue,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(10.dp))

            // 문제 (접힌 상태: 2줄 말줄임)
            Text(
                text = note.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )

            // 접힌 상태일 때만 클릭 안내 표시
            if (!expanded) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👆 카드를 클릭해보세요",
                        style = MaterialTheme.typography.labelSmall,
                        color = PinQBlue,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // 펼쳐지면 내 답 / 정답 / 해설 / 키워드 / 관련 기사 표시
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))

                AnswerRow(label = "내 답", text = note.myAnswerText, isCorrect = false)
                Spacer(Modifier.height(6.dp))
                AnswerRow(label = "정답", text = note.correctAnswerText, isCorrect = true)

                if (note.explanation.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "💡 해설",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (!note.keyword.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔑 키워드  ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PinQLightBlue,
                        ) {
                            Text(
                                text = note.keyword,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PinQBlue,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // ── 관련 기사 버튼 ──────────────────────────────────────
                if (hasArticle) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    note.relatedArticleUrl!!.toUri(),
                                )
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
                                text = "📰 관련 기사",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = note.relatedArticleTitle!!,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!note.relatedArticleSource.isNullOrBlank()) {
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = note.relatedArticleSource,
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
    val bgColor = if (isCorrect) PinQLightBlue else MaterialTheme.colorScheme.errorContainer
    val textColor = if (isCorrect) PinQBlue else MaterialTheme.colorScheme.onErrorContainer
    val icon = if (isCorrect) "✅" else "❌"

    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$icon $label  ",
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

// ─────────────────────────────────────────────────────────────────────────────
// 세션 오답노트 화면 (session/wrongnote route — 결과화면에서 뒤로가기 포함)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WrongNoteScreen(
    quizzes: List<com.example.pinq_frontend.data.model.Quiz>,
    answerHistory: List<com.example.pinq_frontend.data.repository.AnswerResult>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // SavedWrongNote.from 헬퍼를 통해 변환 — 매핑 로직은 한 곳에서만 관리된다.
    val wrongItems = remember(quizzes, answerHistory) {
        quizzes.zip(answerHistory)
            .filter { (_, answer) -> !answer.isCorrect }
            .map { (quiz, answer) -> SavedWrongNote.from(quiz, answer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "오답노트",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (wrongItems.isEmpty()) "모두 맞혔어요! 🎉" else "${wrongItems.size}개 틀렸어요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (wrongItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🏆", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "오답이 없어요!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            ) {
                items(wrongItems, key = { it.quizId }) { note ->
                    WrongNoteCard(note = note)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "결과로 돌아가기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun WrongNoteTabPreview() {
    val sampleNotes = listOf(
        SavedWrongNote(
            quizId = 1L,
            question = "한국은행이 기준금리를 올리면 일반적으로 시중 대출금리는 어떻게 변하나요?",
            categoryName = "INTEREST_RATE",
            categoryDisplay = "금리",
            myAnswerText = "내려간다",
            correctAnswerText = "올라간다",
            explanation = "기준금리가 오르면 은행의 자금 조달 비용이 높아져 대출금리도 함께 상승합니다.",
            keyword = "기준금리",
            relatedArticleTitle = "한국은행, 기준금리 0.25%p 인상 결정",
            relatedArticleUrl = "https://example.com/article/1",
            relatedArticleSource = "연합뉴스",
        ),
        SavedWrongNote(
            quizId = 2L,
            question = "주식 시장에서 '베어 마켓'은 어떤 상황을 가리키나요?",
            categoryName = "STOCK",
            categoryDisplay = "증시",
            myAnswerText = "주가가 급등하는 상황",
            correctAnswerText = "주가가 20% 이상 하락한 상황",
            explanation = "베어 마켓은 주가가 최고점 대비 20% 이상 하락한 하락장을 의미합니다.",
            keyword = "베어 마켓",
        ),
    )
    PinQ_frontendTheme {
        WrongNoteTabContent(allNotes = sampleNotes)
    }
}
