package com.finq.app.ui.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary

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
    /** 행 탭 → 상세 화면 진입. */
    onOpenDetail: (AttemptItem) -> Unit,
    /** 미풀이 북마크 탭 → 풀이 화면 진입. null 이면 비활성. */
    onStartQuiz: ((AttemptItem) -> Unit)? = null,
    /** 카운트 줄 우측에 붙는 추가 필터(오답노트의 복습 필터칩). null 이면 없음. */
    extraFilterRow: (@Composable () -> Unit)? = null,
    /**
     * 대제목 노출 여부. 탭 안(오답노트/북마크/전체이력)에선 탭 라벨과 중복이라 false —
     * 카운트만 얇은 줄로 보여준다. 독립 화면(뒤로가기 있는 전체이력)에선 true.
     */
    showTitle: Boolean = true,
    /**
     * 행 첫 줄에 무엇을 세울지. 기본은 상태(정답/오답) — 정답·오답이 섞인 화면 기준.
     * 오답노트처럼 모든 항목이 오답인 화면은 [AttemptCardEmphasis.CATEGORY] 를 넘긴다.
     */
    cardEmphasis: AttemptCardEmphasis = AttemptCardEmphasis.STATUS,
    /** 날짜 열의 축 — 그 화면의 정렬 축과 같은 값을 넘긴다. [AttemptDateAxis] */
    dateAxis: AttemptDateAxis = AttemptDateAxis.SOLVED,
    /**
     * 정렬 축 한 마디(`푼 날짜순` · `담은 날짜순`). 카운트 뒤에 이어 붙는다.
     *
     * 목록마다 날짜 열의 **뜻이 다르다** — 오답노트·전체이력은 푼 날, 북마크는 담은
     * 날이다. 각 화면 안에서는 정렬 축과 맞아 순서가 설명되지만, 사용자는 탭을
     * 옮겨다니며 보므로 같은 자리의 숫자가 언제 뜻이 바뀌는지 알 수가 없었다.
     *
     * 정렬을 말하면 날짜 뜻이 따라온다 — 그래서 "날짜 = 담은 날"이 아니라 "담은
     * 날짜순"이라고 쓴다. 두 문구 다 서버 정렬과 대조해 확인한 참말이다(오답노트·
     * 전체이력은 `UserQuizAttempt.createdAt DESC` 이고 응답의 `solvedAt` 이 같은 컬럼,
     * 북마크는 `bookmarkedAt DESC`).
     *
     * 카운트 줄 **오른쪽 끝**은 쓸 수 없다 — 오답노트가 그 자리에 복습 필터를 갖고
     * 있어서, 오른쪽에 두면 화면마다 라벨 자리가 달라진다.
     */
    sortLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    // 다중 선택 — 빈 셋이면 "전체". 선택된 카테고리들의 합집합(OR)을 보여준다.
    var selectedCategories by remember { mutableStateOf<Set<Category>>(emptySet()) }

    val filtered = remember(items, selectedCategories) {
        if (selectedCategories.isEmpty()) items
        else items.filter { it.category in selectedCategories }
    }

    // rememberLazyListState 는 saveable — 상세를 보고 돌아와도 보던 자리로 복귀한다.
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (showTitle) {
            // 독립 화면용 — 대제목 + 카운트 (탭이 없어 제목이 필요한 경우)
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
                        text = if (sortLabel != null) "$subtitle  ·  $sortLabel" else subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            // 탭 안 — 대제목은 탭 라벨과 중복이라 생략. 카운트(좌) + 복습필터(우)를 한 줄에.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (sortLabel != null && subtitle.isNotBlank()) {
                    // 카운트보다 한 층 아래로 — 굵기를 빼고 더 흐리게. 개수가 먼저
                    // 읽히고 축은 물어볼 때 눈에 들어오면 된다.
                    Text(
                        text = "  ·  $sortLabel",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.weight(1f))
                extraFilterRow?.invoke()
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
        // showTitle 모드에선 복습필터가 카운트 줄에 못 붙으므로 카테고리 아래에 둔다(폴백).
        if (showTitle) extraFilterRow?.invoke()

        when {
            isLoading -> LoadingState()
            error != null -> AttemptLoadErrorState(onRetry = onRetry)
            // 원본은 있는데 필터 결과만 빈 경우 — 일반 빈 상태와 구분해 안내
            filtered.isEmpty() && items.isNotEmpty() -> EmptyState(
                iconRes = emptyIconRes,
                message = "선택한 카테고리에는 문제가 없어요",
            )
            filtered.isEmpty() -> EmptyState(iconRes = emptyIconRes, message = emptyMessage)
            // 목록은 색인 — 카드가 아니라 구분선으로 가른 얇은 행을 촘촘히 쌓는다.
            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(filtered, key = { it.quizId }) { item ->
                    AttemptItemRow(
                        item = item,
                        emphasis = cardEmphasis,
                        onToggleBookmark = { onToggleBookmark(item) },
                        onOpenDetail = { onOpenDetail(item) },
                        onStartQuiz = onStartQuiz?.let { cb -> { cb(item) } },
                        dateAxis = dateAxis,
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Outline,
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
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
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

/**
 * 목록·상세가 공유하는 로드 실패 화면.
 *
 * 예외 메시지(`Unable to resolve host "…"`)는 싣지 않는다 — 영어 스택 문구를 읽어도
 * 사용자가 할 수 있는 일은 "다시 시도" 하나뿐이라 정보가 아니라 소음이다.
 * 두 화면이 같은 컴포저블을 쓰므로 문구도 버튼 모양도 갈라지지 않는다.
 */
@Composable
internal fun AttemptLoadErrorState(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "불러오지 못했어요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lime,
                    contentColor = OnLime,
                ),
            ) { Text("다시 시도", fontWeight = FontWeight.Bold) }
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
