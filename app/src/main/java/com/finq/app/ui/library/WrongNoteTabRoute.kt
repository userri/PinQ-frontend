package com.finq.app.ui.library

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.finq.app.R

/**
 * 하단 네비게이션 "오답노트" 탭 진입 화면.
 *
 *  - 화면 진입 시마다 최신 데이터 로드
 *  - 카드의 ⭐ 버튼으로 북마크 토글
 */
@Composable
fun WrongNoteTabRoute(
    viewModel: LibraryViewModel,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadWrongNotes() }

    // 토글 실패 시 사용자에게 알려주고 상태 클리어
    LaunchedEffect(state.toggleError) {
        val msg = state.toggleError
        if (msg != null && snackbarHostState != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToggleError()
        }
    }

    LibraryListScreen(
        title = "오답노트",
        subtitle = if (state.wrongNotes.isEmpty()) "" else "${state.wrongNotes.size}문제",
        items = state.wrongNotes,
        isLoading = state.isLoadingWrong,
        error = state.wrongError,
        emptyMessage = "오답이 없어요",
        emptyIconRes = R.drawable.ic_trophy,
        onRetry = viewModel::loadWrongNotes,
        onToggleBookmark = { item -> viewModel.toggleBookmark(item.quizId, item.bookmarked) },
        modifier = modifier,
    )
}
