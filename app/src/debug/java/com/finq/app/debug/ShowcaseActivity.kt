package com.finq.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.QuizOption
import com.finq.app.ui.library.AttemptItemCard
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.repository.AnswerResult
import com.finq.app.ui.screen.HomeScreen
import com.finq.app.ui.screen.MyPageContent
import com.finq.app.ui.screen.QuizAnswerScreen
import com.finq.app.ui.screen.QuizScreen
import com.finq.app.ui.theme.FinQTheme

/**
 * 디버그 전용 색상 검증 화면. 로그인/네트워크 없이 각 화면을 실제 테마로 렌더링한다.
 *
 *   adb shell am start -n com.finq.app/com.finq.app.debug.ShowcaseActivity --es screen home
 *
 * screen: home | quiz | answer | mypage | wrongnote
 * 릴리즈 빌드에는 포함되지 않는다(app/src/debug 소스셋).
 */
class ShowcaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screen = intent.getStringExtra("screen") ?: "home"

        // 강도 0~4 를 모두 포함하도록 구성 (히트맵 램프 전 단계 확인용)
        val activityGrid = List(56) { i ->
            when {
                i % 7 == 0 -> 4   // 최고 단계 → StreakMax(라임)
                i % 5 == 0 -> 3
                i % 3 == 0 -> 2
                i % 2 == 0 -> 1
                else -> 0
            }
        }

        setContent {
            FinQTheme {
                when (screen) {
                    "mypage" -> MyPageContent(
                        nickname = "유리",
                        streak = 7,
                        maxStreak = 15,
                        totalSolved = 28,
                        correctRate = 0.75f,
                        activityGrid = activityGrid,
                        appVersion = "1.1.3",
                        notificationsEnabled = true,   // 알림 토글 ON = 라임
                        notificationTime = "06:00",
                    )

                    "wrongnote" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    ) {
                        AttemptItemCard(item = sampleAttempt(correct = true), onToggleBookmark = {})
                        AttemptItemCard(item = sampleAttempt(correct = false), onToggleBookmark = {})
                    }

                    // 보기 카드 4상태 중 기본/선택 확인
                    "quiz" -> QuizScreen(
                        quizIndex = 1,
                        totalCount = 3,
                        quiz = sampleQuiz,
                        selectedOptionId = 2L,   // 선택 상태 = BgSubtle + Lime 테두리
                        onSelectOption = {},
                        onSubmit = {},
                    )

                    // 보기 카드 4상태 중 정답 공개/오답 공개 확인
                    "answer" -> QuizAnswerScreen(
                        quiz = sampleQuiz,
                        answer = AnswerResult(
                            quizId = sampleQuiz.id,
                            selectedOptionId = 2L,   // 오답 공개 = ErrorFaint + Error
                            isCorrect = false,
                            correctOptionId = 1L,    // 정답 공개 = LimeFaint + Lime
                            explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
                            keyword = "기준금리",
                            relatedArticle = RelatedArticle(
                                title = "한은, 기준금리 0.25%p 인상",
                                url = "https://example.com",
                                source = "경제일보",
                            ),
                        ),
                        isLast = false,
                        quizIndex = 1,
                        totalCount = 3,
                        onNext = {},
                        onBack = {},
                        onArticleClick = {},
                    )

                    else -> HomeScreen(
                        quizCount = 3,
                        streak = 7,
                        maxStreak = 15,
                        activityGrid = activityGrid,
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                    )
                }
            }
        }
    }

    private val sampleQuiz = Quiz(
        id = 1L,
        category = Category.INTEREST_RATE,
        question = "기준금리를 인상하면 일반적으로 나타나는 현상은?",
        options = listOf(
            QuizOption(1, 1, "예금 금리가 오른다"),
            QuizOption(2, 2, "대출이 늘어난다"),
            QuizOption(3, 3, "물가가 급등한다"),
            QuizOption(4, 4, "환율이 급락한다"),
        ),
    )

    private fun sampleAttempt(correct: Boolean) = AttemptItem(
        quizId = if (correct) 1L else 2L,
        category = Category.INTEREST_RATE,
        question = "기준금리를 인상하면 일반적으로 나타나는 현상은?",
        choices = listOf(
            QuizOption(1, 1, "예금 금리가 오른다"),
            QuizOption(2, 2, "대출이 늘어난다"),
            QuizOption(3, 3, "물가가 급등한다"),
            QuizOption(4, 4, "환율이 급락한다"),
        ),
        selectedChoiceId = if (correct) 1L else 2L,
        correctChoiceId = 1L,
        correct = correct,
        explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
        keyword = "기준금리",
        article = null,
        bookmarked = correct,
        solvedAtIso = "2026-07-08T09:00:00",
    )
}
