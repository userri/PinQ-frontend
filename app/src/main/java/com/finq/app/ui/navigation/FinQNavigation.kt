package com.finq.app.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.finq.app.BuildConfig
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.remote.NetworkModule
import com.finq.app.data.repository.ApiLibraryRepository
import com.finq.app.data.repository.ApiQuizRepository
import com.finq.app.data.repository.ApiUserStatsRepository
import com.finq.app.data.repository.NotificationRepository
import com.finq.app.data.repository.LibraryRepository
import com.finq.app.data.repository.QuizRepository
import com.finq.app.data.repository.UserStatsRepository
import com.finq.app.ui.home.HomeViewModel
import com.finq.app.ui.library.AttemptHistoryRoute
import com.finq.app.ui.library.LibraryTabScreen
import com.finq.app.ui.library.LibraryViewModel
import com.finq.app.ui.mypage.MyPageViewModel
import com.finq.app.ui.quiz.QuizSessionViewModel
import com.finq.app.ui.screen.HomeScreen
import com.finq.app.ui.screen.MyPageScreen
import com.finq.app.ui.screen.QuizAnswerScreen
import com.finq.app.ui.screen.QuizScreen
import com.finq.app.ui.screen.ResultReportScreen
import com.finq.app.ui.screen.WrongNoteScreen
import com.finq.app.data.local.SessionManager
import com.finq.app.data.repository.AuthRepository
import com.finq.app.ui.login.LoginEvent
import com.finq.app.ui.login.LoginViewModel
import com.finq.app.ui.screen.LoginScreen
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted

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
 *
 * [세션 중간 이탈 처리]
 *   quiz/answer 화면에서 뒤로가기 시 세션 그래프를 제거하고 홈으로 돌아간다.
 *   홈에서 다시 "풀기"를 누르면 서버의 solved 상태를 새로 받아 아직 안 푼 문제부터 시작한다.
 *   진행도는 전체 오늘 문제 기준으로 표시해, 남은 문제만 풀어도 Q3/4 같은 맥락이 유지된다.
 */
object FinQRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val WRONG_NOTE_TAB = "wrongnote_tab"
    const val BOOKMARK_TAB = "bookmark_tab"
    const val LIBRARY_TAB = "library_tab"
    const val MY_PAGE = "mypage"
    const val ATTEMPT_HISTORY = "attempt_history"
    const val SESSION_GRAPH = "session"
    const val QUIZ = "session/quiz"
    const val ANSWER = "session/answer"
    const val DONE = "session/done"
    const val WRONG_NOTE = "session/wrongnote"
}

private val bottomNavRoutes = setOf(
    FinQRoutes.HOME,
    FinQRoutes.LIBRARY_TAB,
    FinQRoutes.MY_PAGE,
)

/**
 * 세션 진행 중에는 Scaffold 컨테이너까지 풀블리드 네이비로 깐다.
 * 상태바·하단 인셋 영역도 모두 네이비로 채워져 끊김 없는 다크 톤이 유지된다.
 */
private val darkSessionRoutes = setOf(
    FinQRoutes.QUIZ,
    FinQRoutes.ANSWER,
)

data class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int,
)

private val bottomNavItems = listOf(
    BottomNavItem(FinQRoutes.HOME, "홈", com.finq.app.R.drawable.ic_tab_home),
    BottomNavItem(FinQRoutes.LIBRARY_TAB, "내 공부", com.finq.app.R.drawable.ic_tab_book),
    BottomNavItem(FinQRoutes.MY_PAGE, "마이", com.finq.app.R.drawable.ic_tab_user),
)

/**
 * 퀴즈 세션 중 뒤로가기 — 세션 그래프를 제거하고 홈으로 돌아간다.
 * 이후 onStartQuiz 는 신규 세션을 만들고 서버의 solved 상태 기준으로 미풀이 문제만 로드한다.
 */
private fun NavHostController.pauseSessionToHome() {
    navigate(FinQRoutes.HOME) {
        popUpTo(FinQRoutes.SESSION_GRAPH) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * 세션 시작 또는 복귀 — 백스택에 SESSION_GRAPH 가 이미 있으면 복귀, 없으면 신규 생성.
 * 중간 이탈은 SESSION_GRAPH 를 제거하므로 보통 신규 생성되어 최신 solved 상태를 다시 읽는다.
 */
private fun NavHostController.resumeOrStartSession() {
    val hasSession = try {
        getBackStackEntry(FinQRoutes.SESSION_GRAPH)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
    if (hasSession) {
        // SESSION_GRAPH 위에 쌓인 것(중간 이탈 시 HOME)만 제거하고 복귀.
        popBackStack(FinQRoutes.SESSION_GRAPH, inclusive = false)
    } else {
        navigate(FinQRoutes.SESSION_GRAPH)
    }
}

@Composable
fun FinQNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val repository: QuizRepository = remember { ApiQuizRepository(NetworkModule.quizApi) }
    val statsRepository: UserStatsRepository = remember { ApiUserStatsRepository(NetworkModule.userApi) }
    val authRepository: AuthRepository = remember { AuthRepository(NetworkModule.authApi) }
    val libraryRepository: LibraryRepository = remember { ApiLibraryRepository(NetworkModule.libraryApi) }
    val notificationRepository: NotificationRepository = remember { NotificationRepository(NetworkModule.userApi) }
    val context = LocalContext.current

    val startDestination = if (SessionManager.isLoggedIn) FinQRoutes.HOME else FinQRoutes.LOGIN

    // refresh token 재발급 실패로 세션이 만료되면(Authenticator 의 clearSessionSync)
    // 어느 화면에 있든 로그인 화면으로 보낸다. 이 이벤트가 없으면 사용자는
    // 401 에러 화면에 갇혀 앱을 재시작해야만 로그인할 수 있다.
    LaunchedEffect(Unit) {
        SessionManager.sessionExpiredEvents.collect {
            navController.navigate(FinQRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes
    val isDarkSession = currentRoute in darkSessionRoutes

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // 세션 화면에서는 상태바·하단 인셋까지 네이비로 풀블리드.
        containerColor = if (isDarkSession)
            com.finq.app.ui.theme.BgBase
        else
            MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                FinQBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == FinQRoutes.HOME) {
                            navController.navigate(FinQRoutes.HOME) {
                                popUpTo(FinQRoutes.HOME)
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(FinQRoutes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
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
            // ── 로그인 ─────────────────────────────────────────────────
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

            // ── 홈 ────────────────────────────────────────────────────
            composable(FinQRoutes.HOME) { entry ->
                val homeVm: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(repository, statsRepository),
                )
                val state by homeVm.uiState.collectAsState()
                // RESUMED 때마다 재로드 — 퀴즈 완료 후 돌아왔을 때도 최신 스트릭을 반영한다.
                LaunchedEffect(entry.lifecycle) {
                    entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        homeVm.loadQuizInfo()
                    }
                }
                HomeScreen(
                    quizCount = state.quizCount,
                    streak = state.streak,
                    maxStreak = state.maxStreak,
                    activityGrid = state.activityGrid,
                    isLoading = state.isLoading,
                    error = state.error,
                    nickname = state.nickname,
                    onStartQuiz = {
                        // SESSION_GRAPH 가 이미 백스택에 있으면 복귀(중간 이탈 케이스),
                        // 없으면 신규 생성(첫 진입 또는 컴플리티 후 재풌).
                        navController.resumeOrStartSession()
                    },
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

            // ── 보관함 탭 ─────────────────────────────────────────────
            composable(FinQRoutes.LIBRARY_TAB) {
                val libraryVm = libraryViewModel(libraryRepository)
                LibraryTabScreen(
                    wrongNoteViewModel = libraryVm,
                    bookmarkViewModel  = libraryVm,
                    historyViewModel   = libraryVm,
                    snackbarHostState  = snackbarHostState,
                )
            }

            // ── 마이페이지 ────────────────────────────────────────────
            composable(FinQRoutes.MY_PAGE) { entry ->
                val myPageVm: MyPageViewModel = viewModel(
                    factory = MyPageViewModel.factory(statsRepository, notificationRepository),
                )
                val state by myPageVm.uiState.collectAsState()

                // RESUMED 때마다 재로드 — restoreState 로 ViewModel 이 재사용될 때도 최신 스트릭을 반영한다.
                LaunchedEffect(entry.lifecycle) {
                    entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        myPageVm.loadStats()
                    }
                }

                LaunchedEffect(Unit) {
                    myPageVm.withdrawEvents.collect {
                        SessionManager.clearSession(context)
                        navController.navigate(FinQRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    myPageVm.logoutEvents.collect {
                        authRepository.logout(context)
                        navController.navigate(FinQRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                MyPageScreen(
                    nickname = state.nickname,
                    streak = state.streak,
                    maxStreak = state.maxStreak,
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
                    notificationsEnabled = state.notificationsEnabled,
                    notificationTime = state.notificationTime,
                    isSavingNotification = state.isSavingNotification,
                    notificationError = state.notificationError,
                    onToggleNotifications = myPageVm::setNotificationsEnabled,
                    onChangeNotificationTime = myPageVm::setNotificationTime,
                    onClearNotificationError = myPageVm::clearNotificationError,
                )
            }

            // ── 전체 풀이 이력 ────────────────────────────────────────
            composable(FinQRoutes.ATTEMPT_HISTORY) {
                val libraryVm = libraryViewModel(libraryRepository)
                AttemptHistoryRoute(
                    viewModel = libraryVm,
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState,
                )
            }

            // ── 퀴즈 세션 그래프 ──────────────────────────────────────
            navigation(
                startDestination = FinQRoutes.QUIZ,
                route = FinQRoutes.SESSION_GRAPH,
            ) {
                composable(FinQRoutes.QUIZ) { entry ->
                    val vm = entry.sessionViewModel(navController, repository)
                    // 뒤로가기: 세션을 종료하고 홈으로 돌아간다.
                    // 재진입 시 서버의 solved 상태 기준으로 미풀이 문제부터 시작한다.
                    BackHandler { navController.pauseSessionToHome() }
                    QuizRoute(
                        viewModel = vm,
                        onAfterSubmit = { navController.navigate(FinQRoutes.ANSWER) },
                        onClose = { navController.pauseSessionToHome() },
                    )
                }
                composable(FinQRoutes.ANSWER) { entry ->
                    val vm = entry.sessionViewModel(navController, repository)
                    val localContext = LocalContext.current
                    // 정답 화면에서도 동일하게 세션 유지하며 홈으로.
                    BackHandler { navController.pauseSessionToHome() }
                    AnswerRoute(
                        viewModel = vm,
                        libraryRepository = libraryRepository,
                        onBack = { navController.pauseSessionToHome() },
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
                            // 세션 컴플리티 — SESSION_GRAPH 를 inclusive=true 로 완전히 제거하고 HOME 으로.
                            // 이후 홈에서 "풀기" 시 resumeOrStartSession() 이 SESSION_GRAPH 를 감지하지 못해
                            // 신규 navigate 하며 새 세션이 시작된다.
                            navController.navigate(FinQRoutes.HOME) {
                                popUpTo(FinQRoutes.SESSION_GRAPH) { inclusive = true }
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
                        onGoHome = {
                            navController.navigate(FinQRoutes.HOME) {
                                popUpTo(FinQRoutes.SESSION_GRAPH) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
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
        // windowInsets 를 기본값으로 두어 기기 하단 시스템 바(제스처/3버튼)와 겹치지 않게 한다.
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(item.iconRes),
                        contentDescription = item.label,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (selected) Lime
                            else TextMuted
                        ),
                        modifier = Modifier.size(22.dp),
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
                    selectedTextColor = Lime,
                    unselectedTextColor = TextMuted,
                    indicatorColor = BgSubtle,
                ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 세션 ViewModel 공유 헬퍼
// ─────────────────────────────────────────────────────────────────────────────

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
    onClose: () -> Unit,
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
            quizIndex = state.progressIndex,
            totalCount = state.totalCount,
            quiz = state.currentQuiz!!,
            selectedOptionId = state.selectedOptionId,
            isSubmitting = state.isSubmitting,
            onSelectOption = viewModel::selectOption,
            onSubmit = { viewModel.submitAnswer() },
            onClose = onClose,
        )
    }
}

@Composable
private fun AnswerRoute(
    viewModel: QuizSessionViewModel,
    libraryRepository: LibraryRepository,
    onNext: () -> Unit,
    onBack: () -> Unit,
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
            quizIndex = state.progressIndex,
            totalCount = state.totalCount,
            onNext = onNext,
            onBack = onBack,
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
        quizzes = state.allQuizzes.ifEmpty { state.quizzes },
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
    onGoHome: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val libraryVm = libraryViewModel(libraryRepository)
    WrongNoteScreen(
        quizzes = state.allQuizzes.ifEmpty { state.quizzes },
        answerHistory = state.answerHistory,
        onBack = onBack,
        onGoHome = onGoHome,
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
