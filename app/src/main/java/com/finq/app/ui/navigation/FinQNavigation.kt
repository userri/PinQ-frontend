package com.finq.app.ui.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
import com.finq.app.ui.library.AttemptDetailRoute
import com.finq.app.ui.library.AttemptHistoryRoute
import com.finq.app.ui.library.LibraryTabScreen
import com.finq.app.ui.library.LibraryViewModel
import com.finq.app.ui.mypage.MyPageViewModel
import com.finq.app.ui.quiz.QuizSessionViewModel
import com.finq.app.ui.quiz.SoloQuizViewModel
import com.finq.app.ui.quiz.toSoloQuiz
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
import com.finq.app.ui.screen.TasteQuizScreen
import com.finq.app.data.local.SessionManager
import com.finq.app.data.repository.AuthRepository
import com.finq.app.ui.login.LoginEvent
import com.finq.app.ui.login.LoginViewModel
import com.finq.app.data.local.hasSeenTasteQuiz
import com.finq.app.data.local.isFeedbackHintShown
import com.finq.app.data.local.markFeedbackBannerDismissed
import com.finq.app.data.local.markFeedbackHintShown
import com.finq.app.data.local.markTasteQuizSeen
import com.finq.app.data.local.shouldShowFeedbackBanner
import com.finq.app.ui.components.nextWateringText
import com.finq.app.ui.components.openFeedbackForm
import com.finq.app.ui.onboarding.OnboardingScreen
import com.finq.app.ui.onboarding.hasSeenOnboarding
import com.finq.app.ui.onboarding.markOnboardingSeen
import com.finq.app.ui.screen.LoginScreen
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * FinQ 네비게이션 그래프.
 *
 * 구조 (Phase 4):
 *   login                   — 미인증 시 시작점 (하단 네비게이션 없음)
 *   onboarding              — 첫 실행 1회 + 마이페이지 재열람. 홈 "위에" 얹히며,
 *                             첫 실행 완료 시 홈을 거치지 않고 곧장 첫 문제로 간다.
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
object FinQRoutes {
    const val LOGIN = "login"

    /**
     * 첫 실행 온보딩 — 로그인 직후 1회. 판정 플래그는 로컬(SharedPreferences)에만 있다.
     * replay=true 는 마이페이지에서 다시 열어본 경우 — 끝나면 첫 문제가 아니라 되돌아간다.
     */
    const val ONBOARDING_PATTERN = "onboarding?replay={replay}"
    fun onboarding(replay: Boolean = false) = "onboarding?replay=$replay"

    const val HOME = "home"
    const val WRONG_NOTE_TAB = "wrongnote_tab"
    const val BOOKMARK_TAB = "bookmark_tab"
    const val LIBRARY_TAB = "library_tab"
    const val MY_PAGE = "mypage"
    const val ATTEMPT_HISTORY = "attempt_history"

    /**
     * 보관함 항목 상세 — 목록 행 탭과 정원 나무 탭이 모두 여기로 온다.
     * 요약만 있는 목록과 달리 상세는 quizId 로 단건 조회한다.
     */
    const val ATTEMPT_DETAIL_PATTERN = "attempt_detail/{quizId}"
    fun attemptDetail(quizId: Long) = "attempt_detail/$quizId"

    /** 복습 나무 정원 (마이페이지 잔디 카드에서 진입). */
    const val GARDEN = "garden"
    /**
     * 단건 풀이 — 미풀이 북마크 "풀러 가기" 진입 경로.
     * 오늘 세트가 아닌 지난 문제도 quizId 만으로 풀고 채점까지 마칠 수 있다.
     */
    const val SOLO_QUIZ_PATTERN = "solo_quiz/{quizId}"
    fun soloQuiz(quizId: Long) = "solo_quiz/$quizId"

    const val SESSION_GRAPH = "session"
    const val QUIZ = "session/quiz"
    const val ANSWER = "session/answer"
    const val DONE = "session/done"

    // ── 오답 복습 ("잔디에 물 주기") ──────────────────────────────
    /**
     * 복습 세션 그래프.
     *
     * `start` — 이 문제부터 시작한다(정원에서 빛나는 식물을 탭한 경우). 큐에 없으면
     *           큐 처음부터, 큐가 비어 있으면 세션을 열지 않고 상세 열람으로 보낸다.
     * `from`  — 나갈 때 돌아갈 곳. 정원에서 들어왔는데 홈으로 튀어나오면 어긋난다.
     */
    const val REVIEW_GRAPH = "review"

    /**
     * 인자는 그래프가 아니라 **시작 목적지**에 붙인다 — `navigation(arguments=)` 는
     * 이 navigation-compose 버전에 없다. 세션 안의 다른 화면들은 이 엔트리에서 읽는다.
     */
    fun reviewQuiz(startQuizId: Long? = null, from: String? = null) =
        "review/quiz?start=${startQuizId ?: -1L}&from=${from ?: RETURN_HOME}"

    /** [reviewGraph] 의 `from` 값. */
    const val RETURN_HOME = "home"
    const val RETURN_GARDEN = "garden"
    const val REVIEW_QUIZ = "review/quiz?start={start}&from={from}"
    const val REVIEW_ANSWER = "review/answer"
    const val REVIEW_DONE = "review/done"
}

/** 종료 확인 창 — 이 안에 한 번 더 누르면 닫는다. 토스트(LENGTH_SHORT ≈ 2초)와 같은 길이. */
private const val EXIT_CONFIRM_WINDOW_MS = 2000L

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
    // 온보딩도 화면 전체가 밤하늘 한 장면이다 — 상태바·하단 인셋까지 네이비로 이어져야 한다.
    FinQRoutes.ONBOARDING_PATTERN,
    FinQRoutes.QUIZ,
    FinQRoutes.ANSWER,
    FinQRoutes.SOLO_QUIZ_PATTERN,
    FinQRoutes.REVIEW_QUIZ,
    FinQRoutes.REVIEW_ANSWER,
    FinQRoutes.REVIEW_DONE,
    // 상세는 채점 화면과 같은 본문을 쓰므로 같은 풀블리드 네이비 톤으로 깐다.
    FinQRoutes.ATTEMPT_DETAIL_PATTERN,
)

data class BottomNavItem(
    val route: String,
    val label: String,
    /** 비활성 — 아웃라인. */
    val iconRes: Int,
    /** 활성 — 같은 실루엣의 솔리드. fill/line 혼용 금지. */
    val filledIconRes: Int,
)

private val bottomNavItems = listOf(
    BottomNavItem(
        FinQRoutes.HOME, "홈",
        com.finq.app.R.drawable.ic_tab_home, com.finq.app.R.drawable.ic_tab_home_filled,
    ),
    BottomNavItem(
        FinQRoutes.LIBRARY_TAB, "내 공부",
        com.finq.app.R.drawable.ic_tab_book, com.finq.app.R.drawable.ic_tab_book_filled,
    ),
    BottomNavItem(
        FinQRoutes.MY_PAGE, "마이",
        com.finq.app.R.drawable.ic_tab_user, com.finq.app.R.drawable.ic_tab_user_filled,
    ),
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
    // 보관함(목록 3탭 + 상세 + 전체이력)이 한 ViewModel 을 공유한다 — 상세에서 켠 북마크가
    // 목록에 바로 반영되고, 상세가 목록 요약으로 헤더를 먼저 그릴 수 있다.
    val libraryVm = libraryViewModel(libraryRepository)

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

    // ── 앱 종료는 뒤로가기 두 번 ─────────────────────────────────────────
    // 홈(로그인 전이면 로그인)이 스택의 바닥이라 여기서 한 번 누르면 앱이 닫힌다.
    // 다른 탭에서 홈으로 돌아온 직후 습관적으로 한 번 더 누르는 일이 잦아, 그 한 번에
    // 앱이 사라지는 걸 막는다. 안내는 스낵바가 아니라 토스트다 — 스낵바는 하단 탭 위에
    // 얹혀 탭을 가리고, 화면을 떠나는 동작의 안내는 화면 밖에 떠 있는 편이 맞다.
    var lastBackPressedAt by remember { mutableLongStateOf(0L) }
    val atExitPoint = currentRoute == FinQRoutes.HOME || currentRoute == FinQRoutes.LOGIN
    BackHandler(enabled = atExitPoint) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastBackPressedAt < EXIT_CONFIRM_WINDOW_MS) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressedAt = now
            Toast.makeText(context, "한 번 더 누르면 앱이 닫혀요", Toast.LENGTH_SHORT).show()
        }
    }

    // 첫 실행 온보딩 — 홈을 시작 목적지로 두고 그 "위에" 얹는다.
    // 온보딩을 NavHost 의 시작 목적지로 삼아 나갈 때 popUpTo(inclusive) 로 걷어내면
    // 그래프의 시작 목적지가 사라져 백스택이 깨진다(세션 그래프가 부모 없이 컴포즈돼
    // 크래시). 홈 위에 얹으면 나갈 때 popBackStack 한 번이면 되고, 마이페이지
    // 재열람 경로와도 구조가 같아진다.
    var onboardingPending by rememberSaveable { mutableStateOf(!hasSeenOnboarding(context)) }
    LaunchedEffect(currentRoute, onboardingPending) {
        if (onboardingPending && currentRoute == FinQRoutes.HOME) {
            onboardingPending = false
            navController.navigate(FinQRoutes.onboarding())
        }
    }

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
                            // 온보딩은 홈에 도착한 뒤 위에 얹힌다(아래 onboardingPending).
                            navController.navigate(FinQRoutes.HOME) {
                                popUpTo(FinQRoutes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                }

                // 맛보기 문제를 아직 안 본 사람에겐 로그인 대신 그 화면을 먼저 보여준다.
                // 로그인 버튼은 맛보기 화면 안(결과 아래)에 있으므로 화면 전환이 없다 —
                // 계정을 내주기 전에 앱이 뭘 주는지 한 번 겪게 하는 게 목적이다.
                var tasteDone by rememberSaveable { mutableStateOf(hasSeenTasteQuiz(context)) }
                if (!tasteDone) {
                    TasteQuizScreen(
                        isLoading = state.isLoading,
                        onKakaoLogin = {
                            markTasteQuizSeen(context)
                            tasteDone = true
                            loginVm.loginWithKakao(context)
                        },
                        onGoogleLogin = {
                            markTasteQuizSeen(context)
                            tasteDone = true
                            loginVm.loginWithGoogle(context)
                        },
                    )
                } else {
                    LoginScreen(
                        isLoading = state.isLoading,
                        error = state.error,
                        onKakaoLogin = { loginVm.loginWithKakao(context) },
                        onGoogleLogin = { loginVm.loginWithGoogle(context) },
                        onClearError = loginVm::clearError,
                    )
                }
            }

            // ── 첫 실행 온보딩 ─────────────────────────────────────────
            composable(
                route = FinQRoutes.ONBOARDING_PATTERN,
                arguments = listOf(
                    navArgument("replay") { type = NavType.BoolType; defaultValue = false },
                ),
            ) { entry ->
                val replay = entry.arguments?.getBoolean("replay") ?: false
                // 온보딩을 벗어나면(완료·건너뛰기·재열람 종료) 다시는 자동으로 뜨지 않는다.
                // 아래는 늘 홈이므로 셋 다 popBackStack 한 번이면 된다.
                //
                // 완료도 홈이다. 예전엔 마지막 장 CTA 가 곧장 첫 문제(SESSION_GRAPH)로
                // 보냈는데, 가입 직후 사용자가 처음 보는 화면이 문제 풀이가 되어
                // **앱에 무엇이 있는지 못 본 채 시험부터 치렀다.** 홈에 내려놓으면
                // 오늘의 문제 카드가 같은 자리에서 같은 것을 부르고, 정원·내 공부도 함께 보인다.
                val leave: () -> Unit = {
                    markOnboardingSeen(context)
                    navController.popBackStack()
                }
                OnboardingScreen(
                    replay = replay,
                    onFinish = leave,
                    onSkip = leave,
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
                // 피드백 배너 — 로컬 플래그만 보므로 ViewModel 을 거치지 않는다.
                // 닫거나 폼으로 넘어가면 영구 해제되고, 그 순간 딱 한 번
                // "창구는 마이페이지에 있다"를 알려 준다.
                var showFeedback by remember { mutableStateOf(shouldShowFeedbackBanner(context)) }
                val feedbackScope = rememberCoroutineScope()
                val retireFeedbackBanner: () -> Unit = {
                    markFeedbackBannerDismissed(context)
                    showFeedback = false
                    if (!isFeedbackHintShown(context)) {
                        markFeedbackHintShown(context)
                        feedbackScope.launch {
                            snackbarHostState.showSnackbar("의견은 마이페이지에서 언제든 보낼 수 있어요")
                        }
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
                    reviewedToday = state.reviewedToday,
                    grownToday = state.grownToday,
                    nextReviewDate = state.nextReviewDate,
                    garden = state.garden,
                    todayTotal = state.todayTotal,
                    todayCorrect = state.todayCorrect,
                    onOpenGarden = { navController.navigate(FinQRoutes.GARDEN) },
                    onWaterGrass = { navController.navigate(FinQRoutes.reviewQuiz()) },
                    onStartQuiz = {
                        // SESSION_GRAPH 가 이미 백스택에 있으면 복귀(중간 이탈 케이스),
                        // 없으면 신규 생성(첫 진입 또는 컴플리티 후 재풌).
                        navController.resumeOrStartSession()
                    },
                    onRetry = homeVm::loadQuizInfo,
                    showFeedbackBanner = showFeedback,
                    onOpenFeedback = {
                        openFeedbackForm(context)
                        retireFeedbackBanner()
                    },
                    onDismissFeedback = retireFeedbackBanner,
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
                LibraryTabScreen(
                    wrongNoteViewModel = libraryVm,
                    bookmarkViewModel  = libraryVm,
                    historyViewModel   = libraryVm,
                    snackbarHostState  = snackbarHostState,
                    onOpenDetail = { item ->
                        navController.navigate(FinQRoutes.attemptDetail(item.quizId))
                    },
                    // 미풀이 북마크 → 그 문제의 단건 풀이 화면으로 진입.
                    // (예전엔 오늘 세트로만 보내 익일 북마크가 죽은 링크가 됐다.)
                    onStartQuiz = { item ->
                        navController.navigate(FinQRoutes.soloQuiz(item.quizId))
                    },
                )
            }

            // ── 보관함 항목 상세 ──────────────────────────────────────
            composable(
                route = FinQRoutes.ATTEMPT_DETAIL_PATTERN,
                arguments = listOf(navArgument("quizId") { type = NavType.LongType }),
            ) { entry ->
                val quizId = entry.arguments?.getLong("quizId") ?: return@composable
                AttemptDetailRoute(
                    quizId = quizId,
                    viewModel = libraryVm,
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState,
                    onArticleClick = { article ->
                        val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "기사를 열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show()
                        }
                    },
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
                    totalSolved = state.totalSolved,
                    correctRate = state.correctRate,
                    grass = state.grass,
                    grassFailed = state.grassFailed,
                    onRetryGrass = myPageVm::loadGrass,
                    onOpenGarden = { navController.navigate(FinQRoutes.GARDEN) },
                    garden = state.garden,
                    conceptStats = state.conceptStats,
                    appVersion = BuildConfig.VERSION_NAME,
                    // 온보딩 재열람 — 캐러셀을 강제로 다시 재생하지 않고 여기서만 부른다.
                    onOpenOnboarding = {
                        navController.navigate(FinQRoutes.onboarding(replay = true))
                    },
                    onOpenFeedback = { openFeedbackForm(context) },
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
                AttemptHistoryRoute(
                    viewModel = libraryVm,
                    onBack = { navController.popBackStack() },
                    onOpenDetail = { item ->
                        navController.navigate(FinQRoutes.attemptDetail(item.quizId))
                    },
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
                    onOpenQuiz = { item ->
                        // 목적지를 due 상태가 정한다. 빛나는 식물은 "오늘 물 줄 것"이라고
                        // 말하고 있으므로 탭의 기대는 물주기다 — 상세로 보내면 답만 보고
                        // 정작 물은 못 주는 데드엔드가 된다(실사용 보고).
                        //
                        // 분기는 반드시 inTodayQueue 로만 한다. dueDate 로 대체하면 캡에
                        // 잘린 백로그까지 세션으로 보내고, 무엇보다 졸업 항목이 섞이면
                        // 서버가 졸업분 채점을 404 로 막고 있어 에러가 난다.
                        if (item.inTodayQueue) {
                            navController.navigate(
                                FinQRoutes.reviewQuiz(
                                    startQuizId = item.quizId,
                                    from = FinQRoutes.RETURN_GARDEN,
                                ),
                            )
                        } else {
                            navController.navigate(FinQRoutes.attemptDetail(item.quizId))
                        }
                    },
                    onOpenAll = {
                        // "전체 N개 보기" — 특정 문제 포커스 없이 오답노트 탭으로(같은 스택 규약).
                        navController.popBackStack()
                        navController.navigate(FinQRoutes.LIBRARY_TAB) {
                            popUpTo(FinQRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    // 정원은 보상 공간, 복습은 작업 공간 — 홈 물주기 카드와 같은 진입.
                    // 단 나가는 곳은 들어온 곳이다. from 을 안 주면 기본값이 RETURN_HOME 이라
                    // 정원에서 눌러 놓고 홈으로 떨어졌다 — 위 개별 식물 탭과 같은 값을 준다.
                    onStartReview = {
                        navController.navigate(FinQRoutes.reviewQuiz(from = FinQRoutes.RETURN_GARDEN))
                    },
                )
            }

            // ── 단건 풀이 (미풀이 북마크 "풀러 가기") ──────────────────
            composable(
                route = FinQRoutes.SOLO_QUIZ_PATTERN,
                arguments = listOf(navArgument("quizId") { type = NavType.LongType }),
            ) { entry ->
                val quizId = entry.arguments?.getLong("quizId") ?: return@composable
                val vm: SoloQuizViewModel = viewModel(
                    factory = SoloQuizViewModel.factory(quizId, libraryRepository, repository),
                )
                SoloQuizRoute(
                    viewModel = vm,
                    libraryRepository = libraryRepository,
                    // 북마크 목록으로 복귀 — BookmarkTabRoute 는 재진입 시
                    // LaunchedEffect(Unit) 로 목록을 다시 불러 solved 뱃지가 갱신된다.
                    onExit = { navController.popBackStack() },
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
                composable(
                    route = FinQRoutes.REVIEW_QUIZ,
                    arguments = listOf(
                        navArgument("start") { type = NavType.LongType; defaultValue = -1L },
                        navArgument("from") {
                            type = NavType.StringType
                            defaultValue = FinQRoutes.RETURN_HOME
                        },
                    ),
                ) { entry ->
                    val vm = entry.reviewViewModel(navController, reviewRepository)
                    val state by vm.uiState.collectAsState()
                    val from = entry.reviewFrom(navController)
                    val startQuizId = entry.reviewStartQuizId(navController)

                    // 낡은 정원 데이터로 들어온 경우 — 자정을 넘겼거나 다른 기기에서 먼저
                    // 풀었으면 큐가 비어 있다. 빈 세션(완료 화면)을 띄우면 "빛나서 눌렀는데
                    // 아무것도 없음"이 되어 지금 데드엔드보다 나쁘다. 상세 열람으로 돌린다.
                    LaunchedEffect(state.isLoading, state.items.isEmpty(), startQuizId) {
                        if (!state.isLoading && state.error == null &&
                            state.items.isEmpty() && startQuizId != null
                        ) {
                            navController.navigate(FinQRoutes.attemptDetail(startQuizId)) {
                                popUpTo(FinQRoutes.REVIEW_GRAPH) { inclusive = true }
                            }
                        }
                    }

                    BackHandler { navController.exitReview(from) }

                    LaunchedEffect(state.notice) {
                        state.notice?.let {
                            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                            vm.clearNotice()
                        }
                    }

                    // 채점 실패 — 문제를 띄운 채 실패했으면 화면을 갈아엎지 않고 스낵바로
                    // 알린다. 고른 답이 남아 있어 바로 다시 제출할 수 있다. 아래 when 의
                    // ReviewErrorBox 는 목록 자체를 못 불러온 경우(item == null)만 맡는다.
                    LaunchedEffect(state.error) {
                        if (state.error != null && state.currentItem != null) {
                            snackbarHostState.showSnackbar(
                                state.error!!,
                                duration = SnackbarDuration.Short,
                            )
                            vm.clearError()
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
                                onClose = { navController.exitReview(from) },
                                isSubmitting = state.isSubmitting,
                                categoryLabel = "${item.stage.label} · ${item.categoryLabel}",
                                // "왜 이걸 하고 있나"에 답하는 한 줄. 답한 직후 성장 게이지가
                                // 나타나 이 문구를 그 자리에서 증명한다.
                                //
                                // 종전의 "복습은 기록에 영향 없어요"를 뺀 이유: 정답률이 안 떨어진다는 건
                                // 통계 화면을 따로 봐야 아는 사실이라, 여기서 미리 안심시키면 없던 불안만
                                // 만든다. 안심 문구는 개념 시트와 완료 화면에 이미 있고 거기선 "스트릭·정답률"로
                                // 범위까지 밝힌다("기록"이라고 뭉뚱그리지 않는다).
                                //
                                // buildString 이 아니라 분기인 이유: 이어붙이면 마지막 단계에서
                                // "한 번 더 맞히면 나무가 돼요 · 맞히면 나무가 자라요"로 같은 말이 두 번 나온다.
                                // 누적 통계(물 N번 · 흡수 N번)는 진척이 아니므로 여기 없다.
                                headerNote = if (item.stage.isFinalStage) "한 번 더 맞히면 나무가 돼요"
                                             else "맞히면 나무가 자라요",
                            )
                        }
                    }
                }

                composable(FinQRoutes.REVIEW_ANSWER) { entry ->
                    val vm = entry.reviewViewModel(navController, reviewRepository)
                    val state by vm.uiState.collectAsState()
                    val from = entry.reviewFrom(navController)
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
                            onBack = { navController.exitReview(from) },
                            onArticleClick = { article ->
                                val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                                try {
                                    localContext.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(localContext, "기사를 열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show()
                                }
                            },
                            categoryLabel = "${item.stage.label} · ${item.categoryLabel}",
                            graduated = answer.graduated,
                            // 제목("나무가 됐어요")은 배너가 고정으로 갖는다 — 여기선 부제만 준다.
                            graduatedMessage = if (answer.graduated && answer.totalGraduatedTrees != null)
                                "당신의 ${answer.totalGraduatedTrees}번째 나무"
                            else null,
                            // 누적 통계(물 N번 · 흡수 N번)는 뺀다. 진척은 stage 게이지가 전담하고,
                            // waterCount 는 시도 누계라 진척이 아니다 — 나란히 두면 어느 쪽이
                            // "얼마나 자랐나"인지 흐려진다. 여기 남길 건 다음 약속뿐.
                            nextReviewText = answer.nextDueDate?.let {
                                nextWateringText(it)
                            },
                            reviewStage = answer.stage,
                            nextLabel = if (state.isLastItem) "복습 완료" else "다음 복습",
                        )
                    }
                }

                composable(FinQRoutes.REVIEW_DONE) { entry ->
                    val vm = entry.reviewViewModel(navController, reviewRepository)
                    val state by vm.uiState.collectAsState()
                    val from = entry.reviewFrom(navController)

                    BackHandler { navController.exitReview(from) }

                    // 완료 시점의 사용자 단위 다음 물주기를 새로 받는다 — 세션 시작 때
                    // 받은 값은 "오늘 몫이 남아 있던" 시점의 답이다.
                    LaunchedEffect(Unit) { vm.refreshNextDueDate() }

                    ReviewDoneScreen(
                        // 세션이 아니라 오늘 단위 — 정원에서 먼저 푼 것까지 합쳐야
                        // "오늘 물 줄 잔디 5개"라는 목표와 단위가 맞는다.
                        reviewedCount = state.todayReviewed.takeIf { it > 0 } ?: state.totalCount,
                        correctCount = state.todayReviewed.takeIf { it > 0 }
                            ?.let { state.todayCorrect } ?: state.correctCount,
                        graduatedCount = state.graduatedCount,
                        nextDueDate = state.nextDueDate,
                        onGoHome = { navController.exitReview(from) },
                        // 버튼은 실제로 가는 곳을 말한다 — exitReview 가 정원으로 보내는데
                        // 라벨만 "홈으로"면 누르기 전과 후가 어긋난다.
                        exitLabel = if (from == FinQRoutes.RETURN_GARDEN) "정원으로" else "홈으로",
                    )
                }
            }
        }
    }
}

/**
 * 복습 그래프를 통째로 걷어내고 **들어온 곳으로** 돌아간다.
 *
 * 정원에서 빛나는 식물을 눌러 들어왔는데 홈으로 튀어나오면 맥락이 끊긴다.
 * 중간 이탈(BackHandler)과 완주(완료 화면의 "돌아가기")가 **같은 목적지**를 써야 한다 —
 * 한쪽만 고치면 완주했을 때만 홈으로 튄다.
 *
 * 정원으로 돌아갈 땐 새로 navigate 해서 GardenViewModel 이 다시 로드되게 한다.
 * 방금 물을 줬으므로 후광·개수가 갱신돼야 한다.
 */
private fun NavHostController.exitReview(from: String) {
    val dest = if (from == FinQRoutes.RETURN_GARDEN) FinQRoutes.GARDEN else FinQRoutes.HOME
    navigate(dest) {
        popUpTo(FinQRoutes.REVIEW_GRAPH) { inclusive = true }
        launchSingleTop = true
    }
}

/** 세션 인자는 시작 목적지(REVIEW_QUIZ) 엔트리에 있다 — 답변·완료 화면도 여기서 읽는다. */
@Composable
private fun NavBackStackEntry.reviewGraphArgs(navController: NavHostController) =
    remember(this) {
        runCatching { navController.getBackStackEntry(FinQRoutes.REVIEW_QUIZ).arguments }
            .getOrNull()
    }

/** 나갈 때 돌아갈 곳. */
@Composable
private fun NavBackStackEntry.reviewFrom(navController: NavHostController): String =
    reviewGraphArgs(navController)?.getString("from") ?: FinQRoutes.RETURN_HOME

/** 이 문제부터 시작(정원에서 탭). 지정이 없으면 null. */
@Composable
private fun NavBackStackEntry.reviewStartQuizId(navController: NavHostController): Long? =
    reviewGraphArgs(navController)?.getLong("start", -1L)?.takeIf { it > 0 }

/** 복습 그래프 전체가 공유하는 ViewModel. */
@Composable
private fun NavBackStackEntry.reviewViewModel(
    navController: NavHostController,
    reviewRepository: ReviewRepository,
): ReviewSessionViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(FinQRoutes.REVIEW_GRAPH)
    }
    val start = remember(this) {
        runCatching {
            navController.getBackStackEntry(FinQRoutes.REVIEW_QUIZ).arguments
                ?.getLong("start", -1L)
        }.getOrNull()?.takeIf { it > 0 }
    }
    val factory = remember(reviewRepository, start) {
        ReviewSessionViewModel.factory(reviewRepository, start)
    }
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

/**
 * 하단 탭 바 — "지평선" 인디케이터.
 *
 * M3 NavigationBar 의 알약 인디케이터·80dp 슬래브를 버리고 직접 그린다.
 *   · 배경은 BgBase — 화면과 같은 색이라 바가 판처럼 떠 보이지 않고 화면이 그대로 이어진다.
 *   · 바 상단 1dp Outline 헤어라인이 곧 인디케이터다. 활성 탭 구간만 Lime 으로 굵고 밝게
 *     칠하고, 탭 전환 시 그 구간이 옆으로 미끄러진다. 라벨 아래에 요소를 더 쌓지 않는다.
 *   · 활성 표시는 4중(지평선 + 솔리드 아이콘 + Lime + 라벨 볼드) — 색만으로 정보를
 *     전달하지 않는다(WCAG 1.4.1).
 *   · windowInsetsPadding 을 배경 뒤에 둬서 시스템 내비 영역까지 BgBase 로 깔리되
 *     터치 타깃은 인셋 위로 올라온다(edge-to-edge).
 */
@Composable
private fun FinQBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    // 보관함은 optional 인자 라우트(library_tab?focusQuizId=…)로 도착할 수 있어
    // 쿼리 부분을 떼고 base 라우트로 비교한다 — 딥링크 진입 시에도 탭 하이라이트 유지.
    val baseRoute = currentRoute?.substringBefore("?")
    val selectedIndex = bottomNavItems.indexOfFirst { it.route == baseRoute }
    val tabCount = bottomNavItems.size

    // 활성 구간의 위치(탭 인덱스 단위) — 탭 전환 시 지평선이 옆으로 미끄러진다.
    val position by animateFloatAsState(
        targetValue = selectedIndex.coerceAtLeast(0).toFloat(),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "horizon",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBase)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        HorizonIndicator(
            position = position,
            tabCount = tabCount,
            visible = selectedIndex >= 0,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarContentHeight)
                // 홈 네온 물주기 버튼과 같은 언어 — 라임이 바 안쪽으로만 은은하게 번진다.
                .clipToBounds()
                .drawBehind {
                    if (selectedIndex < 0) return@drawBehind
                    val centerX = size.width / tabCount * (position + 0.5f)
                    val radius = 46.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to Lime.copy(alpha = 0.13f),
                            0.4f to Lime.copy(alpha = 0.05f),
                            1f to Color.Transparent,
                            center = Offset(centerX, 0f),
                            radius = radius,
                        ),
                        radius = radius,
                        center = Offset(centerX, 0f),
                    )
                },
        ) {
            bottomNavItems.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            onClick = { onNavigate(item.route) },
                            role = Role.Tab,
                        )
                        // 아이콘이 지평선에 붙어 답답해 보이지 않도록 상단 여백을 준다.
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(
                            if (selected) item.filledIconRes else item.iconRes
                        ),
                        contentDescription = item.label,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (selected) Lime else TextMuted
                        ),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Lime else TextMuted,
                    )
                }
            }
        }
    }
}

/** 탭 바 콘텐츠 높이 — M3 Expressive 기준 64dp (구형 80dp 슬래브를 쓰지 않는다). */
private val BottomBarContentHeight = 64.dp

/**
 * 지평선 — 평소엔 Outline 1dp 헤어라인, 활성 탭 구간만 Lime 2.5dp.
 * 탭 전환 시 라임 구간이 220ms 동안 옆으로 미끄러진다(유휴 모션 없음).
 */
@Composable
private fun HorizonIndicator(position: Float, tabCount: Int, visible: Boolean) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(HorizonHeight),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Outline),
        )
        if (visible) {
            val tabWidth = maxWidth / tabCount
            val segmentWidth = tabWidth * 0.45f
            val offsetX = tabWidth * position + (tabWidth - segmentWidth) / 2
            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .width(segmentWidth)
                    .height(HorizonHeight)
                    .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                    .background(Lime),
            )
        }
    }
}

/** 지평선 두께 — 헤어라인 위에 얹히는 라임 구간의 높이. */
private val HorizonHeight = 2.5.dp

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

/**
 * 단건 풀이 라우트 — 풀이(QuizScreen) → 채점 후 해설(QuizAnswerScreen)을 한 라우트에서 전환한다.
 * 1문제짜리라 진행도는 1/1 로 표시된다.
 */
@Composable
private fun SoloQuizRoute(
    viewModel: SoloQuizViewModel,
    libraryRepository: LibraryRepository,
    onExit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val localContext = LocalContext.current
    val item = state.item

    // 삭제된 문제(404) 또는 이미 푼 문제(낡은 목록) — 안내 후 목록으로 복귀.
    // Toast 사용: 스낵바는 화면 이탈 시 코루틴이 취소돼 안내가 사라질 수 있다.
    LaunchedEffect(state.notFound, state.alreadySolved) {
        if (state.notFound) {
            Toast.makeText(localContext, "문제를 찾을 수 없어요 — 북마크를 정리해 주세요", Toast.LENGTH_SHORT).show()
            onExit()
        } else if (state.alreadySolved) {
            Toast.makeText(localContext, "이미 푼 문제예요 — 카드를 눌러 해설을 확인해 보세요", Toast.LENGTH_SHORT).show()
            onExit()
        }
    }

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Lime)
            }
        }
        state.error != null && state.lastAnswer == null -> {
            ReviewErrorBox(message = state.error!!, onRetry = viewModel::load)
        }
        item == null || state.alreadySolved || state.notFound -> {
            // 복귀 대기 (LaunchedEffect 가 곧 onExit)
        }
        state.lastAnswer != null -> {
            QuizAnswerScreen(
                quiz = item.toSoloQuiz(),
                answer = state.lastAnswer!!,
                isLast = true,
                quizIndex = 0,
                totalCount = 1,
                onNext = onExit,
                onBack = onExit,
                onArticleClick = { article ->
                    val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                    try {
                        localContext.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(localContext, "기사를 열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show()
                    }
                },
                libraryRepository = libraryRepository,
                initialBookmarked = item.bookmarked,
                nextLabel = "완료",
            )
        }
        else -> {
            QuizScreen(
                quizIndex = 0,
                totalCount = 1,
                quiz = item.toSoloQuiz(),
                selectedOptionId = state.selectedOptionId,
                onSelectOption = viewModel::selectOption,
                onSubmit = viewModel::submitAnswer,
                onClose = onExit,
                isSubmitting = state.isSubmitting,
                headerNote = "북마크한 문제 — 지금 풀면 오늘 기록으로 반영돼요",
            )
        }
    }
}

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

    // 채점 제출 실패 → 문제 화면을 유지한 채 스낵바로만. 고른 답이 남아 바로 재시도된다.
    LaunchedEffect(Unit) {
        viewModel.submitErrors.collect { msg ->
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
        com.finq.app.ui.components.ReviewTreeConceptSheet(
            title = "첫 복습 나무가 태어났어요",
            confirmLabel = "키워볼게요",
            // 참조가 아니라 축하 모먼트 — 히어로를 방금 난 새싹 단독으로 바꾼다.
            variant = com.finq.app.ui.components.ReviewTreeConceptVariant.CELEBRATION,
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
