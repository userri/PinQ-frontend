package com.example.pinq_frontend.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.data.model.AttemptItem
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.QuizOption
import com.example.pinq_frontend.data.repository.AnswerResult
import com.example.pinq_frontend.R
import com.example.pinq_frontend.ui.library.AttemptItemCard

/**
 * 세션 결과 화면에서 진입하는 "이번 회차 오답노트" 화면.
 *
 * 이 화면은 방금 푼 회차의 결과만 보여주므로 네트워크 호출 없이
 * 메모리(quizzes + answerHistory) 만으로 렌더링한다.
 *
 * 누적 오답노트(하단 탭) 는 서버 기반의 [com.example.pinq_frontend.ui.library.WrongNoteTabRoute] 가 담당.
 *
 * 북마크 토글 콜백을 받아 카드의 ⭐ 버튼 클릭 시 호출한다.
 */
@Composable
fun WrongNoteScreen(
    quizzes: List<Quiz>,
    answerHistory: List<AnswerResult>,
    onBack: () -> Unit,
    onToggleBookmark: (Long, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val wrongItems: List<AttemptItem> = remember(quizzes, answerHistory) {
        quizzes.zip(answerHistory)
            .filter { (_, answer) -> !answer.isCorrect }
            .map { (quiz, answer) -> buildAttemptItem(quiz, answer) }
    }

    // 로컬 북마크 상태 — wrongItems 는 세션 메모리 기반이라 bookmarked 가 항상 false.
    // 북마크 버튼을 누르면 여기서 즉시(낙관적) 반영하고, 서버 호출은 부모 콜백에 위임.
    val localBookmarks = remember { mutableStateMapOf<Long, Boolean>() }

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
                text = if (wrongItems.isEmpty()) "모두 맞혔어요" else "${wrongItems.size}개 틀렸어요",
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
                    Image(
                        painter = painterResource(R.drawable.ic_trophy),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "오답이 없어요",
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
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                items(wrongItems, key = { it.quizId }) { item ->
                    val isBookmarked = localBookmarks[item.quizId] ?: item.bookmarked
                    AttemptItemCard(
                        item = item.copy(bookmarked = isBookmarked),
                        onToggleBookmark = {
                            val current = localBookmarks[item.quizId] ?: item.bookmarked
                            localBookmarks[item.quizId] = !current
                            onToggleBookmark(item.quizId, current)
                        },
                    )
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

/**
 * 세션 메모리 데이터(Quiz + AnswerResult) 를 화면용 [AttemptItem] 으로 변환.
 *
 * 이 매핑은 세션 내부 화면에서만 사용한다. 누적 오답노트/북마크는 서버 DTO 를 그대로 매핑한다.
 */
private fun buildAttemptItem(quiz: Quiz, answer: AnswerResult): AttemptItem = AttemptItem(
    quizId = quiz.id,
    category = quiz.category,
    question = quiz.question,
    choices = quiz.options.map {
        QuizOption(id = it.id, optionNumber = it.optionNumber, text = it.text)
    },
    selectedChoiceId = answer.selectedOptionId,
    correctChoiceId = answer.correctOptionId,
    correct = answer.isCorrect,
    explanation = answer.explanation,
    keyword = answer.keyword,
    article = answer.relatedArticle.takeIf { it.url.isNotBlank() },
    bookmarked = false, // 세션 직후엔 알 수 없으므로 일단 false. 토글 시 서버에서 동기화.
    solvedAtIso = null,
)
