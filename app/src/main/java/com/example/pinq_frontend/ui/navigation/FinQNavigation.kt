package com.example.pinq_frontend.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.pinq_frontend.data.repository.ApiLibraryRepository
import com.example.pinq_frontend.data.repository.ApiQuizRepository
import com.example.pinq_frontend.data.repository.ApiUserStatsRepository
import com.example.pinq_frontend.data.repository.LibraryRepository
import com.example.pinq_frontend.data.repository.QuizRepository
import com.example.pinq_frontend.data.repository.UserStatsRepository
import com.example.pinq_frontend.ui.home.HomeViewModel
import com.example.pinq_frontend.ui.library.AttemptHistoryRoute
import com.example.pinq_frontend.ui.library.LibraryTabScreen
import com.example.pinq_frontend.ui.library.LibraryViewModel
import com.example.pinq_frontend.ui.mypage.MyPageViewModel
import com.example.pinq_frontend.ui.quiz.QuizSessionViewModel
import com.example.pinq_frontend.ui.screen.HomeScreen
import com.example.pinq_frontend.ui.screen.MyPageScreen
import com.example.pinq_frontend.ui.screen.QuizAnswerScreen
import com.example.pinq_frontend.ui.screen.QuizScreen
import com.example.pinq_frontend.ui.screen.ResultReportScreen
import com.example.pinq_frontend.ui.screen.WrongNoteScreen
import com.example.pinq_frontend.ui.theme.PinQBlue
import com.example.pinq_frontend.data.local.SessionManager
import com.example.pinq_frontend.data.repository.AuthRepository
import com.example.pinq_frontend.ui.login.LoginEvent
import com.example.pinq_frontend.ui.login.LoginViewModel
import com.example.pinq_frontend.ui.screen.LoginScreen

/**
 * FinQ 네비게이션 그래프.
 *
 * 구조 (Phase 4):
 *   login                   — 미인증 시 시작점 (하단 네비게이션 없음)
 *   home                  ─┐
 *   library_tab            ├─ 하단 네비게이션 표시 영역 (3탭)
 *   mypage                ─┘
 *   session (nested graph)  — 하단 네비게이션 숨김
 *     ├── session/quiz
 *     ├── session/answer
 *     ├── session/done
 *     └── session/wrongnote
 */
object FinQRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val WRONG_NOTE_TAB = "wrongnote_tab"   // 하위 호환용 — 직접 접근 시 library_tab 으로 리다이렉트
    const val BOOKMARK_TAB = "bookmark_tab"       // 하위 호환용
    const val LIBRARY_TAB = "library_tab"
    const val MY_PAGE = "mypage"
    const val ATTEMPT_HISTORY = "attempt_history"
    const val SESSION_GRAPH = "session"
    const val QUIZ = "session/quiz"
    const val ANSWER = "session/answer"
    const val DONE = "session/done"
    const val WRONG_NOTE = "session/wrongnote"
}

// 하단 네비게이션을 표시하는 최상위 루트 목록
private val bottomNavRoutes = setOf(
    FinQRoutes.HOME,
    FinQRoutes.LIBRARY_TAB,
    FinQRoutes.MY_PAGE,
)

data class BottomNavItem(
    val route: String,
    val label: String,
    val emoji: String,
)

private val bottomNavItems = listOf(
    BottomNavItem(FinQRoutes.HOME, "홈", "🏠"),
    BottomNavItem(FinQRoutes.LIBRARY_TAB, "보관함", "📚"),
    BottomNavItem(FinQRoutes.MY_PAGE, "마이", "👤"),
)

@Composable
fun FinQNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val repository: QuizRepository = remember { ApiQuizRepository(NetworkModule.quizApi) }
    val statsRepository: UserStatsRepository = remember { ApiUserStatsRepository(NetworkModule.userApi) }
    val authRepository: AuthRepository = remember { AuthRepository(NetworkModule.authApi) }
    val libraryRepository: LibraryRepository = remember { ApiLibraryRepository(NetworkModule.libraryApi) }
    val context = LocalContext.current

    // 로그인 여부에 따라 시작 화면 결정
    val startDestination = if (SessionManager.isLoggedIn) FinQRoutes.HOME else FinQRoutes.LOGIN

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                FinQBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── 로그인 ────────────────────────────────────────────────────
            composable(FinQRoutes.LOGIN) {
                val loginVm: LoginViewModel = viewModel(
                    factory = LoginViewModel.factory(authRepository),
                )
                val state by loginVm.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    loginVm.events.collect { event ->
                        if (event is LoginEvent.LoginSuccess) {
                            navController.navigate(FinQRoutes.HOME) {
                                popUpTo(FinQRoutes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                }

                LoginScreen(
                    isLoading = state.isLoading,
                    error = state.error,
                    onKakaoLogin = { loginVm.loginWithKakao(context) },
                    onGoogleLogin = { loginVm.loginWithGoogle(context) },
                    onClearError = loginVm::clearError,
                )
            }

            // ── 홈 ──────────────────────────────────────────────────────
            composable(FinQRoutes.HOME) {
                val homeVm: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(repository, statsRepository),
                )
                val state by homeVm.uiState.collectAsState()
                LaunchedEffect(Unit) { homeVm.loadQuizInfo() }
                HomeScreen(
                    quizCount = state.quizCount,
                    streak = state.streak,
                    activityGrid = state.activityGrid,
                    isLoading = state.isLoading,
                    error = state.error,
                    nickname = state.nickname,
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

            // ── 보관함 탭 (오답노트 + 북마크 + 전체이력) ────────────────
            composable(FinQRoutes.LIBRARY_TAB) {
                val wrongNoteVm = libraryViewModel(libraryRepository)
                val bookmarkVm  = libraryViewModel(libraryRepository)
                val historyVm   = libraryViewModel(libraryRepository)
                LibraryTabScreen(
                    wrongNoteViewModel = wrongNoteVm,
                    bookmarkViewModel  = bookmarkVm,
                    historyViewModel   = historyVm,
                    snackbarHostState  = snackbarHostState,
                )
            }

            // ── 마이페이지 ───────────────────────────────────────────────
            composable(FinQRoutes.MY_PAGE) {
                val myPageVm: MyPageViewModel = viewModel(
                    factory = MyPageViewModel.factory(statsRepository),
                )
                val state by myPageVm.uiState.collectAsState()

                LaunchedEffect(Unit) { myPageVm.loadStats() }

                LaunchedEffect(Unit) {
                    myPageVm.withdrawEvents.collect {
                        SessionManager.clearSession()
                        navController.navigate(FinQRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    myPageVm.logoutEvents.collect {
                        authRepository.logout()
                        navController.navigate(FinQRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                MyPageScreen(
                    nickname = state.nickname,
                    streak = state.streak,
                    totalSolved = state.totalSolved,
                    correctRate = state.correctRate,
                    activityGrid = state.activityGrid,
                    appVersion = BuildConfig.VERSION_NAME,
                    isLoading = state.isLoading,
                    error = state.error,
                    onRetry = myPageVm::loadStats,
                    isWithdrawing = state.isWithdrawing,
                    onWithdraw = myPageVm::withdraw,
                    withdrawError = state.withdrawError,
                    onClearWithdrawError = myPageVm::clearWithdrawError,
                    onLogout = myPageVm::logout,
                    isUpdatingNickname = state.isUpdatingNickname,
                    nicknameUpdateError = state.nicknameUpdateError,
                    onUpdateNickname = myPageVm::updateNickname,
                    onClearNicknameUpdateError = myPageVm::clearNicknameUpdateError,
                )
            }

            // ── 전체 풀이 이력 (마이페이지에서 진입) ──────────────────
            composable(FinQRoutes.ATTEMPT_HISTORY) {
                val libraryVm = libraryViewModel(libraryRepository)
                AttemptHistoryRoute(
                    viewModel = libraryVm,
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState,
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
                        libraryRepository = libraryRepository,
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
                            try {
                                localContext.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    localContext,
                                    "기사를 열 수 있는 앱이 없어요",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                    )
                }
                composable(FinQRoutes.DONE) { entry ->
                    val vm = entry.sessionViewModel(navController, repository)
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
                        libraryRepository = libraryRepository,
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
        windowInsets = WindowInsets(0),
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

/** 탭마다 새 인스턴스가 만들어지므로 데이터를 캐싱하지 않는다. 화면 진입 시 명시 로드. */
@Composable
private fun libraryViewModel(repository: LibraryRepository): LibraryViewModel {
    val factory = remember(repository) { LibraryViewModel.factory(repository) }
    return viewModel(factory = factory)
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
    libraryRepository: LibraryRepository,
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
            libraryRepository = libraryRepository,
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
    libraryRepository: LibraryRepository,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    // 세션 내부 화면에서도 북마크 토글 가능 — 토글 결과는 즉시 서버 반영.
    // 화면 자체는 메모리 데이터로 그리므로 별도 로드 없음.
    val libraryVm = libraryViewModel(libraryRepository)
    WrongNoteScreen(
        quizzes = state.quizzes,
        answerHistory = state.answerHistory,
        onBack = onBack,
        onToggleBookmark = { quizId, currentlyBookmarked ->
            libraryVm.toggleBookmark(quizId, currentlyBookmarked)
        },
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
