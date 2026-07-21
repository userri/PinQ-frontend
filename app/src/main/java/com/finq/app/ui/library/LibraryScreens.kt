package com.finq.app.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime

/**
 * 오답노트 / 북마크 화면 공용 본문.
 *
 *  - 헤더(타이틀 + 보조 문구)
 *  - 카테고리 필터칩 (전체 / 금리 / 환율 / 증시 / 부동산)
 *  - 항목 리스트 또는 빈/로딩/에러 상태
 *
 * 두 화면이 거의 동일한 구조라 한 컴포저블로 묶었다.
 */
@Composable
fun LibraryListScreen(
    title: String,
    subtitle: String,
    items: List<AttemptItem>,
    isLoading: Boolean,
    error: String?,
    emptyMessage: String,
    emptyIconRes: Int,
    onRetry: () -> Unit,
    onToggleBookmark: (AttemptItem) -> Unit,
    /** 미풀이 북마크 탭 → 풀이 화면 진입. null 이면 비활성. */
    onStartQuiz: ((AttemptItem) -> Unit)? = null,
    /** 카테고리칩 아래 추가 필터 Row (오답노트의 복습 필터칩). null 이면 없음. */
    extraFilterRow: (@Composable () -> Unit)? = null,
    /** 정원 나무 딥링크 — 진입 시 이 문제로 스크롤하고 카드를 펼친다. */
    focusQuizId: Long? = null,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val filtered = remember(items, selectedCategory) {
        if (selectedCategory == null) items
        else items.filter { it.category == selectedCategory }
    }

    val listState = rememberLazyListState()
    // 정원 딥링크 — 목록이 준비되면 해당 문제로 1회 스크롤. 목록에 없으면 조용히 무시.
    LaunchedEffect(focusQuizId, items) {
        val index = focusQuizId?.let { id -> filtered.indexOfFirst { it.quizId == id } } ?: -1
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 카테고리 필터칩
        CategoryFilterRow(
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
        )
        extraFilterRow?.invoke()

        when {
            isLoading -> LoadingState()
            error != null -> ErrorState(message = error, onRetry = onRetry)
            filtered.isEmpty() -> EmptyState(iconRes = emptyIconRes, message = emptyMessage)
            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                items(filtered, key = { it.quizId }) { item ->
                    AttemptItemCard(
                        item = item,
                        onToggleBookmark = { onToggleBookmark(item) },
                        onStartQuiz = onStartQuiz?.let { cb -> { cb(item) } },
                        initialExpanded = item.quizId == focusQuizId,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selected: Category?,
    onSelect: (Category?) -> Unit,
) {
    // enum 에서 동적 생성 — 카테고리가 추가돼도(예: INFLATION) 여기 수정 없이 자동 반영된다.
    val filters: List<Pair<String, Category?>> =
        listOf<Pair<String, Category?>>("전체" to null) +
            Category.selectable.map { it.displayName to it }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters) { (label, cat) ->
            val isSelected = cat == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Lime else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) OnLime else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "불러오지 못했어요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("다시 시도") }
        }
    }
}

@Composable
private fun EmptyState(iconRes: Int, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
