package com.finq.app.ui.library

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.RelatedArticle
import kotlinx.coroutines.launch

/**
 * 보관함 상세 라우트 — 목록 요약으로 헤더를 먼저 세우고 본문은 지연 로드한다.
 *
 * 목록 응답은 선지·해설·기사를 담지 않는다(요약). 예전엔 카드를 펼치는 순간
 * 상세를 가져왔지만, 이제 트리거가 **화면 진입**이라 로딩·에러·재시도도 이 화면이 갖는다.
 * 정원 딥링크처럼 목록을 거치지 않고 들어오면 요약조차 없으므로 헤더도 상세를 기다린다.
 */
@Composable
fun AttemptDetailRoute(
    quizId: Long,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onArticleClick: (RelatedArticle) -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    val summary = state.findItem(quizId)
    var detail by remember(quizId) { mutableStateOf<AttemptItem?>(null) }
    var isLoading by remember(quizId) { mutableStateOf(false) }
    var error by remember(quizId) { mutableStateOf<String?>(null) }
    var retryTick by remember(quizId) { mutableStateOf(0) }

    // 요약에 선지가 실려 오는 구서버 응답이면 그대로 쓴다 — 다시 받을 이유가 없다.
    val summaryHasDetail = summary != null && summary.choices.isNotEmpty()
    val shown = detail ?: summary

    LaunchedEffect(quizId, summaryHasDetail, retryTick) {
        if (detail != null || summaryHasDetail) return@LaunchedEffect
        isLoading = true
        error = null
        runCatching { viewModel.fetchDetail(quizId) }
            .onSuccess { detail = it }
            .onFailure { error = it.message ?: "문제를 불러오지 못했어요" }
        isLoading = false
    }

    // 북마크 — 목록 밖에서도 켜고 끌 수 있어야 하므로 로컬 상태를 우선하고,
    // 서버 반영이 실패하면 되돌린다(목록 쪽 롤백은 ViewModel 이 한다).
    var bookmarkOverride by remember(quizId) { mutableStateOf<Boolean?>(null) }
    val bookmarked = bookmarkOverride ?: shown?.bookmarked ?: false

    LaunchedEffect(state.toggleError) {
        val msg = state.toggleError
        if (msg != null && snackbarHostState != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToggleError()
        }
    }

    AttemptDetailScreen(
        item = shown,
        detailReady = detail != null || summaryHasDetail,
        isLoading = isLoading,
        error = error,
        bookmarked = bookmarked,
        onToggleBookmark = {
            val next = !bookmarked
            bookmarkOverride = next
            scope.launch {
                if (!viewModel.setBookmark(quizId, next)) bookmarkOverride = !next
            }
        },
        onRetry = { retryTick++ },
        onBack = onBack,
        onArticleClick = onArticleClick,
        modifier = modifier,
    )
}
