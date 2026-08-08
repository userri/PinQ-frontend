package com.finq.app.ui.library

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.finq.app.R
import com.finq.app.data.model.AttemptItem

/**
 * 하단 네비게이션 "북마크" 탭 진입 화면.
 *
 * 정책 메모: 북마크 해제 시 데이터가 사라지지 않는다.
 *  - 서버에는 풀이 이력(UserQuizAttempt) 이 영구 저장돼있으므로,
 *    해제된 문제는 마이페이지의 "전체 풀이 이력" 또는 "오답노트" 에서 다시 찾아
 *    언제든 북마크 복구 가능.
 */
@Composable
fun BookmarkTabRoute(
    viewModel: LibraryViewModel,
    snackbarHostState: SnackbarHostState? = null,
    /** 행 탭 → 상세 화면. */
    onOpenDetail: (AttemptItem) -> Unit,
    /** 미풀이 북마크 탭 → 그 문제의 단건 풀이 화면 진입. */
    onStartQuiz: ((AttemptItem) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadBookmarks() }

    LaunchedEffect(state.toggleError) {
        val msg = state.toggleError
        if (msg != null && snackbarHostState != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToggleError()
        }
    }

    LibraryListScreen(
        title = "북마크",
        subtitle = if (state.bookmarks.isEmpty()) "" else "${state.bookmarks.size}문제",
        items = state.bookmarks,
        isLoading = state.isLoadingBookmark,
        error = state.bookmarkError,
        emptyMessage = "다시 보고 싶은 문제를 별 아이콘으로 저장해보세요",
        emptyIconRes = R.drawable.ic_bookmark_star_filled,
        onRetry = viewModel::loadBookmarks,
        onToggleBookmark = { item -> viewModel.toggleBookmark(item.quizId, item.bookmarked) },
        onOpenDetail = onOpenDetail,
        onStartQuiz = onStartQuiz,
        // 이 화면의 서버 정렬 축은 **담은 시각**이다. 날짜 열도 같은 값을 찍어야
        // 순서가 설명된다 — 푼 날짜를 찍으면 미풀이는 빈칸이고 나머지는 정렬과
        // 무관한 날짜라 목록이 뒤죽박죽으로 읽힌다.
        dateAxis = AttemptDateAxis.BOOKMARKED,
        sortLabel = "담은 날짜순",
        showTitle = false,
        modifier = modifier,
    )
}
