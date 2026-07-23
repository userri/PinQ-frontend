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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import com.finq.app.data.repository.ApiReviewRepository
import com.finq.app.data.repository.QuizRepository
import com.finq.app.data.repository.ReviewRepository
import com.finq.app.data.repository.UserStatsRepository
import com.finq.app.ui.home.HomeViewModel
import com.finq.app.ui.library.AttemptHistoryRoute
import com.finq.app.ui.library.LibraryTabScreen
import com.finq.app.ui.library.LibraryViewModel
import com.finq.app.ui.mypage.MyPageViewModel
import com.finq.app.ui.quiz.QuizSessionViewModel
import com.finq.app.ui.review.ReviewSessionViewModel
import com.finq.app.ui.review.toAnswerResult
import com.finq.app.ui.review.toQuiz
import com.finq.app.ui.garden.GardenViewModel
import com.finq.app.ui.screen.GardenScreen
import com.finq.app.ui.screen.HomeScreen
import com.finq.app.ui.screen.MyPageScreen
import com.finq.app.ui.screen.QuizAnswerScreen
import com.finq.app.ui.screen.QuizScreen
import com.finq.app.ui.screen.ResultReportScreen
import com.finq.app.ui.screen.ReviewDoneScreen
import com.finq.app.ui.screen.SolvedQuizReviewScreen
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
 *     └── session/done
 *
 * [세션 중간 이탈 처리]
 *   quiz/answer 화면에서 뒤로가기 시 세션 그래프를 제거하고 홈으로 돌아간다.
 *   홈에서 다시 "풀기"를 누르면 서버의 solved 상태를 새로 받아 진행 상태를 복원한다 —
 *   아직 안 푼 문제부터 이어서 보여주고, 이미 푼 문제는 채점 UI 대신 결과 보기 모드
 *   (SolvedQuizReviewScreen) 로 렌더한다. 진행도는 오늘 전체 문제 기준으로 표시해,
 *   남은 문제만 풀어도 Q3/4 같은 맥락이 유지된다.
 *
 * [재도전 경로 일원화]
 *   이미 채점된 문제를 다시 채점하는 진입점은 없다("다시 풀기" 버튼 제거됨).
 *   공식 재도전은 오답노트 → 복습(REVIEW_GRAPH, api/reviews 하위 endpoint) 경로뿐이며,
 *   복습 결과는 오늘의 정답률/스트릭에 영향을 주지 않는다.
 */
/** 복습 "다음 물 주기" 날짜 표기. */
private val reviewDueDateFormat: java.time.format.DateTimeFormatter =
    java.time.format.DateTimeFormatter.ofPattern("M월 d일")

object FinQRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val WRONG_NOTE_TAB = "wrongnote_tab"
    const val BOOKMARK_TAB = "bookmark_tab"
    const val LIBRARY_TAB = "library_tab"

    /** 보관함 탭 optional 인자 — 정원 나무 탭 딥링크용. */
    const val LIBRARY_TAB_PATTERN = "library_tab?focusQuizId={focusQuizId}"
    const val MY_PAGE = "mypage"
    const val ATTEMPT_HISTORY = "attempt_history"

    /** 복습 나무 정원 (마이페이지 잔디 카드에서 진입). */
    const val GARDEN = "garden"
    const val SESSION_GRAPH = "session"
    const val QUIZ = "session/quiz"
    const val ANSWER = "session/answer"
    const val DONE = "session/done"

    // ── 오답 복습 ("잔디에 물 주기") ──────────────────────────────
    const val REVIEW_GRAPH = "review"
    const val REVIEW_QUIZ = "review/quiz"
    const val REVIEW_ANSWER = "review/answer"
    const val REVIEW_DONE = "review/done"
}

private val bottomNavRoutes = setOf(
    FinQRoutes.HOME,
    // currentRoute 는 등록된 라우트 패턴 문자열로 도착하므로 패턴을 담는다.
    FinQRoutes.LIBRARY_TAB_PATTERN,
    FinQRoutes.MY_PAGE,
)

/**
 * 세션 진행 중에는 Scaffold 컨테이너까지 풀블리드 네이비로 깐다.
 * 상태바·하단 인셋 영역도 모두 네이비로 채워져 끊김 없는 다크 톤이 유지된다.
 */
private val darkSessionRoutes = setOf(
    FinQRoutes.QUIZ,
    FinQRoutes.ANSWER,
    FinQRoutes.REVIEW_QUIZ,
    FinQRoutes.REVIEW_ANSWER,
    FinQRoutes.REVIEW_DONE,
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
 * 세션을 종료하고 오답노트(내 공부 탭, 기본 첫 페이지)로 이동한다.
 * "자세한 해설은 오답노트에서" 안내나 결과 화면의 "오답노트 보기" 가 이 경로로 간다 —
 * 서버 기반이라 항상 최신 상세(선택지·정답·해설)를 보여주며, 재도전(복습)도 여기서 이어진다.
 */
private fun NavHostController.pauseSessionToLibrary() {
    navigate(FinQRoutes.LIBRARY_TAB) {
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
    val reviewRepository: ReviewRepository = remember { ApiReviewRepository(NetworkModule.reviewApi) }
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
                    factory = HomeViewModel.factory(repository, statsRepository, reviewRepository),
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
                    solvedToday = state.solvedToday,
                    maxStreak = state.maxStreak,
                    weekLevels = state.weekLevels,
                    isLoading = state.isLoading,
                    error = state.error,
                    nickname = state.nickname,
                    reviewCount = state.reviewCount,
                    nextReviewDate = state.nextReviewDate,
                    garden = state.garden,
                    todayTotal = state.todayTotal,
                    todayCorrect = state.todayCorrect,
                    onOpenGarden = { navController.navigate(FinQRoutes.GARDEN) },
                    onWaterGrass = { navController.navigate(FinQRoutes.REVIEW_GRAPH) },
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
            composable(
                route = FinQRoutes.LIBRARY_TAB_PATTERN,
                arguments = listOf(navArgument("focusQuizId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }),
            ) { entry ->
                val focusQuizId = entry.arguments?.getLong("focusQuizId")
                    ?.takeIf { it > 0 }
                val libraryVm = libraryViewModel(libraryRepository)
                LibraryTabScreen(
                    wrongNoteViewModel = libraryVm,
                    bookmarkViewModel  = libraryVm,
                    historyViewModel   = libraryVm,
                    snackbarHostState  = snackbarHostState,
                    // 미풀이 북마크(오늘 세트) → 오늘 풀이 세션으로 진입.
                    onStartQuiz = { navController.resumeOrStartSession() },
                    focusQuizId = focusQuizId,
                )
            }

            // ── 마이페이지 ────────────────────────────────────────────
            composable(FinQRoutes.MY_PAGE) { entry ->
                val myPageVm: MyPageViewModel = viewModel(
                    factory = MyPageViewModel.factory(statsRepository, notificationRepository, reviewRepository),
                )
                val state by myPageVm.uiState.collectAsState()

                // RESUMED 때마다 재로드 — restoreState 로 ViewModel 이 재사용될 때도 최신 스트릭을 반영한다.
                LaunchedEffect(entry.lifecycle) {
                    entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        // SWR: 데이터가 있으면 이전 값 표시 + 백그라운드 갱신 (스피너·깜빡임 없음)
                        myPageVm.refresh()
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
                    grass = state.grass,
                    grassFailed = state.grassFailed,
                    onRetryGrass = myPageVm::loadGrass,
                    onOpenGarden = { navController.navigate(FinQRoutes.GARDEN) },
                    garden = state.garden,
                    conceptStats = state.conceptStats,
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

            // ── 정원 (복습 나무 현황) ─────────────────────────────────
            composable(FinQRoutes.GARDEN) {
                val gardenVm: GardenViewModel = viewModel(
                    factory = GardenViewModel.factory(reviewRepository),
                )
                val state by gardenVm.uiState.collectAsState()
                GardenScreen(
                    garden = state.garden,
                    isLoading = state.isLoading,
                    error = state.error,
                    onRetry = gardenVm::load,
                    onBack = { navController.popBackStack() },
                    onOpenQuiz = { quizId ->
                        // 하단 탭 진입 규약과 동일하게 HOME 위 스택을 정리해 최상위 탭으로 도착시킨다.
                        // 이렇게 해야 이후 마이/홈 탭 전환의 saveState/restoreState 부기가 깨지지 않는다
                        // (기존엔 library_tab 이 GARDEN·MYPAGE 위에 쌓여 마이 탭 이동이 no-op 이 됐다).
                        // restoreState 는 생략 — 새 focusQuizId 로 새로 진입해야 스크롤·펼침이 동작한다.
                        // 정원은 저장 스택에 남기지 않는다 — 홈이 정원 진입점이 된 뒤로,
                        // 마이 탭 복원 시 정원이 되살아나는 어색함을 막기 위해 먼저 걷어낸다.
                        navController.popBackStack()
                        navController.navigate("library_tab?focusQuizId=$quizId") {
                            popUpTo(FinQRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            // ── 퀴즈 세션 그래프 ──────────────────────────────────────
            navigation(
                startDestination = FinQRoutes.QUIZ,
                route = FinQRoutes.SESSION_GRAPH,
            ) {
                composable(FinQRoutes.QUIZ) { entry ->
                    val vm = entry.sessionViewModel(navController, repository, libraryRepository)
                    // 뒤로가기: 세션을 종료하고 홈으로 돌아간다.
                    // 재진입 시 서버의 solved 상태 기준으로 진행 상태를 복원한다.
                    BackHandler { navController.pauseSessionToHome() }
                    QuizRoute(
                        viewModel = vm,
                        snackbarHostState = snackbarHostState,
                        onAfterSubmit = { navController.navigate(FinQRoutes.ANSWER) },
                        onClose = { navController.pauseSessionToHome() },
                        onFinishSession = {
                            // 결과 보기 모드로 마지막 문제까지 훑은 경우(재진입 안전망) —
                            // 라이브 제출 없이 곧장 결과 화면으로.
                            navController.navigate(FinQRoutes.DONE) {
                                popUpTo(FinQRoutes.QUIZ) { inclusive = true }
                            }
                        },
                        onViewWrongNote = { navController.pauseSessionToLibrary() },
                    )
                }
                composable(FinQRoutes.ANSWER) { entry ->
                    val vm = entry.sessionViewModel(navController, repository, libraryRepository)
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
                    val vm = entry.sessionViewModel(navController, repository, libraryRepository)
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
                        // 공식 재도전은 오답노트(→ 복습) 경로뿐 — "다시 풀기"로 이미 채점된
                        // 문제를 또 제출하는 진입점은 두지 않는다.
                        onWrongNote = { navController.pauseSessionToLibrary() },
                    )
                }
            }

            // ── 오답 복습 그래프 ("잔디에 물 주기") ────────────────────
            // 복습 결과는 스트릭·정답률에 반영되지 않으므로 퀴즈 세션과 완전히 분리한다.
            navigation(
                startDestination = FinQRoutes.REVIEW_QUIZ,
                route = FinQRoutes.REVIEW_GRAPH,
            ) {
                composable(FinQRoutes.REVIEW_QUIZ) { entry ->
                    val vm = entry.reviewViewModel(navController, reviewRepository)
                    val state by vm.uiState.collectAsState()

                    BackHandler { navController.exitReviewToHome() }

                    LaunchedEffect(state.notice) {
                        state.notice?.let {
                            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                            vm.clearNotice()
                        }
                    }

                    // 복습할 게 없거나 모두 끝나면 완료 화면으로.
                    LaunchedEffect(state.isFinished) {
                        if (state.isFinished) {
                            navController.navigate(FinQRoutes.REVIEW_DONE) {
                                popUpTo(FinQRoutes.REVIEW_QUIZ) { inclusive = true }
                            }
                        }
                    }
                    // 채점이 끝나면 정답 화면으로.
                    LaunchedEffect(state.lastAnswer, state.isSubmitting) {
                        if (!state.isSubmitting && state.lastAnswer != null) {
                            navController.navigate(FinQRoutes.REVIEW_ANSWER)
                        }
                    }

                    val item = state.currentItem
                    when {
                        state.isLoading || (item == null && !state.isFinished) -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Lime)
                            }
                        }
                        state.error != null && item == null -> {
                            ReviewErrorBox(message = state.error!!, onRetry = vm::loadReviews)
                        }
                        item != null -> {
                            QuizScreen(
                                quizIndex = state.currentIndex,
                                totalCount = state.totalCount,
                                quiz = item.toQuiz(),
                                selectedOptionId = state.selectedOptionId,
                                onSelectOption = vm::selectOption,
                                onSubmit = vm::submitAnswer,
                                onClose = { navController.exitReviewToHome() },
                                isSubmitting = state.isSubmitting,
                                categoryLabel = "${item.stage.label} · ${item.categoryLabel}",
                                categoryIconRes = item.stage.iconRes,
                                headerNote = buildString {
                                    if (item.waterCount > 0) append("💧 물 ${item.waterCount}번 · 흡수 ${item.absorbedCount}번 · ")
                                    if (item.stage.isFinalStage) append("한 번 더 맞히면 나무가 돼요 · ")
                                    append("복습은 기록에 영향 없어요")
                                },
                            )
                        }
                    }
                }

                composable(FinQRoutes.REVIEW_ANSWER) { entry ->
                    val vm = entry.reviewViewModel(navController, reviewRepository)
                    val state by vm.uiState.collectAsState()
                    val localContext = LocalContext.current
                    val item = state.currentItem
                    val answer = state.lastAnswer

                    // moveToNext() 가 lastAnswer 를 비우면 이 화면을 빠져나간다.
                    LaunchedEffect(answer) {
                        if (answer == null) navController.popBackStack()
                    }

                    if (item != null && answer != null) {
                        BackHandler { /* 채점 후 뒤로가기 차단 — "다음"으로만 진행 */ }
                        QuizAnswerScreen(
                            quiz = item.toQuiz(),
                            answer = answer.toAnswerResult(state.selectedOptionId ?: 0L),
                            isLast = state.isLastItem,
                            quizIndex = state.currentIndex,
                            totalCount = state.totalCount,
                            onNext = vm::moveToNext,
                            onBack = { navController.exitReviewToHome() },
                            onArticleClick = { article ->
                                val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                                try {
                                    localContext.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(localContext, "기사를 열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show()
                                }
                            },
                            categoryLabel = "${item.stage.label} · ${item.categoryLabel}",
                            categoryIconRes = item.stage.iconRes,
                            graduated = answer.graduated,
                            graduatedMessage = if (answer.graduated && answer.totalGraduatedTrees != null)
                                "물 ${answer.waterCount}번 준 나무가 완성됐어요 — 당신의 ${answer.totalGraduatedTrees}번째 나무"
                            else null,
                            nextReviewText = answer.nextDueDate?.let {
                                "다음 물 주기: ${it.format(reviewDueDateFormat)} · 💧 물 ${answer.waterCount}번 · 흡수 ${answer.absorbedCount}번"
                            },
                            nextLabel = if (state.isLastItem) "복습 완료" else "다음 복습",
                        )
                    }
                }

                composable(FinQRoutes.REVIEW_DONE) { entry ->
                    val vm = entry.reviewViewModel(navController, reviewRepository)
                    val state by vm.uiState.collectAsState()

                    BackHandler { navController.exitReviewToHome() }
                    ReviewDoneScreen(
                        reviewedCount = state.totalCount,
                        correctCount = state.correctCount,
                        graduatedCount = state.graduatedCount,
                        nextDueDate = state.nextDueDate,
                        onGoHome = { navController.exitReviewToHome() },
                    )
                }
            }
        }
    }
}

/** 복습 그래프를 통째로 걷어내고 홈으로. 홈은 RESUMED 마다 재로드하므로 카운트가 갱신된다. */
private fun NavHostController.exitReviewToHome() {
    navigate(FinQRoutes.HOME) {
        popUpTo(FinQRoutes.REVIEW_GRAPH) { inclusive = true }
        launchSingleTop = true
    }
}

/** 복습 그래프 전체가 공유하는 ViewModel. */
@Composable
private fun NavBackStackEntry.reviewViewModel(
    navController: NavHostController,
    reviewRepository: ReviewRepository,
): ReviewSessionViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(FinQRoutes.REVIEW_GRAPH)
    }
    val factory = remember(reviewRepository) { ReviewSessionViewModel.factory(reviewRepository) }
    return viewModel(viewModelStoreOwner = parentEntry, factory = factory)
}

@Composable
private fun ReviewErrorBox(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("다시 시도") }
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
            // 보관함은 optional 인자 라우트(library_tab?focusQuizId=…)로 도착할 수 있어
            // 쿼리 부분을 떼고 base 라우트로 비교한다 — 딥링크 진입 시에도 탭 하이라이트 유지.
            val selected = currentRoute?.substringBefore("?") == item.route
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
    libraryRepository: LibraryRepository,
): QuizSessionViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(FinQRoutes.SESSION_GRAPH)
    }
    val factory = remember(repository, libraryRepository) {
        QuizSessionViewModel.factory(repository, libraryRepository)
    }
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
    snackbarHostState: SnackbarHostState,
    onAfterSubmit: () -> Unit,
    onClose: () -> Unit,
    onFinishSession: () -> Unit,
    onViewWrongNote: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.lastAnswer, state.isSubmitting) {
        if (!state.isSubmitting && state.lastAnswer != null) {
            onAfterSubmit()
        }
    }

    // 북마크 토글 실패 → 짧은 스낵바 (낙관적 업데이트는 VM 에서 이미 롤백됨).
    LaunchedEffect(Unit) {
        viewModel.bookmarkErrors.collect { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    when {
        state.isLoading -> LoadingBox()
        state.error != null -> ErrorBox(state.error!!) { viewModel.loadQuizzes() }
        state.currentQuiz == null -> ErrorBox("표시할 퀴즈가 없습니다.") { viewModel.loadQuizzes() }
        // 이미 채점된 문제 — 재진입 시 서버의 solved/correct 기준으로 결과 보기 모드만
        // 렌더한다. 여기엔 제출 경로가 없으므로 재채점될 일이 없다.
        state.currentQuiz!!.solved -> SolvedQuizReviewScreen(
            quizIndex = state.progressIndex,
            totalCount = state.totalCount,
            quiz = state.currentQuiz!!,
            isLast = state.isLastQuiz,
            onNext = { if (state.isLastQuiz) onFinishSession() else viewModel.moveToNext() },
            onClose = onClose,
            onViewWrongNote = onViewWrongNote,
        )
        else -> QuizScreen(
            quizIndex = state.progressIndex,
            totalCount = state.totalCount,
            quiz = state.currentQuiz!!,
            selectedOptionId = state.selectedOptionId,
            isSubmitting = state.isSubmitting,
            onSelectOption = viewModel::selectOption,
            onSubmit = { viewModel.submitAnswer() },
            onClose = onClose,
            bookmarked = state.isCurrentBookmarked,
            onToggleBookmark = viewModel::toggleBookmark,
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

    // 첫 오답 인트로 — 첫 오답이 '복습 나무'가 되는 순간 딱 한 번 개념을 소개한다.
    val introContext = LocalContext.current
    var showIntro by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(answer) {
        if (answer != null && !answer.isCorrect &&
            !com.finq.app.ui.components.hasSeenReviewTreeIntro(introContext)
        ) {
            com.finq.app.ui.components.markReviewTreeIntroSeen(introContext)
            showIntro = true
        }
    }
    if (showIntro) {
        com.finq.app.ui.components.ReviewTreeConceptDialog(
            title = "🌱 첫 복습 나무가 태어났어요",
            confirmLabel = "키워볼게요",
            onDismiss = { showIntro = false },
        )
    }

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
            // 풀이 화면과 같은 세션 상태 — 풀이 중 켠 북마크가 여기서도 켜져 있다.
            bookmarked = state.isCurrentBookmarked,
            onToggleBookmark = viewModel::toggleBookmark,
        )
    }
}

@Composable
private fun DoneRoute(
    viewModel: QuizSessionViewModel,
    onGoHome: () -> Unit,
    onWrongNote: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    ResultReportScreen(
        quizzes = state.quizzes,
        answerHistory = state.answerHistory,
        onGoHome = onGoHome,
        onWrongNote = onWrongNote,
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
