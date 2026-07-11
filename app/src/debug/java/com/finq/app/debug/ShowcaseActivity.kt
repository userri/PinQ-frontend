package com.finq.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.QuizOption
import com.finq.app.ui.library.AttemptItemCard
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.repository.AnswerResult
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import com.finq.app.ui.components.ConceptStatsCard
import com.finq.app.ui.components.GrassCalendarCard
import com.finq.app.ui.components.WaterGrassCard
import com.finq.app.ui.screen.ReviewDoneScreen
import com.finq.app.ui.screen.HomeScreen
import com.finq.app.ui.screen.MyPageContent
import com.finq.app.ui.screen.QuizAnswerScreen
import com.finq.app.ui.screen.QuizScreen
import com.finq.app.ui.theme.FinQTheme
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * 디버그 전용 색상 검증 화면. 로그인/네트워크 없이 각 화면을 실제 테마로 렌더링한다.
 *
 *   adb shell am start -n com.finq.app/com.finq.app.debug.ShowcaseActivity --es screen home
 *
 * screen: home | quiz | answer | mypage | wrongnote | grass | review | concept
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
                    "grass" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        GrassCalendarCard(grass = sampleGrass)
                    }

                    // 복습 진입 카드 3상태 + 완료 화면
                    "review" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        WaterGrassCard(reviewCount = 3, nextDueDate = null, onClick = {})
                        Spacer(Modifier.height(12.dp))
                        WaterGrassCard(reviewCount = 0, nextDueDate = LocalDate.now().plusDays(4), onClick = {})
                        Spacer(Modifier.height(12.dp))
                        WaterGrassCard(reviewCount = 0, nextDueDate = null, onClick = {})
                        Spacer(Modifier.height(24.dp))
                        ReviewDoneScreen(
                            reviewedCount = 3,
                            correctCount = 2,
                            graduatedCount = 1,
                            nextDueDate = LocalDate.now().plusDays(4),
                            onGoHome = {},
                        )
                    }

                    // 복습 졸업 순간 (graduated=true) — 나무 축하 배너
                    "review_graduated" -> QuizAnswerScreen(
                        quiz = sampleQuiz,
                        answer = AnswerResult(
                            quizId = 1L, selectedOptionId = 1L, isCorrect = true,
                            correctOptionId = 1L,
                            explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
                            keyword = "기준금리", relatedArticle = RelatedArticle.EMPTY,
                        ),
                        isLast = true, quizIndex = 0, totalCount = 2,
                        onNext = {}, onBack = {}, onArticleClick = {},
                        categoryLabel = "🪴 나무 직전 · 금리",
                        graduated = true,
                        nextLabel = "복습 완료",
                    )

                    // 복습 미졸업 (graduated=false) — 다음 물 주기 안내
                    "review_next" -> QuizAnswerScreen(
                        quiz = sampleQuiz,
                        answer = AnswerResult(
                            quizId = 1L, selectedOptionId = 1L, isCorrect = true,
                            correctOptionId = 1L,
                            explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
                            keyword = "기준금리", relatedArticle = RelatedArticle.EMPTY,
                        ),
                        isLast = false, quizIndex = 0, totalCount = 2,
                        onNext = {}, onBack = {}, onArticleClick = {},
                        categoryLabel = "🌿 풀 · 금리",
                        graduated = false,
                        nextReviewText = "다음 물 주기: 7월 15일",
                        nextLabel = "다음 복습",
                    )

                    "concept" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        ConceptStatsCard(sampleConcepts)
                        Spacer(Modifier.height(16.dp))
                        ConceptStatsCard(sampleConcepts.copy(weakest = null))
                    }

                    "mypage" -> MyPageContent(
                        grass = sampleGrass,
                        conceptStats = sampleConcepts,
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

                    // 카테고리 필터 칩 — enum 동적 생성 확인 (물가 포함 5개 + 전체)
                    "filters" -> com.finq.app.ui.library.LibraryListScreen(
                        title = "오답노트",
                        subtitle = "6문제",
                        items = listOf(
                            sampleAttempt(correct = false),
                            sampleAttempt(correct = true).copy(
                                category = Category.fromServer("INFLATION"),
                                question = "소비자물가지수(CPI)가 상승하면 실질 구매력은?",
                            ),
                        ),
                        isLoading = false,
                        error = null,
                        emptyMessage = "",
                        emptyIconRes = com.finq.app.R.drawable.ic_bookmark_star,
                        onRetry = {},
                        onToggleBookmark = {},
                    )

                    "wrongnote" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    ) {
                        AttemptItemCard(item = sampleAttempt(correct = true), onToggleBookmark = {})
                        AttemptItemCard(item = sampleAttempt(correct = false), onToggleBookmark = {})
                        // 세션 직후 오답노트와 동일한 형태(solvedAtIso=null, 정답정보 있음).
                        // 예전엔 이게 "아직 안 푼 문제"로 오판돼 카드가 안 펼쳐졌다 → 이제 정상.
                        AttemptItemCard(
                            item = sampleAttempt(correct = false).copy(solvedAtIso = null),
                            onToggleBookmark = {},
                        )
                        // 신규 카테고리 INFLATION("물가") 표시 확인
                        AttemptItemCard(
                            item = sampleAttempt(correct = true).copy(
                                category = com.finq.app.data.model.Category.fromServer("INFLATION"),
                                question = "소비자물가지수(CPI)가 상승하면 실질 구매력은 어떻게 되는가?",
                            ),
                            onToggleBookmark = {},
                        )
                        // 클라이언트가 모르는 카테고리 → UNKNOWN("기타") 폴백, 크래시 없어야 함
                        AttemptItemCard(
                            item = sampleAttempt(correct = true).copy(
                                category = com.finq.app.data.model.Category.fromServer("CRYPTO_FUTURE"),
                                question = "미지 카테고리 방어 파싱 확인용 문제",
                            ),
                            onToggleBookmark = {},
                        )
                        // 진짜 미풀이(마스킹): correctChoiceId=null → "아직 안 푼 문제" 표시돼야 함
                        AttemptItemCard(
                            item = sampleAttempt(correct = false).copy(
                                selectedChoiceId = null, correctChoiceId = null,
                                explanation = "", keyword = null, solvedAtIso = null,
                            ),
                            onToggleBookmark = {},
                        )
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
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
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

    /** 1년치 잔디 — 강도 0~4 가 모두 나오도록. days 는 서버처럼 활동일만 담는다(sparse). */
    private val sampleGrass: GrassCalendar by lazy {
        val today = LocalDate.now()
        val from = today.minusDays(364)
        val dayMap = (0..364).mapNotNull { offset ->
            val level = (offset * 7) % 6
            if (level !in 1..4) null
            else from.plusDays(offset.toLong()) to com.finq.app.data.repository.GrassDay(
                level = level,
                solved = if (level == 1) 0 else level,   // level 1 은 복습만 한 날로 흉내
                reviewed = if (level == 1) 2 else 0,
            )
        }.toMap()
        GrassCalendar(
            from = from,
            to = today,
            totalActiveDays = dayMap.size,
            perfectDays = dayMap.count { it.value.level == 4 },
            currentStreak = 7,
            maxStreak = 15,
            graduatedTrees = 4,
            dayByDate = dayMap,
        )
    }

    private val sampleConcepts: ConceptStats by lazy {
        val cats = listOf(
            ConceptStat("INTEREST_RATE", "금리", 20, 16, 0.80f),
            ConceptStat("EXCHANGE_RATE", "환율", 12, 4, 0.33f),
            ConceptStat("STOCK", "증시", 18, 13, 0.72f),
            ConceptStat("REAL_ESTATE", "부동산", 9, 6, 0.67f),
            ConceptStat("INFLATION", "물가", 5, 4, 0.80f),
        )
        ConceptStats(categories = cats, weakest = cats[1])
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
