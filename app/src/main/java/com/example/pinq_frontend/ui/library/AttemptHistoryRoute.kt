package com.example.pinq_frontend.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.R

/**
 * 마이페이지에서 진입하는 "전체 풀이 이력" 화면.
 *
 *  - 풀어본 모든 문제(정답 + 오답)를 최신순으로 표시.
 *  - 안 푼 날은 표시하지 않음 — 시도가 있던 문제만 카드로 나옴.
 *  - 사용자는 여기서 어느 문제든 ⭐ 로 북마크 가능.
 */
@Composable
fun AttemptHistoryRoute(
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAttempts() }

    LaunchedEffect(state.toggleError) {
        val msg = state.toggleError
        if (msg != null && snackbarHostState != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToggleError()
        }
    }

    Column(modifier = modifier) {
        // 상단 바
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로",
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(4.dp))
        }

        LibraryListScreen(
            title = "전체 풀이 이력",
            subtitle = if (state.attempts.isEmpty()) ""
            else "${state.attempts.size}문제 풀어봤어요",
            items = state.attempts,
            isLoading = state.isLoadingAttempts,
            error = state.attemptsError,
            emptyMessage = "아직 풀어본 문제가 없어요",
            emptyIconRes = R.drawable.ic_tab_book,
            onRetry = viewModel::loadAttempts,
            onToggleBookmark = { item ->
                viewModel.toggleBookmark(item.quizId, item.bookmarked)
            },
            modifier = Modifier.weight(1f),
        )
    }
}
