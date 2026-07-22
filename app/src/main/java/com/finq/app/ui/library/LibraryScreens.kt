package com.finq.app.ui.library

import androidx.compose.animation.animateColorAsState
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
    // 다중 선택 — 빈 셋이면 "전체". 선택된 카테고리들의 합집합(OR)을 보여준다.
    var selectedCategories by remember { mutableStateOf<Set<Category>>(emptySet()) }

    val filtered = remember(items, selectedCategories) {
        if (selectedCategories.isEmpty()) items
        else items.filter { it.category in selectedCategories }
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

        // 카테고리 필터칩 (다중 선택)
        CategoryFilterRow(
            selected = selectedCategories,
            onToggle = { cat ->
                val next = if (cat in selectedCategories) selectedCategories - cat
                else selectedCategories + cat
                // 전부 선택하면 "전체"와 같으므로 자동 환원
                selectedCategories =
                    if (next.containsAll(Category.selectable)) emptySet() else next
            },
            onClear = { selectedCategories = emptySet() },
        )
        extraFilterRow?.invoke()

        when {
            isLoading -> LoadingState()
            error != null -> ErrorState(message = error, onRetry = onRetry)
            // 원본은 있는데 필터 결과만 빈 경우 — 일반 빈 상태와 구분해 안내
            filtered.isEmpty() && items.isNotEmpty() -> EmptyState(
                iconRes = emptyIconRes,
                message = "선택한 카테고리에는 문제가 없어요",
            )
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

/**
 * 카테고리 다중 선택 칩 Row.
 *
 * "전체"는 선택 초기화 버튼처럼 동작하고(빈 셋 = 전체), 나머지 칩은 탭할 때마다
 * 켜고 끌 수 있다. 여러 개를 켜면 그 카테고리들의 합집합이 보인다.
 */
@Composable
private fun CategoryFilterRow(
    selected: Set<Category>,
    onToggle: (Category) -> Unit,
    onClear: () -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(
                label = "전체",
                isSelected = selected.isEmpty(),
                onClick = onClear,
            )
        }
        // enum 에서 동적 생성 — 카테고리가 추가돼도(예: INFLATION) 여기 수정 없이 자동 반영된다.
        items(Category.selectable, key = { it.name }) { cat ->
            val isSelected = cat in selected
            FilterChip(
                // 선택된 칩은 ✓ 로 "여러 개 켜져 있음"을 한눈에 보여준다.
                label = if (isSelected) "✓ ${cat.displayName}" else cat.displayName,
                isSelected = isSelected,
                onClick = { onToggle(cat) },
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (isSelected) Lime else MaterialTheme.colorScheme.surfaceVariant,
        label = "chipBg",
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable { onClick() }
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
