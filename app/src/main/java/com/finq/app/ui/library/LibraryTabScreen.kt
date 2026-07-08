package com.finq.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.finq.app.R
import kotlinx.coroutines.launch
import com.finq.app.ui.theme.Lime

/**
 * 보관함 탭 — 오답노트 / 북마크 / 전체이력을 하나의 화면으로 묶는다.
 *
 * 탭 전환 시 각 ViewModel 이 독립적으로 데이터를 로드한다.
 * 하단 네비게이션 "보관함" 탭 진입 시 이 화면이 표시된다.
 */
@Composable
fun LibraryTabScreen(
    wrongNoteViewModel: LibraryViewModel,
    bookmarkViewModel: LibraryViewModel,
    historyViewModel: LibraryViewModel,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf("오답노트", "북마크", "전체이력")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val selectedTab = pagerState.currentPage

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Lime,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Lime,
                )
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Lime
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> WrongNoteTabRoute(
                    viewModel = wrongNoteViewModel,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize(),
                )
                1 -> BookmarkTabRoute(
                    viewModel = bookmarkViewModel,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize(),
                )
                2 -> AttemptHistoryTabContent(
                    viewModel = historyViewModel,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * 전체 풀이 이력 탭 내용 — 뒤로가기 버튼 없이 탭 내에서 표시.
 */
@Composable
private fun AttemptHistoryTabContent(
    viewModel: LibraryViewModel,
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

    LibraryListScreen(
        title = "전체 풀이 이력",
        subtitle = if (state.attempts.isEmpty()) "" else "${state.attempts.size}문제 풀어봤어요",
        items = state.attempts,
        isLoading = state.isLoadingAttempts,
        error = state.attemptsError,
        emptyMessage = "아직 풀어본 문제가 없어요",
        emptyIconRes = R.drawable.ic_tab_book,
        onRetry = viewModel::loadAttempts,
        onToggleBookmark = { item -> viewModel.toggleBookmark(item.quizId, item.bookmarked) },
        modifier = modifier,
    )
}
