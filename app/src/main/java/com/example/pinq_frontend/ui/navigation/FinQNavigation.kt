package com.example.pinq_frontend.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.pinq_frontend.data.model.RelatedArticle
import com.example.pinq_frontend.data.remote.NetworkModule
import com.example.pinq_frontend.data.repository.ApiQuizRepository
import com.example.pinq_frontend.data.repository.QuizRepository
import com.example.pinq_frontend.ui.home.HomeViewModel
import com.example.pinq_frontend.ui.quiz.QuizSessionViewModel
import com.example.pinq_frontend.ui.screen.HomeScreen
import com.example.pinq_frontend.ui.screen.QuizAnswerScreen
import com.example.pinq_frontend.ui.screen.ResultReportScreen
import com.example.pinq_frontend.ui.screen.QuizScreen

/**
 * FinQ 네비게이션 그래프.
 *
 * 구조:
 *   home
 *   session (nested graph)
 *     ├── session/quiz
 *     ├── session/answer
 *     └── session/done
 *
 * `session` 그래프 안의 세 화면은 같은 [QuizSessionViewModel] 인스턴스를 공유한다.
 * (NavBackStackEntry 를 부모 그래프의 것으로 잡아 viewModel() 에 전달)
 */
object FinQRoutes {
    const val HOME = "home"
    const val SESSION_GRAPH = "session"
    const val QUIZ = "session/quiz"
    const val ANSWER = "session/answer"
    const val DONE = "session/done"
}

@Composable
fun FinQNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    // Phase 2: 백엔드 API 를 호출하는 ApiQuizRepository 사용.
    // 더미 모드로 돌리고 싶으면 DummyQuizRepository() 로 교체.
    val repository: QuizRepository = remember { ApiQuizRepository(NetworkModule.quizApi) }

    NavHost(
        navController = navController,
        startDestination = FinQRoutes.HOME,
        modifier = modifier,
    ) {
        // ── 홈 화면 ──────────────────────────────────────────────────
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
                onStartQuiz = {
                    navController.navigate(FinQRoutes.SESSION_GRAPH)
                },
                onRetry = homeVm::loadQuizInfo,
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
                val context = LocalContext.current
                AnswerRoute(
                    viewModel = vm,
                    onNext = {
                        val state = vm.uiState.value
                        if (state.isLastQuiz) {
                            navController.navigate(FinQRoutes.DONE) {
                                // 퀴즈/정답 스택을 정리하고 done 만 남긴다.
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
                        // Phase 1: 외부 브라우저로 열기. Phase 2 에서 Custom Tab 으로 교체 검토.
                        val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                        context.startActivity(intent)
                    },
                )
            }
            composable(FinQRoutes.DONE) { entry ->
                val vm = entry.sessionViewModel(navController, repository)
                DoneRoute(
                    viewModel = vm,
                    onGoHome = {
                        // 세션 스택 전체를 정리하고 홈으로 돌아간다.
                        navController.navigate(FinQRoutes.HOME) {
                            popUpTo(FinQRoutes.HOME) { inclusive = true }
                        }
                    },
                    onRestart = {
                        // DONE 만 팝하고 QUIZ 로 이동해 같은 ViewModel 인스턴스 재사용.
                        vm.restart()
                        navController.navigate(FinQRoutes.QUIZ) {
                            popUpTo(FinQRoutes.DONE) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

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
// Route 래퍼: ViewModel 상태를 stateless 스크린으로 풀어 넘긴다.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuizRoute(
    viewModel: QuizSessionViewModel,
    onAfterSubmit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    // API 응답(lastAnswer)이 도착하면 Answer 화면으로 이동.
    // isSubmitting 이 false 로 바뀌고 lastAnswer 가 채워진 순간 딱 한 번만 트리거.
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
        // 채점 결과 도착 대기
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
) {
    val state by viewModel.uiState.collectAsState()
    ResultReportScreen(
        quizzes = state.quizzes,
        answerHistory = state.answerHistory,
        onGoHome = onGoHome,
        onRestart = onRestart,
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
