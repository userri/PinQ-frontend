package com.finq.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime

/**
 * 하단 네비게이션 "오답노트" 탭 진입 화면.
 *
 *  - 화면 진입 시마다 최신 데이터 로드
 *  - 카드의 ⭐ 버튼으로 북마크 토글
 *  - 복습 상태 필터칩(전체/오답만/복습중/졸업) — 옛 정원 목록 기능의 이관처
 */
@Composable
fun WrongNoteTabRoute(
    viewModel: LibraryViewModel,
    snackbarHostState: SnackbarHostState? = null,
    /** 정원 나무 딥링크 — 해당 문제로 스크롤·펼침. 필터는 기본값(전체)이라 졸업 항목도 보인다. */
    focusQuizId: Long? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var reviewFilter by remember { mutableStateOf(ReviewFilter.ALL) }

    LaunchedEffect(Unit) { viewModel.loadWrongNotes() }

    // 토글 실패 시 사용자에게 알려주고 상태 클리어
    LaunchedEffect(state.toggleError) {
        val msg = state.toggleError
        if (msg != null && snackbarHostState != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToggleError()
        }
    }

    val filtered = remember(state.wrongNotes, reviewFilter) {
        state.wrongNotes.applyReviewFilter(reviewFilter)
    }

    LibraryListScreen(
        title = "오답노트",
        subtitle = if (filtered.isEmpty()) "" else "${filtered.size}문제",
        items = filtered,
        isLoading = state.isLoadingWrong,
        error = state.wrongError,
        emptyMessage = when (reviewFilter) {
            ReviewFilter.ALL -> "오답이 없어요"
            ReviewFilter.GROWING -> "자라는 중인 복습이 없어요"
            ReviewFilter.GRADUATED -> "아직 완성한 나무가 없어요"
        },
        emptyIconRes = R.drawable.ic_trophy,
        onRetry = viewModel::loadWrongNotes,
        onToggleBookmark = { item -> viewModel.toggleBookmark(item.quizId, item.bookmarked) },
        focusQuizId = focusQuizId,
        onLoadDetail = viewModel::fetchDetail,
        showTitle = false,
        extraFilterRow = {
            ReviewFilterRow(selected = reviewFilter, onSelect = { reviewFilter = it })
        },
        modifier = modifier,
    )
}

/**
 * 복습 상태 칩 — 카운트 줄 우측에 인라인으로 붙는다.
 * 배경·너비는 부모(카운트 Row)가 제공하므로 여기선 칩만 그린다.
 */
@Composable
private fun ReviewFilterRow(selected: ReviewFilter, onSelect: (ReviewFilter) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReviewFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Lime else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) OnLime else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
