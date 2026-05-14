package com.example.pinq_frontend.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.pinq_frontend.BuildConfig
import com.example.pinq_frontend.data.model.RelatedArticle
import com.example.pinq_frontend.data.remote.NetworkModule
import com.example.pinq_frontend.data.repository.ApiQuizRepository
import com.example.pinq_frontend.data.repository.QuizRepository
import com.example.pinq_frontend.ui.home.HomeViewModel
import com.example.pinq_frontend.ui.quiz.QuizSessionViewModel
import com.example.pinq_frontend.ui.screen.HomeScreen
import com.example.pinq_frontend.ui.screen.MyPageScreen
import com.example.pinq_frontend.ui.screen.QuizAnswerScreen
import com.example.pinq_frontend.ui.screen.QuizScreen
import com.example.pinq_frontend.ui.screen.ResultReportScreen
import com.example.pinq_frontend.ui.screen.WrongNoteScreen
import com.example.pinq_frontend.ui.screen.WrongNoteTabScreen
import com.example.pinq_frontend.ui.theme.PinQBlue
import com.example.pinq_frontend.ui.wrongnote.WrongNoteViewModel

/**
 * FinQ 네비게이션 그래프.
 *
 * 구조:
 *   home                ─┐
 *   wrongnote_tab        ├─ 하단 네비게이션 표시 영역
 *   mypage              ─┘
 *   session (nested graph) — 하단 네비게이션 숨김
 *     ├── session/quiz
 *     ├── session/answer
 *     ├── session/done
 *     └── session/wrongnote
 */
object FinQRoutes {
    const val HOME = "home"
    const val WRONG_NOTE_TAB = "wrongnote_tab"
    const val MY_PAGE = "mypage"
    const val SESSION_GRAPH = "session"
    const val QUIZ = "session/quiz"
    const val ANSWER = "session/answer"
    const val DONE = "session/done"
    const val WRONG_NOTE = "session/wrongnote"
}

// 하단 네비게이션을 표시하는 최상위 루트 목록
private val bottomNavRoutes = setOf(
    FinQRoutes.HOME,
    FinQRoutes.WRONG_NOTE_TAB,
    FinQRoutes.MY_PAGE,
)

data class BottomNavItem(
    val route: String,
    val label: String,
    val emoji: String,
)

private val bottomNavItems = listOf(
    BottomNavItem(FinQRoutes.HOME, "홈", "🏠"),
    BottomNavItem(FinQRoutes.WRONG_NOTE_TAB, "오답노트", "📝"),
    BottomNavItem(FinQRoutes.MY_PAGE, "마이", "👤"),
)

@Composable
fun FinQNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val repository: QuizRepository = remember { ApiQuizRepository(NetworkModule.quizApi) }
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                FinQBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // 탭 전환 시 백스택을 home 위로 팝해서 중복 누적 방지
                            popUpTo(FinQRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FinQRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── 홈 ──────────────────────────────────────────────────────
            composable(FinQRoutes.HOME) {
                val homeVm: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(repository),
                )
                val state by homeVm.uiState.collectAsState()
                HomeScreen(
                    quizCount = state.quizCount,
                    streak = state.streak,
                    isLoading = state.isLoading,
                    error = state.error,
                    onStartQuiz = { navController.navigate(FinQRoutes.SESSION_GRAPH) },
                    onRetry = homeVm::loadQuizInfo,
                    onMyPage = {
                        navController.navigate(FinQRoutes.MY_PAGE) {
                            popUpTo(FinQRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            // ── 오답노트 탭 ──────────────────────────────────────────────
            composable(FinQRoutes.WRONG_NOTE_TAB) {
                val wrongNoteVm: WrongNoteViewModel = viewModel(
                    factory = WrongNoteViewModel.factory(context),
                )
                // 화면 진입 시마다 최신 데이터 로드
                LaunchedEffect(Unit) { wrongNoteVm.refresh() }
                WrongNoteTabScreen(viewModel = wrongNoteVm)
            }

            // ── 마이페이지 ───────────────────────────────────────────────
            composable(FinQRoutes.MY_PAGE) {
                val dummyGrid = remember {
                    val pattern = listOf(
                        true, false, true, true, false, true, false,
                        false, true, true, false, true, true, true,
                    )
                    List(56) { i -> pattern[i % pattern.size] }
                }
                MyPageScreen(
                    streak = 0,
                    totalSolved = 0,
                    correctRate = 0f,
                    activityGrid = dummyGrid,
                    appVersion = BuildConfig.VERSION_NAME,
                    onBack = { navController.popBackStack() },
                )
            }

            // ── 퀴즈 세션 그래프 ──────────────────────────────────────────
            navigation(
                startDestination = FinQRoutes.QUIZ,
                route = FinQRoutes.SESSION_GRAPH,
            ) {
                composable(FinQRoutes.QUIZ) { entry ->
                    val vm = entry.sessionViewModel(navController, repository)
                    QuizRoute(
                        viewModel = vm,
                        onAfterSubmit = { navController.navigate(FinQRoutes.ANSWER) },
                    )
                }
                composable(FinQRoutes.ANSWER) { entry ->
                    val vm = entry.sessionViewModel(navController, repository)
                    val localContext = LocalContext.current
                    AnswerRoute(
                        viewModel = vm,
                        onNext = {
                            val state = vm.uiState.value
                            if (state.isLastQuiz) {
                                navController.navigate(FinQRoutes.DONE) {
                                    popUpTo(FinQRoutes.QUIZ) { inclusive = true }
                                }
                            } else {
                                vm.moveToNext()
                                navController.navigate(FinQRoutes.QUIZ) {
                                    popUpTo(FinQRoutes.QUIZ) { inclusive = true }
                                }
                            }
                        },
                        onArticleClick = { article ->
                            val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                            localContext.startActivity(intent)
                        },
                    )
                }
                composable(FinQRoutes.DONE) { entry ->
                    val vm = entry.sessionViewModel(navController, repository)
                    val store = remember { com.example.pinq_frontend.data.local.WrongNoteStore(context.applicationContext) }
                    // 세션 종료 시 오답을 영구 저장소에 기록
                    LaunchedEffect(Unit) { vm.saveWrongNotes(store) }
                    DoneRoute(
                        viewModel = vm,
                        onGoHome = {
                            navController.navigate(FinQRoutes.HOME) {
                                popUpTo(FinQRoutes.HOME) { inclusive = true }
                            }
                        },
                        onRestart = {
                            vm.restart()
                            navController.navigate(FinQRoutes.QUIZ) {
                                popUpTo(FinQRoutes.DONE) { inclusive = true }
                            }
                        },
                        onWrongNote = {
                            navController.navigate(FinQRoutes.WRONG_NOTE)
                        },
                    )
                }
                composable(FinQRoutes.WRONG_NOTE) { entry ->
                    val vm = entry.sessionViewModel(navController, repository)
                    WrongNoteRoute(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 하단 내비게이션 바
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FinQBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Text(
                        text = item.emoji,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = PinQBlue,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color(0xFFE8EFFE),
                ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 세션 ViewModel 공유 헬퍼
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 부모 그래프(`session`) 의 BackStackEntry 를 ViewModelStoreOwner 로 사용해
 * 세션 단위로 공유되는 [QuizSessionViewModel] 을 가져온다.
 */
@Composable
private fun NavBackStackEntry.sessionViewModel(
    navController: NavHostController,
    repository: QuizRepository,
): QuizSessionViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(FinQRoutes.SESSION_GRAPH)
    }
    val factory = remember(repository) { QuizSessionViewModel.factory(repository) }
    return viewModel(viewModelStoreOwner = parentEntry, factory = factory)
}

// ─────────────────────────────────────────────────────────────────────────────
// Route 래퍼
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuizRoute(
    viewModel: QuizSessionViewModel,
    onAfterSubmit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.lastAnswer, state.isSubmitting) {
        if (!state.isSubmitting && state.lastAnswer != null) {
            onAfterSubmit()
        }
    }

    when {
        state.isLoading -> LoadingBox()
        state.error != null -> ErrorBox(state.error!!) { viewModel.loadQuizzes() }
        state.currentQuiz == null -> ErrorBox("표시할 퀴즈가 없습니다.") { viewModel.loadQuizzes() }
        else -> QuizScreen(
            quizIndex = state.currentIndex,
            totalCount = state.totalCount,
            quiz = state.currentQuiz!!,
            selectedOptionId = state.selectedOptionId,
            isSubmitting = state.isSubmitting,
            onSelectOption = viewModel::selectOption,
            onSubmit = { viewModel.submitAnswer() },
        )
    }
}

@Composable
private fun AnswerRoute(
    viewModel: QuizSessionViewModel,
    onNext: () -> Unit,
    onArticleClick: (RelatedArticle) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val quiz = state.currentQuiz
    val answer = state.lastAnswer
    if (quiz == null || answer == null) {
        LoadingBox()
    } else {
        QuizAnswerScreen(
            quiz = quiz,
            answer = answer,
            isLast = state.isLastQuiz,
            onNext = onNext,
            onArticleClick = onArticleClick,
        )
    }
}

@Composable
private fun DoneRoute(
    viewModel: QuizSessionViewModel,
    onGoHome: () -> Unit,
    onRestart: () -> Unit,
    onWrongNote: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    ResultReportScreen(
        quizzes = state.quizzes,
        answerHistory = state.answerHistory,
        onGoHome = onGoHome,
        onRestart = onRestart,
        onWrongNote = onWrongNote,
    )
}

@Composable
private fun WrongNoteRoute(
    viewModel: QuizSessionViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    WrongNoteScreen(
        quizzes = state.quizzes,
        answerHistory = state.answerHistory,
        onBack = onBack,
    )
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "오류: $message",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}
