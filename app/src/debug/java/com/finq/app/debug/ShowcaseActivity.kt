package com.finq.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.ReviewStatus
import com.finq.app.ui.library.AttemptCardEmphasis
import com.finq.app.ui.library.AttemptDetailScreen
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.repository.AnswerResult
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter as ColorFilterIcon
import androidx.compose.ui.res.painterResource
import com.finq.app.R
import com.finq.app.ui.theme.BgElevated as BgElevatedIcon
import com.finq.app.ui.theme.TextMuted as TextMutedIcon
import com.finq.app.ui.components.ConceptStatsSection
import com.finq.app.ui.components.ForcedUpdateDialog
import com.finq.app.ui.components.NoticeDialog
import com.finq.app.ui.components.ReviewTreeConceptSheet
import com.finq.app.ui.components.ReviewTreeConceptVariant
import com.finq.app.ui.components.garden.GardenCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.components.GrassCalendarCard
import com.finq.app.ui.components.WaterGrassCard
import com.finq.app.ui.screen.GardenScreen
import com.finq.app.ui.screen.LoginScreen
import com.finq.app.ui.screen.ReviewDoneScreen
import com.finq.app.ui.screen.HomeScreen
import com.finq.app.ui.screen.MyPageContent
import com.finq.app.ui.screen.QuizAnswerScreen
import com.finq.app.ui.screen.QuizScreen
import com.finq.app.ui.screen.TasteQuizScreen
import com.finq.app.ui.theme.FinQTheme
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * 디버그 전용 색상 검증 화면. 로그인/네트워크 없이 각 화면을 실제 테마로 렌더링한다.
 *
 *   adb shell am start -n com.finq.app/com.finq.app.debug.ShowcaseActivity --es screen home
 *
 * screen: home | home_pending | home_zero | home_done_water | home_done | quiz | answer | solo_quiz | solo_answer | solved_correct | solved_wrong | mypage | mypage_loading | mypage_grass_error | mypage_trees_none | mypage_trees_zero | mypage_trees_few | mypage_trees_many | filters | wrongnote | history | detail_wrong | detail_correct | detail_graduated | detail_loading | detail_error | list_error | grass | review | review_graduated | review_next | garden | garden_empty | garden_growing_many | garden_trees_few | garden_trees_many | garden_canvas | concept | concept_tie | concept_sheet | concept_sheet_intro | onboarding | onboarding_grass | onboarding_tree | onboarding_replay | app_icon | store_icon | taste | home_feedback | review_grown | review_grown_last | review_wrong | wrongnote_store | version_gate | notice
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

                    // 복습 진입 카드 4상태 + 완료 화면
                    "review" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        // 미착수 — 남은 수가 곧 오늘 전체다.
                        WaterGrassCard(reviewCount = 3, nextDueDate = null, onClick = {})
                        Spacer(Modifier.height(12.dp))
                        // 진행중 — 5개 중 2개를 준 상태. 분모가 보여야 잔량으로 읽힌다.
                        WaterGrassCard(
                            reviewCount = 3,
                            nextDueDate = null,
                            onClick = {},
                            reviewedToday = 2,
                            grownToday = 1,
                        )
                        Spacer(Modifier.height(12.dp))
                        // 완료 — 오늘 다 줬다.
                        WaterGrassCard(
                            reviewCount = 0,
                            nextDueDate = LocalDate.now().plusDays(4),
                            onClick = {},
                            reviewedToday = 5,
                            grownToday = 4,
                        )
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
                        categoryLabel = "나무 직전 · 금리",
                        graduated = true,
                        graduatedMessage = "당신의 5번째 나무",
                        nextLabel = "복습 완료",
                    )

                    // 복습 성장 밴드 — 맞혀서 한 단계 자란 순간(게이지 2칸 + 나무).
                    "review_grown" -> QuizAnswerScreen(
                        quiz = sampleQuiz,
                        answer = AnswerResult(
                            quizId = 1L, selectedOptionId = 1L, isCorrect = true,
                            correctOptionId = 1L,
                            explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
                            keyword = "기준금리", relatedArticle = RelatedArticle.EMPTY,
                        ),
                        isLast = false, quizIndex = 0, totalCount = 3,
                        onNext = {}, onBack = {}, onArticleClick = {},
                        categoryLabel = "풀 · 금리",
                        nextReviewText = "다음 물주기 8월 10일",
                        reviewStage = 1,
                        nextLabel = "다음 복습",
                    )

                    // 마지막 단계(stage 2) — 문구가 가장 길어 사다리와 폭을 다투는 케이스.
                    "review_grown_last" -> QuizAnswerScreen(
                        quiz = sampleQuiz,
                        answer = AnswerResult(
                            quizId = 1L, selectedOptionId = 1L, isCorrect = true,
                            correctOptionId = 1L,
                            explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
                            keyword = "기준금리", relatedArticle = RelatedArticle.EMPTY,
                        ),
                        isLast = false, quizIndex = 0, totalCount = 3,
                        onNext = {}, onBack = {}, onArticleClick = {},
                        categoryLabel = "나무 직전 · 금리",
                        nextReviewText = "다음 물주기 8월 17일",
                        reviewStage = 2,
                        nextLabel = "다음 복습",
                    )

                    // 복습 오답 — 게이지를 그리지 않는다(리셋을 화면이 말하지 않음).
                    "review_wrong" -> QuizAnswerScreen(
                        quiz = sampleQuiz,
                        answer = AnswerResult(
                            quizId = 1L, selectedOptionId = 2L, isCorrect = false,
                            correctOptionId = 1L,
                            explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
                            keyword = "기준금리", relatedArticle = RelatedArticle.EMPTY,
                        ),
                        isLast = false, quizIndex = 1, totalCount = 3,
                        onNext = {}, onBack = {}, onArticleClick = {},
                        categoryLabel = "새싹 · 금리",
                        nextReviewText = "다음 물주기 8월 6일",
                        reviewStage = 0,
                        nextLabel = "다음 복습",
                    )

                    // 정원 — 자라는 중 + 완성 나무 + 카운터 불일치(배포 이전 졸업분) 케이스
                    // 정원 캔버스(잔디+나무 Canvas) — 빈/성장/만원 3케이스
                    "garden_canvas" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        Text("정원 · 빈")
                        GardenCanvas(
                            garden = ReviewGarden.EMPTY,
                            compact = true,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                        )
                        Spacer(Modifier.height(16.dp))

                        Text("정원 · 성장")
                        GardenCanvas(
                            garden = ReviewGarden(
                                growing = listOf(
                                    gardenSample(1, ReviewStage.SPROUT),
                                    gardenSample(2, ReviewStage.GRASS),
                                    gardenSample(3, ReviewStage.ALMOST_TREE),
                                    gardenSample(4, ReviewStage.SPROUT),
                                    gardenSample(5, ReviewStage.GRASS),
                                ),
                                graduated = listOf(
                                    gardenSample(101, ReviewStage.ALMOST_TREE).copy(graduatedAtIso = "2026-07-19T12:00:00"),
                                    gardenSample(102, ReviewStage.ALMOST_TREE).copy(graduatedAtIso = "2026-07-20T12:00:00"),
                                ),
                                graduatedTrees = 4,
                            ),
                            compact = true,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                        )
                        Spacer(Modifier.height(16.dp))

                        Text("정원 · 만원(+N)")
                        GardenCanvas(
                            garden = ReviewGarden(
                                growing = List(15) { i ->
                                    gardenSample(
                                        200L + i,
                                        ReviewStage.values()[i % ReviewStage.values().size],
                                    )
                                },
                                graduated = emptyList(),
                                graduatedTrees = 20,
                            ),
                            compact = true,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                        )
                    }

                    "garden" -> GardenScreen(
                        garden = ReviewGarden(
                            growing = listOf(
                                GardenItem(
                                    quizId = 101, categoryLabel = "주식", question = "PER이 낮다는 것은 무엇을 의미할까요?",
                                    keyword = "PER", stage = ReviewStage.SPROUT,
                                    dueDate = LocalDate.now().plusDays(1),
                                    waterCount = 1, absorbedCount = 0, graduatedAtIso = null,
                                ),
                                GardenItem(
                                    quizId = 102, categoryLabel = "금리", question = "기준금리가 오르면 예금 금리는?",
                                    keyword = "기준금리", stage = ReviewStage.ALMOST_TREE,
                                    dueDate = LocalDate.now().plusDays(3),
                                    waterCount = 4, absorbedCount = 2, graduatedAtIso = null,
                                ),
                            ),
                            graduated = listOf(
                                GardenItem(
                                    quizId = 88, categoryLabel = "경제", question = "인플레이션의 정의는?",
                                    keyword = "인플레이션", stage = ReviewStage.ALMOST_TREE,
                                    dueDate = null, waterCount = 5, absorbedCount = 4,
                                    graduatedAtIso = "2026-07-19T14:32:00",
                                ),
                            ),
                            graduatedTrees = 12, // 목록(1)보다 큰 카운터 — 안내 문구 확인용
                        ),
                        isLoading = false,
                        error = null,
                        onRetry = {},
                        onBack = {},
                        onOpenQuiz = {},
                    )

                    // 정원 리디자인 검증 케이스 — 빈/자라는중만 다수(실계정 상태)/나무 소수/나무 다수
                    "garden_empty" -> GardenScreen(
                        garden = ReviewGarden.EMPTY,
                        isLoading = false, error = null,
                        onRetry = {}, onBack = {}, onOpenQuiz = {},
                    )

                    // 실계정 상태 재현: 자라는 중 49 · 나무 0 · 일부 due(오늘 물 주기 가능)
                    "garden_growing_many" -> GardenScreen(
                        garden = ReviewGarden(
                            growing = List(49) { i ->
                                gardenSample(
                                    300L + i,
                                    ReviewStage.values()[i % ReviewStage.values().size],
                                ).copy(
                                    waterCount = i % 7,
                                    dueDate = if (i % 5 == 0) LocalDate.now() else LocalDate.now().plusDays(3),
                                    // due 는 10개지만 서버가 캡(5)을 적용해 5개만 오늘 세트다.
                                    // 후광도 이 플래그를 따르므로 배지 숫자와 개수가 맞는다.
                                    inTodayQueue = i % 5 == 0 && i < 25,
                                )
                            },
                            graduated = emptyList(),
                            graduatedTrees = 0,
                            // due 는 10개지만 서버가 하루 캡(5)을 적용해 내려준다 —
                            // 배지가 큐 캡을 따르는지 확인하는 케이스.
                            todayQueueSize = 5,
                        ),
                        isLoading = false, error = null,
                        onRetry = {}, onBack = {}, onOpenQuiz = {},
                    )

                    "garden_trees_few" -> GardenScreen(
                        garden = ReviewGarden(
                            growing = List(6) { i -> gardenSample(400L + i, ReviewStage.values()[i % 3]) },
                            graduated = List(3) { i ->
                                gardenSample(500L + i, ReviewStage.ALMOST_TREE)
                                    .copy(graduatedAtIso = "2026-07-1${i + 1}T12:00:00")
                            },
                            graduatedTrees = 3,
                        ),
                        isLoading = false, error = null,
                        onRetry = {}, onBack = {}, onOpenQuiz = {},
                    )

                    // 나무 다수 스케일링 — 카운터(40)가 목록(10)보다 큰 레거시 포함
                    "garden_trees_many" -> GardenScreen(
                        garden = ReviewGarden(
                            growing = List(12) { i -> gardenSample(600L + i, ReviewStage.values()[i % 3]) },
                            graduated = List(10) { i ->
                                gardenSample(700L + i, ReviewStage.ALMOST_TREE)
                                    .copy(graduatedAtIso = "2026-07-0${(i % 9) + 1}T12:00:00")
                            },
                            graduatedTrees = 40,
                        ),
                        isLoading = false, error = null,
                        onRetry = {}, onBack = {}, onOpenQuiz = {},
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
                        categoryLabel = "풀 · 금리",
                        graduated = false,
                        nextReviewText = "다음 물 주기: 7월 15일",
                        nextLabel = "다음 복습",
                    )

                    // 복습 나무 개념 시트 — 참조형(정원 "?") / 축하형(첫 오답 직후)
                    "concept_sheet", "concept_sheet_intro" -> {
                        val intro = screen == "concept_sheet_intro"
                        var open by remember { mutableStateOf(true) }
                        Box(
                            Modifier.fillMaxSize().background(BgBase),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "시트 다시 열기",
                                color = Lime,
                                modifier = Modifier.clickable { open = true },
                            )
                        }
                        if (open) ReviewTreeConceptSheet(
                            title = if (intro) "첫 복습 나무가 태어났어요" else "복습 나무란?",
                            confirmLabel = if (intro) "키워볼게요" else "알겠어요",
                            variant = if (intro) ReviewTreeConceptVariant.CELEBRATION
                                      else ReviewTreeConceptVariant.REFERENCE,
                            onDismiss = { open = false },
                        )
                    }

                    // 첫 실행 온보딩 — 장별 + 완료 직전(마지막 장) + 마이페이지 재열람.
                    // onboarding_tree 가 곧 "완료 직전" 상태다(CTA 가 "시작하기").
                    "onboarding", "onboarding_grass", "onboarding_tree", "onboarding_replay" ->
                        com.finq.app.ui.onboarding.OnboardingScreen(
                            onFinish = {},
                            onSkip = {},
                            replay = screen == "onboarding_replay",
                            initialPage = when (screen) {
                                "onboarding_grass" -> 1
                                "onboarding_tree", "onboarding_replay" -> 2
                                else -> 0
                            },
                            // 앱에서는 FinQNavHost 의 Scaffold 가 시스템 인셋을 준다.
                            // 쇼케이스는 Scaffold 없이 바로 띄우므로 여기서 직접 넣는다.
                            modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
                        )

                    "concept" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        ConceptStatsSection(sampleConcepts)
                        Spacer(Modifier.height(16.dp))
                        ConceptStatsSection(sampleConcepts.copy(weakest = null))
                    }

                    // 동률 지목 · 지목 과다 시 숨김
                    "concept_tie" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        ConceptStatsSection(tiedConcepts)
                        Spacer(Modifier.height(16.dp))
                        ConceptStatsSection(allAboveBarConcepts)
                        Spacer(Modifier.height(16.dp))
                        ConceptStatsSection(flatLowConcepts)
                        Spacer(Modifier.height(16.dp))
                        ConceptStatsSection(allLowVariedConcepts)
                        Spacer(Modifier.height(16.dp))
                        ConceptStatsSection(allGoodConcepts)
                    }

                    // 잔디밭 로딩 스켈레톤 (grass=null) — stale flash 수정 확인용
                    "mypage_loading" -> MyPageContent(
                        grass = null,
                        conceptStats = null,
                        nickname = "유리",
                        totalSolved = 28,
                        correctRate = 0.75f,
                        appVersion = "1.1.3",
                    )

                    // 잔디밭 첫 로드 실패 → 재시도 카드
                    "mypage_grass_error" -> MyPageContent(
                        grass = null,
                        grassFailed = true,
                        conceptStats = null,
                        nickname = "유리",
                        totalSolved = 28,
                        correctRate = 0.75f,
                        appVersion = "1.1.3",
                    )

                    "mypage" -> MyPageContent(
                        grass = sampleGrass,
                        garden = sampleGardenGrowing,
                        conceptStats = sampleConcepts,
                        nickname = "유리",
                        totalSolved = 28,
                        correctRate = 0.75f,
                        appVersion = "1.1.3",
                        notificationsEnabled = true,   // 알림 토글 ON = 라임
                        notificationTime = "06:00",
                    )

                    // 복습 나무 기록 밴드 — 0그루. 이 상태가 초라해 보이면 실패다.
                    "mypage_trees_zero" -> MyPageContent(
                        grass = sampleGrass.copy(graduatedTrees = 0),
                        garden = ReviewGarden(
                            growing = listOf(
                                gardenSample(1, ReviewStage.SPROUT),
                                gardenSample(2, ReviewStage.GRASS),
                                gardenSample(3, ReviewStage.SPROUT),
                            ),
                            graduated = emptyList(),
                            graduatedTrees = 0,
                        ),
                        conceptStats = sampleConcepts,
                        nickname = "유리",
                        totalSolved = 3, correctRate = 0.33f,
                        appVersion = "1.1.3",
                    )

                    // 0그루 + 자라는 것도 없음(완전 신규) — 유도 카피만 남는 최소 상태.
                    "mypage_trees_none" -> MyPageContent(
                        grass = sampleGrass.copy(graduatedTrees = 0),
                        garden = ReviewGarden.EMPTY,
                        conceptStats = null,
                        nickname = "유리",
                        totalSolved = 0, correctRate = 0f,
                        appVersion = "1.1.3",
                    )

                    // 소수(2그루) — 아이콘 스트립이 짧을 때 균형.
                    "mypage_trees_few" -> MyPageContent(
                        grass = sampleGrass.copy(graduatedTrees = 2),
                        garden = ReviewGarden(
                            growing = listOf(gardenSample(1, ReviewStage.ALMOST_TREE)),
                            graduated = emptyList(),
                            graduatedTrees = 2,
                        ),
                        conceptStats = sampleConcepts,
                        nickname = "유리",
                        totalSolved = 12, correctRate = 0.6f,
                        appVersion = "1.1.3",
                    )

                    // 다수(137그루) — 스트립 상한(5개)과 세 자리 숫자 폭 확인.
                    "mypage_trees_many" -> MyPageContent(
                        grass = sampleGrass.copy(graduatedTrees = 137),
                        garden = ReviewGarden(
                            growing = List(9) { i ->
                                gardenSample(100L + i, ReviewStage.entries[i % ReviewStage.entries.size])
                            },
                            graduated = emptyList(),
                            graduatedTrees = 137,
                        ),
                        conceptStats = sampleConcepts,
                        nickname = "유리",
                        totalSolved = 412, correctRate = 0.81f,
                        appVersion = "1.1.3",
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
                        onOpenDetail = {},
                        // 오답노트 화면 — 앞에 세우는 건 카테고리, "오답"은 나오지 않는다.
                        cardEmphasis = AttemptCardEmphasis.CATEGORY,
                    )

                    // 목록 밀도 — 한 화면에 6개 이상 들어와야 한다.
                    // 카드가 아니라 구분선 행이고, 진척(물 N/3·단계·예정일)은 나오지 않는다.
                    // 스토어 스크린샷 촬영용 — 방어 파싱 샘플("기타")과 날짜 없는 항목을 뺀
                    // 깨끗한 목록. "wrongnote" 케이스는 그 두 가지를 검증해야 하므로 그대로 둔다.
                    "wrongnote_store" -> com.finq.app.ui.library.LibraryListScreen(
                        title = "오답노트",
                        subtitle = "7문제",
                        items = listOf(
                            sampleAttempt(correct = false).copy(
                                review = ReviewStatus(0, 3, 0, false, LocalDate.now().toString()),
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 11L,
                                question = "정부가 재개발·재건축 이주비 대출 한도를 신축기준으로 바꾸면 조합원 부담은 어떻게 달라지는가?",
                                review = ReviewStatus(1, 1, 1, false, LocalDate.now().toString()),
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 12L,
                                question = "환율이 오르면 수입 물가는 어떻게 되는가?",
                                review = ReviewStatus(2, 7, 4, true, null),
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 13L,
                                category = Category.fromServer("INFLATION"),
                                question = "소비자물가지수(CPI)가 상승하면 실질 구매력은 어떻게 되는가?",
                                review = ReviewStatus(2, 14, 5, false, LocalDate.now().toString()),
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 16L,
                                question = "기준금리 인하기에 채권 가격은 일반적으로 어떻게 움직이는가?",
                                review = ReviewStatus(1, 7, 2, false, LocalDate.now().toString()),
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 17L,
                                question = "총부채원리금상환비율(DSR) 규제가 강화되면 대출 한도는?",
                                review = ReviewStatus(0, 3, 1, false, LocalDate.now().toString()),
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 18L,
                                category = Category.fromServer("EXCHANGE_RATE"),
                                question = "원·달러 환율이 오를 때 수출 기업의 실적은 일반적으로?",
                                review = ReviewStatus(2, 14, 4, false, LocalDate.now().toString()),
                            ),
                        ),
                        isLoading = false,
                        error = null,
                        emptyMessage = "",
                        emptyIconRes = com.finq.app.R.drawable.ic_trophy,
                        onRetry = {},
                        onToggleBookmark = {},
                        onOpenDetail = {},
                        cardEmphasis = AttemptCardEmphasis.CATEGORY,
                        showTitle = false,
                    )

                    // 졸업 항목만 나무 아이콘으로 구분된다.
                    "wrongnote" -> com.finq.app.ui.library.LibraryListScreen(
                        title = "오답노트",
                        subtitle = "8문제",
                        items = listOf(
                            sampleAttempt(correct = false),
                            sampleAttempt(correct = false).copy(
                                quizId = 11L,
                                question = "정부가 재개발·재건축 이주비 대출 한도를 신축기준으로 바꾸면 조합원 부담은 어떻게 달라지는가?",
                                review = ReviewStatus(1, 1, 1, false, LocalDate.now().toString()),
                            ),
                            // 졸업 — 유일한 진척 표시(나무 아이콘)
                            sampleAttempt(correct = false).copy(
                                quizId = 12L,
                                question = "환율이 오르면 수입 물가는 어떻게 되는가?",
                                review = ReviewStatus(2, 7, 4, true, null),
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 13L,
                                category = Category.fromServer("INFLATION"),
                                question = "소비자물가지수(CPI)가 상승하면 실질 구매력은 어떻게 되는가?",
                            ),
                            // 클라이언트가 모르는 카테고리 → "기타" 폴백, 크래시 없어야 함
                            sampleAttempt(correct = false).copy(
                                quizId = 14L,
                                category = Category.fromServer("CRYPTO_FUTURE"),
                                question = "미지 카테고리 방어 파싱 확인용 문제",
                            ),
                            // 세션 직후 오답노트와 같은 형태 (solvedAtIso=null) — 날짜 자리가 빈다
                            sampleAttempt(correct = false).copy(quizId = 15L, solvedAtIso = null),
                            // 제목(개념어)이 카테고리명과 **같은 낱말**인 과거 발행분.
                            // 메타에서 카테고리를 빼되 줄 자리는 남겨야 한다 — 줄을
                            // 없애면 이 행만 키가 작아져 목록에 계단이 생긴다.
                            // 백엔드가 8/5 에 저장 전 폐기를 넣어 신규분엔 안 생기지만,
                            // 이미 발행된 12건이 남아 있어 방어가 필요하다.
                            sampleAttempt(correct = false).copy(
                                quizId = 18L,
                                keyword = "금리: 돈을 빌리는 값. 기준금리가 시중 금리의 바탕이 된다",
                                question = "기준금리가 오르면 시중 예금 금리는 어떻게 되는가?",
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 16L,
                                question = "기준금리 인하기에 채권 가격은 일반적으로 어떻게 움직이는가?",
                            ),
                            sampleAttempt(correct = false).copy(
                                quizId = 17L,
                                question = "총부채원리금상환비율(DSR) 규제가 강화되면 대출 한도는?",
                            ),
                        ),
                        isLoading = false,
                        error = null,
                        emptyMessage = "",
                        emptyIconRes = com.finq.app.R.drawable.ic_trophy,
                        onRetry = {},
                        onToggleBookmark = {},
                        onOpenDetail = {},
                        cardEmphasis = AttemptCardEmphasis.CATEGORY,
                        showTitle = false,
                    )

                    // 전체이력 밀도 — 앞에 세우는 건 정답/오답 상태, 뒤에 카테고리.
                    // 미풀이 북마크는 행을 탭하면 상세가 아니라 풀이로 간다.
                    "history" -> com.finq.app.ui.library.LibraryListScreen(
                        title = "전체 풀이 이력",
                        subtitle = "5문제 풀어봤어요",
                        items = listOf(
                            sampleAttempt(correct = true),
                            sampleAttempt(correct = false),
                            sampleAttempt(correct = true).copy(
                                quizId = 21L,
                                question = "환율이 오르면 수입 물가는 어떻게 되는가?",
                                review = ReviewStatus(2, 7, 4, true, null),
                            ),
                            sampleAttempt(correct = false).copy(quizId = 22L, solvedAtIso = null),
                            // 미풀이(마스킹) — "아직 안 푼 문제"
                            sampleAttempt(correct = false).copy(
                                quizId = 23L,
                                selectedChoiceId = null, correctChoiceId = null,
                                explanation = "", keyword = null, solvedAtIso = null,
                            ),
                        ),
                        isLoading = false,
                        error = null,
                        emptyMessage = "",
                        emptyIconRes = com.finq.app.R.drawable.ic_tab_book,
                        onRetry = {},
                        onToggleBookmark = {},
                        onOpenDetail = {},
                        onStartQuiz = {},
                        showTitle = false,
                    )

                    // 로드 실패 — 목록과 상세가 같은 화면을 쓴다(예외 메시지 노출 없음).
                    "list_error" -> com.finq.app.ui.library.LibraryListScreen(
                        title = "오답노트",
                        subtitle = "",
                        items = emptyList(),
                        isLoading = false,
                        error = "Unable to resolve host \"yuri-hub.com\"",
                        emptyMessage = "",
                        emptyIconRes = com.finq.app.R.drawable.ic_trophy,
                        onRetry = {},
                        onToggleBookmark = {},
                        onOpenDetail = {},
                        cardEmphasis = AttemptCardEmphasis.CATEGORY,
                        showTitle = false,
                    )

                    // ── 상세 화면 5케이스 — 채점 화면과 같은 본문이어야 한다 ────────
                    // 오답: 정답 보기만 강조되고, 내가 고른 보기는 면 없이 "내 답" 라벨.
                    "detail_wrong" -> AttemptDetailScreen(
                        item = sampleAttempt(correct = false).copy(
                            article = RelatedArticle(
                                title = "한국은행, 기준금리 3.00%로 동결… \"물가 둔화 흐름 확인\"",
                                url = "https://example.com/news/1",
                                source = "연합뉴스",
                            ),
                        ),
                        detailReady = true, isLoading = false, error = null,
                        bookmarked = false,
                        onToggleBookmark = {}, onRetry = {}, onBack = {}, onArticleClick = {},
                    )

                    // 정답: 색 블록이 하나로 유지되도록 라벨이 "내 답 · 정답" 으로 합쳐진다.
                    "detail_correct" -> AttemptDetailScreen(
                        item = sampleAttempt(correct = true),
                        detailReady = true, isLoading = false, error = null,
                        bookmarked = true,
                        onToggleBookmark = {}, onRetry = {}, onBack = {}, onArticleClick = {},
                    )

                    // 졸업 항목 — 상세에서도 헤더는 카테고리 · 날짜 그대로다.
                    "detail_graduated" -> AttemptDetailScreen(
                        item = sampleAttempt(correct = false).copy(
                            review = ReviewStatus(2, 7, 4, true, null),
                        ),
                        detailReady = true, isLoading = false, error = null,
                        bookmarked = true,
                        onToggleBookmark = {}, onRetry = {}, onBack = {}, onArticleClick = {},
                    )

                    // 지연 로드 중 — 목록 요약으로 헤더만 서고 본문 자리에 스피너.
                    "detail_loading" -> AttemptDetailScreen(
                        item = sampleAttempt(correct = false).copy(
                            choices = emptyList(), explanation = "", keyword = null,
                        ),
                        detailReady = false, isLoading = true, error = null,
                        bookmarked = false,
                        onToggleBookmark = {}, onRetry = {}, onBack = {}, onArticleClick = {},
                    )

                    // 로드 실패 — 다시 시도(배경 없는 라임 글자 = 누를 수 있는 것)
                    "detail_error" -> AttemptDetailScreen(
                        item = sampleAttempt(correct = false).copy(
                            choices = emptyList(), explanation = "", keyword = null,
                        ),
                        detailReady = false, isLoading = false, error = "네트워크 오류",
                        bookmarked = false,
                        onToggleBookmark = {}, onRetry = {}, onBack = {}, onArticleClick = {},
                    )

                    // 재진입 시 결과 보기 모드 — 정답이었던 경우
                    "solved_correct" -> com.finq.app.ui.screen.SolvedQuizReviewScreen(
                        quizIndex = 1,
                        totalCount = 4,
                        quiz = sampleQuiz.copy(solved = true, correct = true),
                        isLast = false,
                        onNext = {},
                        onClose = {},
                    )

                    // 재진입 시 결과 보기 모드 — 오답이었던 경우 (오답노트 링크 노출)
                    "solved_wrong" -> com.finq.app.ui.screen.SolvedQuizReviewScreen(
                        quizIndex = 2,
                        totalCount = 4,
                        quiz = sampleQuiz.copy(solved = true, correct = false),
                        isLast = true,
                        onNext = {},
                        onClose = {},
                        onViewWrongNote = {},
                    )

                    // 보기 카드 4상태 중 기본/선택 확인
                    // 긴 지문·긴 선지 — 짧은 더미로는 타이포와 줄바꿈을 검증할 수 없다.
                    // 실제 서버 문제는 두세 줄이 예사고, 카드를 걷은 이유가 "긴 지문이
                    // 벽이 된다"였으므로 여기서 봐야 판단이 선다.
                    "quiz_long" -> QuizScreen(
                        quizIndex = 1,
                        totalCount = 5,
                        quiz = longQuiz,
                        selectedOptionId = 3L,
                        onSelectOption = {},
                        onSubmit = {},
                    )

                    "answer_long" -> QuizAnswerScreen(
                        quiz = longQuiz,
                        answer = AnswerResult(
                            quizId = longQuiz.id,
                            selectedOptionId = 3L,
                            isCorrect = false,
                            correctOptionId = 1L,
                            explanation = "기준금리가 오르면 은행이 자금을 조달하는 비용이 함께 " +
                                "올라갑니다. 은행은 그 비용을 메우기 위해 예금 금리를 올려 " +
                                "자금을 더 끌어모으고, 동시에 대출 금리도 올려 마진을 지킵니다. " +
                                "그래서 예금과 대출 금리가 같은 방향으로 움직입니다.",
                            keyword = "기준금리 — 한국은행 금융통화위원회가 정하는 정책금리로, " +
                                "시중 금리의 기준이 된다",
                            relatedArticle = RelatedArticle(
                                title = "한은, 기준금리 0.25%p 인상… 연 3.75%",
                                url = "https://example.com",
                                source = "경제일보",
                            ),
                        ),
                        isLast = false,
                        quizIndex = 1,
                        totalCount = 5,
                        onNext = {},
                        onBack = {},
                        onArticleClick = {},
                    )

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

                    // 단건 풀이 (미풀이 북마크 → 풀러 가기) — 진행도 1/1 + 헤더 안내 한 줄
                    "solo_quiz" -> QuizScreen(
                        quizIndex = 0,
                        totalCount = 1,
                        quiz = sampleQuiz,
                        selectedOptionId = 2L,
                        onSelectOption = {},
                        onSubmit = {},
                        headerNote = "북마크한 문제 — 지금 풀면 오늘 기록으로 반영돼요",
                    )

                    // 단건 풀이 채점 후 — 해설 화면, CTA "완료" (북마크 목록 복귀)
                    "solo_answer" -> QuizAnswerScreen(
                        quiz = sampleQuiz,
                        answer = AnswerResult(
                            quizId = sampleQuiz.id,
                            selectedOptionId = 1L,
                            isCorrect = true,
                            correctOptionId = 1L,
                            explanation = "기준금리가 오르면 시중 금리가 따라 올라 예금 금리도 상승합니다.",
                            keyword = "기준금리",
                            relatedArticle = RelatedArticle(
                                title = "한은, 기준금리 0.25%p 인상",
                                url = "https://example.com",
                                source = "경제일보",
                            ),
                        ),
                        isLast = true,
                        quizIndex = 0,
                        totalCount = 1,
                        onNext = {},
                        onBack = {},
                        onArticleClick = {},
                        libraryRepository = stubLibraryRepository,
                        initialBookmarked = true,
                        nextLabel = "완료",
                    )

                    // 스트릭 문구 분기: 오늘 미풀이 + streak>0 → "오늘 풀면 N+1일 연속!"
                    "home_pending" -> HomeScreen(
                        quizCount = 3,
                        streak = 7,
                        solvedToday = false,
                        maxStreak = 15,
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                        reviewCount = 3,
                        garden = sampleGarden,
                    )

                    // 하다 만 상태 — 5문제 중 1문제만 풀고 홈으로 돌아왔다.
                    // 퀴즈 카드는 "오늘의 퀴즈 / 5문제 중 1문제 풀었어요 / 이어 풀기",
                    // 물주기 카드는 "잔디 물주기 / 5개 중 2개 줬어요 / 이어서 주기"여야 한다.
                    // 남은 수만 보여주던 옛 문구("오늘의 퀴즈 4문제")는 4문제 출제로 읽혔다.
                    "home_partial" -> HomeScreen(
                        quizCount = 4,
                        todayTotal = 5,
                        todayCorrect = 1,
                        streak = 7,
                        solvedToday = false,
                        maxStreak = 15,
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                        reviewCount = 3,
                        reviewedToday = 2,
                        grownToday = 1,
                        garden = sampleGarden,
                    )

                    // 스트릭 문구 분기: 미풀이 + streak==0 → 시작 유도 문구. 정원도 빈 상태.
                    "home_zero" -> HomeScreen(
                        quizCount = 3,
                        streak = 0,
                        solvedToday = false,
                        maxStreak = 15,
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                    )

                    // 퀴즈 다 풂 + 물 줄 잔디 있음 — 완료 칩(N/M 정답) + 복습 카드 Lime CTA
                    "home_done_water" -> HomeScreen(
                        quizCount = 0,
                        streak = 8,
                        solvedToday = true,
                        maxStreak = 15,
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                        reviewCount = 4,
                        garden = sampleGarden,
                        todayTotal = 3,
                        todayCorrect = 2,
                    )

                    // 퀴즈 다 풂 + 물 줄 잔디도 없음 — 조용한 완료 + "다음 물 주기 M/d"
                    "home_done" -> HomeScreen(
                        quizCount = 0,
                        streak = 8,
                        solvedToday = true,
                        maxStreak = 15,
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                        reviewCount = 0,
                        nextReviewDate = LocalDate.now().plusDays(2),
                        garden = sampleGarden,
                        todayTotal = 3,
                        todayCorrect = 3,
                    )

                    // 언덕 스케일링: 다수(nn그루) — 실루엣 숲 밀도·언덕 높이 상승 확인
                    "home_forest_many" -> HomeScreen(
                        quizCount = 3,
                        streak = 30,
                        solvedToday = true,
                        maxStreak = 42,
                        weekLevels = listOf(2, 3, 4, 1, 3, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                        reviewCount = 2,
                        garden = manyTreesGarden,
                    )

                    // 스토어 아이콘(512x512) 추출용 — 배경+전경을 **마스크 없이** 정사각으로
                    // 꽉 채워 그린다. Play Console 아이콘은 APK 와 별개로 올려야 하고
                    // 알파·라운딩 없이 512 정사각 PNG 를 요구한다(스토어가 알아서 깎는다).
                    // 제출 후 화면 본문 시안 — 왼쪽 정렬을 어떻게 잡을지 두 안 비교.
                    //
                    // 면을 걷으면 해설·키워드 글자가 화면 패딩에서 시작하는데, 선지는
                    // 면이라 안쪽 패딩만큼 들어가 있다. 그래서 선지 → 해설로 내려올 때
                    // 글자 왼쪽 끝이 밖으로 튄다. 액센트 바를 세우면 그 들여쓰기가
                    // 되살아난다. 말로는 안 갈려서 실기기에서 본다.
                    "answer_v2" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        val tp = androidx.compose.ui.graphics.Color(0xFFF4F7FB)
                        val ts = androidx.compose.ui.graphics.Color(0xFFB8C7DA)
                        val tm = TextMutedIcon
                        val ol = androidx.compose.ui.graphics.Color(0xFF2A4A6E)
                        val g1 = androidx.compose.ui.graphics.Color(0xFF124A2E)
                        val errFaint = androidx.compose.ui.graphics.Color(0xFF4A2530)
                        val err = androidx.compose.ui.graphics.Color(0xFFFF6B6B)

                        @androidx.compose.runtime.Composable
                        fun shared() {
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .background(errFaint).padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) { Text("틀렸어요", color = err, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                            Spacer(Modifier.height(12.dp))
                            listOf("내려간다" to false, "올라간다" to true, "변하지 않는다" to false).forEach { (t, ok) ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                                        .background(if (ok) g1 else androidx.compose.ui.graphics.Color.Transparent)
                                        .border(
                                            if (ok) 2.dp else 1.dp, if (ok) Lime else ol,
                                            androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                        )
                                        .padding(14.dp),
                                ) { Text(t, color = if (ok) tp else tm) }
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        @androidx.compose.runtime.Composable
                        fun bodyText(bar: Boolean) {
                            @androidx.compose.runtime.Composable
                            fun block(barColor: androidx.compose.ui.graphics.Color?, content: @androidx.compose.runtime.Composable () -> Unit) {
                                if (bar && barColor != null) {
                                    Row(Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                                        Box(
                                            Modifier.width(3.dp).fillMaxHeight()
                                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                                                .background(barColor),
                                        )
                                        Spacer(Modifier.width(11.dp))
                                        Column { content() }
                                    }
                                } else Column { content() }
                            }
                            Spacer(Modifier.height(14.dp))
                            block(Lime) {
                                Text("해설", color = tm, style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "기준금리가 오르면 은행이 돈을 빌려오는 비용이 올라갑니다. " +
                                        "그 비용을 메우려면 예금으로 자금을 더 모아야 하므로 예금 금리도 따라 오릅니다.",
                                    color = tp, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, lineHeight = 25.sp,
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            block(ol) {
                                Text("알아두면 좋아요", color = tm, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(5.dp))
                                Text("기준금리", color = tp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(3.dp))
                                Text("한국은행이 정하는 정책금리", color = ts, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                            }
                        }

                        Text("가안 · 글자로만 (바 없음)", color = Lime)
                        Spacer(Modifier.height(10.dp))
                        shared(); bodyText(bar = false)
                        Spacer(Modifier.height(28.dp))
                        Text("나안 · 액센트 바", color = Lime)
                        Spacer(Modifier.height(10.dp))
                        shared(); bodyText(bar = true)
                        Spacer(Modifier.height(40.dp))
                    }

                    // 채점 후 선지 구조 시안 — 열어둔 문 ①②(테두리의 뜻 · 왼쪽 기준선).
                    //
                    // 지금 화면은 선지 넷이 다 테두리를 두르는데 넷 다 **누를 수 없다**.
                    // 풀이 화면에서 테두리에 "고를 수 있음"을 부여했기 때문에 더 어긋난다.
                    // C안은 면·테두리를 정답 하나로 몰아 "면 = 정답" 한 뜻만 남긴다.
                    //
                    // 세 시안을 오답 상태로 나란히 둔다. 각 시안 **뒤에 해설을 붙인 것**은
                    // ②를 눈으로 재려는 것이다 — 선지에서 해설로 내려올 때 글자 왼쪽이
                    // 튀는지를 보는 게 이 케이스의 목적이다.
                    "answer_v3" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            // 좌우 6dp — C2 의 정답 면이 화면 패딩(20) 안쪽 6 에서 시작해
                            // 내부 패딩 14 를 더하면 글자가 정확히 20dp 에 선다.
                            // 다른 블록은 각자 start=14 를 더해 같은 20dp 를 맞춘다.
                            .padding(horizontal = 6.dp, vertical = 12.dp),
                    ) {
                        val tp = androidx.compose.ui.graphics.Color(0xFFF4F7FB)
                        val ts = androidx.compose.ui.graphics.Color(0xFFB8C7DA)
                        val tm = TextMutedIcon
                        val ol = androidx.compose.ui.graphics.Color(0xFF2A4A6E)
                        val g1 = androidx.compose.ui.graphics.Color(0xFF124A2E)
                        val errFaint = androidx.compose.ui.graphics.Color(0xFF4A2530)
                        val err = androidx.compose.ui.graphics.Color(0xFFFF6B6B)
                        val onLime = androidx.compose.ui.graphics.Color(0xFF05221A)
                        val r14 = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                        val r12 = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)

                        // 선지 넷 — 2번이 정답, 3번이 내가 고른 오답.
                        val opts = listOf(
                            Triple("예금 금리가 내려간다", false, false),
                            Triple("예금 금리가 올라간다", true, false),
                            Triple("환율이 먼저 움직인다", false, true),
                            Triple("아무 변화도 없다", false, false),
                        )

                        @androidx.compose.runtime.Composable
                        fun verdict(inset: androidx.compose.ui.unit.Dp) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = inset)
                                    .clip(r12).background(errFaint)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "틀렸어요", color = err,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                )
                            }
                        }

                        // 해설 — 시안마다 같은 글, 시작 x 만 다르다. 여기가 기준선의 기준.
                        @androidx.compose.runtime.Composable
                        fun explain(inset: androidx.compose.ui.unit.Dp) {
                            Column(Modifier.fillMaxWidth().padding(start = inset, end = inset)) {
                                Text(
                                    "해설", color = tm,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "기준금리가 오르면 은행이 돈을 빌려오는 비용이 올라갑니다. " +
                                        "그 비용을 메우려면 예금으로 자금을 더 모아야 하므로 " +
                                        "예금 금리도 따라 오릅니다.",
                                    color = tp,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                    lineHeight = 25.sp,
                                )
                            }
                        }

                        @androidx.compose.runtime.Composable
                        fun heading(text: String) {
                            Spacer(Modifier.height(28.dp))
                            Text(
                                text, color = Lime,
                                modifier = Modifier.padding(start = 14.dp),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        // ── 가안 · 현행 ────────────────────────────────────────
                        // 넷 다 테두리. 번호 원은 표시된 둘만(직전 커밋 상태).
                        heading("가안 · 현행 (넷 다 테두리)")
                        verdict(inset = 14.dp)
                        Spacer(Modifier.height(14.dp))
                        opts.forEachIndexed { i, (t, ok, mine) ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                                    .clip(r14)
                                    .background(
                                        when {
                                            ok -> g1
                                            mine -> androidx.compose.ui.graphics.Color(0xFF0E2947)
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        },
                                    )
                                    .border(
                                        if (ok || mine) 2.dp else 1.dp,
                                        if (ok) Lime else if (mine) err else ol, r14,
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(28.dp).then(
                                        if (ok || mine) Modifier.clip(CircleShape)
                                            .background(if (ok) Lime else err)
                                        else Modifier,
                                    ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${i + 1}",
                                        color = if (ok) onLime else if (mine) errFaint else tm,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    t, color = if (ok || mine) tp else tm,
                                    modifier = Modifier.weight(1f),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                )
                                if (ok || mine) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (ok) "정답" else "내 답",
                                        color = if (ok) Lime else err,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        explain(inset = 14.dp)

                        // ── 나안 · C1 ─────────────────────────────────────────
                        // 면·테두리를 정답 하나로. 나머지는 번호 열만 남기고 물린다.
                        // ①은 닫히지만 글자 시작은 여전히 안쪽(≈52dp)이라 ②는 남는다.
                        heading("나안 · C1 (정답만 면, 번호 열 유지)")
                        verdict(inset = 14.dp)
                        Spacer(Modifier.height(14.dp))
                        opts.forEachIndexed { i, (t, ok, mine) ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                                    .then(
                                        if (ok) Modifier.clip(r14).background(g1)
                                            .border(2.dp, Lime, r14)
                                        else Modifier,
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(28.dp).then(
                                        if (ok || mine) Modifier.clip(CircleShape)
                                            .background(if (ok) Lime else err)
                                        else Modifier,
                                    ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${i + 1}",
                                        color = if (ok) onLime else if (mine) errFaint else tm,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    t, color = if (ok || mine) tp else tm,
                                    modifier = Modifier.weight(1f),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                )
                                if (mine) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "내 답", color = err,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        explain(inset = 14.dp)

                        // ── 다안 · C2 ─────────────────────────────────────────
                        // C1 에서 **번호를 뺀다.** 채점 후 화면에서 번호는 아무것도
                        // 참조하지 않는다(해설이 "2번"이라고 부르지 않는다). 번호가
                        // 빠지면 정답 면을 좌우 6dp 로 물려 글자가 20dp 에 서고,
                        // 선지·해설의 왼쪽이 한 선에 붙는다 — ①②가 같이 닫힌다.
                        // 표시는 색과 라벨이 맡는다.
                        heading("다안 · C2 (번호 없음 · 기준선 통일)")
                        verdict(inset = 14.dp)
                        Spacer(Modifier.height(14.dp))
                        opts.forEach { (t, ok, mine) ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .then(
                                        if (ok) Modifier.clip(r14).background(g1)
                                            .border(2.dp, Lime, r14).padding(14.dp)
                                        else Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    t, color = if (ok || mine) tp else tm,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (ok || mine)
                                        androidx.compose.ui.text.font.FontWeight.SemiBold
                                    else androidx.compose.ui.text.font.FontWeight.Normal,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                )
                                if (ok || mine) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (ok) "정답" else "내 답",
                                        color = if (ok) Lime else err,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        explain(inset = 14.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "↑ 다안에서만 선지 글자와 해설 글자의 왼쪽이 같은 선에 선다",
                            color = ts, modifier = Modifier.padding(start = 14.dp),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(40.dp))
                    }

                    // 전환 시안 — "번호가 있다가 없어져도 되나"를 재는 케이스.
                    //
                    // answer_v3 는 채점 후 화면**만** 나란히 놓아서, 풀이 화면에서
                    // 넘어오는 순간이 자연스러운지가 안 보였다. 여기선 같은 문제의
                    // **풀이 → 채점** 두 장을 붙여 세로로 잇는다. 위아래 두 묶음이
                    // 서로 다른 답을 준다.
                    //
                    //  1) C2  — 채점되면 번호가 사라진다. 대신 선지·해설 기준선이 통일.
                    //  2) C2b — 번호를 넷 다 유지하고, 대신 **해설을 선지 쪽으로 민다.**
                    //           사라지는 것이 없고 기준선도 맞지만 본문 폭을 40dp 먹는다.
                    "answer_v4" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .padding(horizontal = 6.dp, vertical = 12.dp),
                    ) {
                        val tp = androidx.compose.ui.graphics.Color(0xFFF4F7FB)
                        val ts = androidx.compose.ui.graphics.Color(0xFFB8C7DA)
                        val tm = TextMutedIcon
                        val ol = androidx.compose.ui.graphics.Color(0xFF2A4A6E)
                        val g1 = androidx.compose.ui.graphics.Color(0xFF124A2E)
                        val errFaint = androidx.compose.ui.graphics.Color(0xFF4A2530)
                        val err = androidx.compose.ui.graphics.Color(0xFFFF6B6B)
                        val onLime = androidx.compose.ui.graphics.Color(0xFF05221A)
                        val r14 = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                        val r12 = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        val opts = listOf(
                            Triple("예금 금리가 내려간다", false, false),
                            Triple("예금 금리가 올라간다", true, false),
                            Triple("환율이 먼저 움직인다", false, true),
                            Triple("아무 변화도 없다", false, false),
                        )

                        @androidx.compose.runtime.Composable
                        fun label(text: String, color: androidx.compose.ui.graphics.Color) {
                            Text(
                                text, color = color,
                                modifier = Modifier.padding(start = 14.dp),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        @androidx.compose.runtime.Composable
                        fun question() {
                            Text(
                                "Q. 기준금리가 오르면 예금 금리는 어떻게 될까요?",
                                color = tp, modifier = Modifier.padding(start = 14.dp, end = 14.dp),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(14.dp))
                        }

                        // 풀이 화면 — 넷 다 누를 수 있어 넷 다 원·테두리. 이건 안 바꾼다.
                        @androidx.compose.runtime.Composable
                        fun solving() {
                            question()
                            opts.forEachIndexed { i, (t, _, _) ->
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                                        .clip(r14).border(1.dp, ol, r14).padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier.size(28.dp).clip(CircleShape)
                                            .background(BgElevatedIcon),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${i + 1}", color = ts,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        t, color = tp, modifier = Modifier.weight(1f),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }

                        @androidx.compose.runtime.Composable
                        fun verdict() {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                                    .clip(r12).background(errFaint)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "틀렸어요", color = err,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                        }

                        /** 해설 — [inset] 만 다르다. C2 는 14(=화면 20), C2b 는 54. */
                        @androidx.compose.runtime.Composable
                        fun explain(inset: androidx.compose.ui.unit.Dp) {
                            Column(Modifier.fillMaxWidth().padding(start = inset, end = 14.dp)) {
                                Text(
                                    "해설", color = tm,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "기준금리가 오르면 은행이 돈을 빌려오는 비용이 올라갑니다. " +
                                        "그 비용을 메우려면 예금으로 자금을 더 모아야 하므로 " +
                                        "예금 금리도 따라 오릅니다.",
                                    color = tp,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                    lineHeight = 25.sp,
                                )
                            }
                        }

                        // ── 묶음 1 · C2 ──────────────────────────────────────
                        label("① 풀이 화면 (공통 · 안 바뀜)", Lime)
                        solving()
                        Spacer(Modifier.height(24.dp))
                        label("② 채점 후 · C2 — 번호가 사라진다", err)
                        question()
                        verdict()
                        opts.forEach { (t, ok, mine) ->
                            Row(
                                Modifier.fillMaxWidth().then(
                                    if (ok) Modifier.clip(r14).background(g1)
                                        .border(2.dp, Lime, r14).padding(14.dp)
                                    else Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    t, color = if (ok || mine) tp else tm,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (ok || mine)
                                        androidx.compose.ui.text.font.FontWeight.SemiBold
                                    else androidx.compose.ui.text.font.FontWeight.Normal,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                )
                                if (ok || mine) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (ok) "정답" else "내 답",
                                        color = if (ok) Lime else err,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        explain(inset = 14.dp)

                        // ── 묶음 2 · C2b ─────────────────────────────────────
                        Spacer(Modifier.height(36.dp))
                        label("① 풀이 화면 (같은 화면)", Lime)
                        solving()
                        Spacer(Modifier.height(24.dp))
                        label("② 채점 후 · C2b — 번호는 넷 다 남는다", Lime)
                        question()
                        verdict()
                        opts.forEachIndexed { i, (t, ok, mine) ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                                    .then(
                                        if (ok) Modifier.clip(r14).background(g1)
                                            .border(2.dp, Lime, r14)
                                        else Modifier,
                                    )
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 원은 표시된 둘만. 번호 **글자**는 넷 다 남아서
                                // 풀이 화면의 번호 열이 그대로 이어진다.
                                Box(
                                    Modifier.size(28.dp).then(
                                        if (ok || mine) Modifier.clip(CircleShape)
                                            .background(if (ok) Lime else err)
                                        else Modifier,
                                    ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${i + 1}",
                                        color = if (ok) onLime else if (mine) errFaint else tm,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    t, color = if (ok || mine) tp else tm,
                                    modifier = Modifier.weight(1f),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                )
                                if (mine) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "내 답", color = err,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        // 해설을 선지 글자와 같은 x 로 민다 — 번호 원 28 + 간격 12 + 14.
                        explain(inset = 54.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "↑ C2b 는 해설이 선지 글자에 맞춰 들어와 있다 (본문 폭 −40dp)",
                            color = ts, modifier = Modifier.padding(start = 14.dp, end = 14.dp),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(40.dp))
                    }

                    // 미풀이 북마크 행 표기 시안 — 제목 자리에 무엇을 세울까.
                    //
                    // 안 푼 문제는 서버가 keyword 를 마스킹한다(치팅 차단, 유지 확정).
                    // 그래서 제목이 질문 장문으로 폴백돼 한 줄로 잘리는데, 실계정에서
                    // 미풀이가 담은 순 상단에 몰리면 **목록 첫인상이 잘린 문장 셋**이 된다.
                    // 개념어 제목으로 얻은 이득을 상단에서 그대로 잃는 자리다.
                    //
                    // 네 안은 "어떻게 자르나"의 변주가 아니라 **무엇을 제목으로 세우나**가
                    // 서로 다르다. 각 안에 미풀이 둘 + 푼 항목 하나를 섞어, 목록의 리듬이
                    // 깨지는지(행 키·왼쪽 기준선)까지 같이 본다.
                    "bookmark_unsolved" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        val tp = androidx.compose.ui.graphics.Color(0xFFF4F7FB)
                        val ts = androidx.compose.ui.graphics.Color(0xFFB8C7DA)
                        val tm = TextMutedIcon
                        val ol = androidx.compose.ui.graphics.Color(0xFF2A4A6E)
                        val bold = androidx.compose.ui.text.font.FontWeight.Bold
                        val semi = androidx.compose.ui.text.font.FontWeight.SemiBold
                        val normal = androidx.compose.ui.text.font.FontWeight.Normal
                        val typo = androidx.compose.material3.MaterialTheme.typography

                        // 실계정에 실제로 들어 있는 세 항목(길이를 그대로 쓴다).
                        val q1 = "원·달러 환율이 빠르게 하락하여 원화 가치가 오를 때 수출 기업의 " +
                            "채산성에 나타나는 변화로 가장 적절한 것은?"
                        val q2 = "일본 엔화 가치가 장기적으로 약세를 보일 때, 아시아 주요 국가들이 " +
                            "동반 통화 약세 압력에 대응해 자국 통화 정책을 조정하는 주된 이유는?"

                        @androidx.compose.runtime.Composable
                        fun rowFrame(
                            date: String,
                            action: Boolean = false,
                            content: @androidx.compose.runtime.Composable () -> Unit,
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) { content() }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    date, color = tm, style = typo.labelMedium,
                                    fontWeight = normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    modifier = Modifier.width(42.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Image(
                                    painter = painterResource(R.drawable.ic_bookmark_star_filled),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                if (action) {
                                    // 라안 전용 — 이 행만 동작이 다르다(상세가 아니라 풀이로 간다)
                                    Text("풀기", color = Lime, style = typo.labelMedium, fontWeight = bold)
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.ic_chevron_right),
                                        contentDescription = null,
                                        colorFilter = ColorFilterIcon.tint(tm),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                            androidx.compose.material3.HorizontalDivider(thickness = 1.dp, color = ol)
                        }

                        /** 푼 항목 — 네 안에서 똑같다. 리듬 비교용 기준선. */
                        @androidx.compose.runtime.Composable
                        fun solvedRow() {
                            rowFrame(date = "8/7") {
                                Text("기준금리", color = tp, style = typo.bodyLarge, fontWeight = normal, maxLines = 1)
                                Spacer(Modifier.height(3.dp))
                                Row {
                                    Text("정답", color = Lime, style = typo.labelMedium, fontWeight = bold)
                                    Text("  ·  ", color = tm, style = typo.labelMedium)
                                    Text("금리", color = ts, style = typo.labelMedium)
                                }
                            }
                        }

                        @androidx.compose.runtime.Composable
                        fun metaUnsolved(category: String) {
                            Row {
                                Text("아직 안 푼 문제", color = ts, style = typo.labelMedium, fontWeight = bold)
                                Text("  ·  ", color = tm, style = typo.labelMedium)
                                Text(category, color = ts, style = typo.labelMedium)
                            }
                        }

                        @androidx.compose.runtime.Composable
                        fun heading(t: String, sub: String) {
                            Spacer(Modifier.height(26.dp))
                            Text(t, color = Lime, fontWeight = bold, style = typo.titleSmall)
                            Text(sub, color = tm, style = typo.bodySmall)
                            Spacer(Modifier.height(6.dp))
                        }

                        // ── 가안 · 현행 ─────────────────────────────────────
                        heading("가안 · 현행", "질문을 제목 자리에 한 줄로 자른다")
                        listOf(q1 to "환율", q2 to "환율").forEach { (q, c) ->
                            rowFrame(date = if (q === q1) "오늘" else "8/7") {
                                Text(
                                    q, color = ts, style = typo.bodyLarge, fontWeight = normal,
                                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(3.dp))
                                metaUnsolved(c)
                            }
                        }
                        solvedRow()

                        // ── 나안 · 질문 두 줄 ───────────────────────────────
                        // 자르지 않고 더 보여준다. 뜻은 통하지만 행이 커져 한 화면에
                        // 들어오는 개수가 줄고, 미풀이 행만 키가 달라진다.
                        heading("나안 · 질문 두 줄", "자르지 말고 더 보여준다 — 행이 커진다")
                        listOf(q1 to "환율", q2 to "환율").forEach { (q, c) ->
                            rowFrame(date = if (q === q1) "오늘" else "8/7") {
                                Text(
                                    q, color = ts, style = typo.bodyMedium, fontWeight = normal,
                                    lineHeight = 20.sp,
                                    maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(4.dp))
                                metaUnsolved(c)
                            }
                        }
                        solvedRow()

                        // ── 다안 · 카테고리를 제목으로 ──────────────────────
                        // 푼 항목의 개념어와 **같은 자리·같은 크기**에 짧은 낱말이 온다.
                        // 질문은 아래 줄로 내려 보조 정보가 된다(잘려도 제목이 아니라 부제라
                        // 뇌가 끝까지 파싱하려 들지 않는다).
                        heading("다안 · 카테고리를 제목으로", "질문은 아래 줄 보조로 — 제목 자리는 항상 짧다")
                        listOf(q1 to "환율", q2 to "환율").forEach { (q, c) ->
                            rowFrame(date = if (q === q1) "오늘" else "8/7") {
                                Text(c, color = tp, style = typo.bodyLarge, fontWeight = normal, maxLines = 1)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    q, color = tm, style = typo.bodySmall,
                                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(3.dp))
                                Text("아직 안 푼 문제", color = ts, style = typo.labelMedium, fontWeight = bold)
                            }
                        }
                        solvedRow()

                        // ── 라안 · 액션 행 ──────────────────────────────────
                        // 이 행만 **동작이 다르다**(상세가 아니라 그 문제 풀이로 간다).
                        // 질문을 아예 빼고 형태로 그 차이를 말한다 — 셰브론 대신 `풀기`.
                        heading("라안 · 액션 행", "질문을 빼고 동작이 다름을 형태로 — 셰브론 대신 `풀기`")
                        listOf("환율", "환율").forEachIndexed { i, c ->
                            rowFrame(date = if (i == 0) "오늘" else "8/7", action = true) {
                                Text(c, color = tp, style = typo.bodyLarge, fontWeight = semi, maxLines = 1)
                                Spacer(Modifier.height(3.dp))
                                Text("아직 안 푼 문제", color = ts, style = typo.labelMedium, fontWeight = bold)
                            }
                        }
                        solvedRow()
                        Spacer(Modifier.height(40.dp))
                    }

                    // 날짜 축 라벨 시안 — 같은 자리의 숫자가 탭마다 다른 뜻이다.
                    //
                    // 오답노트·전체이력의 날짜는 **푼 날**, 북마크의 날짜는 **담은 날**이다
                    // (각 화면의 서버 정렬 축과 맞춘 결과). 화면 안에서는 순서가 설명되지만,
                    // 사용자는 탭을 옮겨다니며 보므로 **숫자의 뜻이 바뀌는 걸 알 수가 없다.**
                    //
                    // 네 안은 라벨을 **어디에 두느냐**가 다르다. 각 안에 북마크 화면 상단
                    // (카운트 줄 + 필터칩)과 행 셋을 함께 그려, 그 자리가 실제로 눈에
                    // 들어오는지·날짜 열과 이어져 보이는지를 본다.
                    "bookmark_dateaxis" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        val tp = androidx.compose.ui.graphics.Color(0xFFF4F7FB)
                        val ts = androidx.compose.ui.graphics.Color(0xFFB8C7DA)
                        val tm = TextMutedIcon
                        val ol = androidx.compose.ui.graphics.Color(0xFF2A4A6E)
                        val bold = androidx.compose.ui.text.font.FontWeight.Bold
                        val normal = androidx.compose.ui.text.font.FontWeight.Normal
                        val typo = androidx.compose.material3.MaterialTheme.typography
                        val endAlign = androidx.compose.ui.text.style.TextAlign.End

                        val rows = listOf(
                            Triple("원·달러 환율이 빠르게 하락하여 원…", "오늘", true),
                            Triple("기준금리", "8/7", false),
                            Triple("소비자물가지수", "7/28", false),
                        )

                        @androidx.compose.runtime.Composable
                        fun listRows() {
                            rows.forEach { (title, date, unsolved) ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            title, color = if (unsolved) ts else tp,
                                            style = typo.bodyLarge, fontWeight = normal, maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Row {
                                            Text(
                                                if (unsolved) "아직 안 푼 문제" else "정답",
                                                color = if (unsolved) ts else Lime,
                                                style = typo.labelMedium, fontWeight = bold,
                                            )
                                            Text("  ·  ", color = tm, style = typo.labelMedium)
                                            Text(
                                                if (unsolved) "환율" else "금리",
                                                color = ts, style = typo.labelMedium,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        date, color = tm, style = typo.labelMedium, fontWeight = normal,
                                        textAlign = endAlign, modifier = Modifier.width(42.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(R.drawable.ic_bookmark_star_filled),
                                        contentDescription = null, modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.width(30.dp), contentAlignment = Alignment.CenterEnd) {
                                        if (unsolved) {
                                            Text("풀기", color = Lime, style = typo.labelMedium, fontWeight = bold)
                                        } else {
                                            Image(
                                                painter = painterResource(R.drawable.ic_chevron_right),
                                                contentDescription = null,
                                                colorFilter = ColorFilterIcon.tint(tm),
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                                androidx.compose.material3.HorizontalDivider(thickness = 1.dp, color = ol)
                            }
                        }

                        @androidx.compose.runtime.Composable
                        fun chips() {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("전체", "금리", "환율").forEachIndexed { i, c ->
                                    Box(
                                        Modifier
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                                            .background(if (i == 0) Lime else BgElevatedIcon)
                                            .padding(horizontal = 16.dp, vertical = 7.dp),
                                    ) {
                                        Text(
                                            c,
                                            color = if (i == 0) androidx.compose.ui.graphics.Color(0xFF05221A) else ts,
                                            style = typo.labelMedium, fontWeight = bold,
                                        )
                                    }
                                }
                            }
                        }

                        @androidx.compose.runtime.Composable
                        fun heading(t: String, sub: String) {
                            Spacer(Modifier.height(28.dp))
                            Text(t, color = Lime, fontWeight = bold, style = typo.titleSmall)
                            Text(sub, color = tm, style = typo.bodySmall)
                            Spacer(Modifier.height(10.dp))
                        }

                        // ── 가안 · 카운트 줄에 이어 붙인다 ────────────────────
                        heading("가안 · 카운트 줄에 붙인다", "`14문제 · 담은 날짜순` — 이미 있는 자리")
                        Text(
                            "14문제  ·  담은 날짜순",
                            color = tp, style = typo.titleMedium, fontWeight = bold,
                        )
                        Spacer(Modifier.height(10.dp)); chips(); Spacer(Modifier.height(8.dp))
                        listRows()

                        // ── 나안 · 카운트 줄 오른쪽 끝 ───────────────────────
                        // 라벨을 날짜 열과 **같은 쪽**에 세운다. 위아래로 이어져 보여
                        // "저 숫자가 그거"라는 연결이 생기는지가 이 안의 전부다.
                        heading("나안 · 카운트 줄 오른쪽 끝", "날짜 열과 같은 쪽에 세운다 — 세로로 이어 보이게")
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text("14문제", color = tp, style = typo.titleMedium, fontWeight = bold)
                            Spacer(Modifier.weight(1f))
                            Text("담은 날짜순", color = tm, style = typo.labelMedium)
                        }
                        Spacer(Modifier.height(10.dp)); chips(); Spacer(Modifier.height(8.dp))
                        listRows()

                        // ── 다안 · 열 머리글 ────────────────────────────────
                        // 목록 첫 행 **바로 위**에 날짜 열의 이름을 단다. 표의 문법이라
                        // 연결은 가장 확실한데, 이 앱 목록엔 없던 어휘라 줄이 하나 는다.
                        heading("다안 · 열 머리글", "목록 바로 위에 열 이름 — 표의 문법, 줄이 하나 는다")
                        Text("14문제", color = tp, style = typo.titleMedium, fontWeight = bold)
                        Spacer(Modifier.height(10.dp)); chips(); Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                "담은 날", color = tm, style = typo.labelSmall,
                                textAlign = endAlign, modifier = Modifier.width(42.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Spacer(Modifier.width(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Spacer(Modifier.width(30.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        listRows()

                        // ── 라안 · 라벨 없음(현행) ──────────────────────────
                        heading("라안 · 라벨 없음 (현행)", "아무 말도 안 한다 — 비교 기준")
                        Text("14문제", color = tp, style = typo.titleMedium, fontWeight = bold)
                        Spacer(Modifier.height(10.dp)); chips(); Spacer(Modifier.height(8.dp))
                        listRows()
                        Spacer(Modifier.height(40.dp))
                    }

                    // 로그인 화면 — 로그아웃하지 않고 보기 위한 케이스.
                    // 실계정 세션을 지우면 다시 로그인해야 하고, 그것 때문에 검증을
                    // 미루게 된다.
                    "login" -> LoginScreen(
                        isLoading = false,
                        error = null,
                        onKakaoLogin = {},
                        onGoogleLogin = {},
                        onClearError = {},
                    )

                    // 스토어 스크린샷 4번(홈) 전용 상태.
                    // 값을 여기 고정해 두는 이유: 실계정으로 찍으면 그날 상태에 따라
                    // 화면이 달라져 다시 찍을 때마다 다른 그림이 나온다. store-assets 의
                    // 캡션("하루 3분이면 끝나요")과 맞는 상태는 **오늘 분량을 마친 화면**이다.
                    "store_home" -> HomeScreen(
                        quizCount = 0,
                        streak = 4,
                        solvedToday = true,
                        maxStreak = 15,
                        // 오늘까지 4일 연속 — 스트릭 문구·잔디 칸·solvedToday 가 서로
                        // 어긋나지 않게 맞춘다. 스토어에 나가는 그림이라 "2일 연속"인데
                        // 오늘 칸이 비어 있는 식의 모순이 보이면 안 된다.
                        weekLevels = listOf(2, 3, 2, 4, -1, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "김잔디",
                        // 복습 카드는 **진행중** 상태로 찍는다("5개 중 1개 줬어요").
                        // reviewedToday 를 안 넘기면 잔량만 보여 "오늘 물 줄 잔디 4개"가
                        // 되는데, 그건 총량을 말하는 것처럼 읽힌다 — 분모가 보이는
                        // 진행중 상태여야 이 앱이 무엇을 세는지 한눈에 전달된다.
                        reviewCount = 4,
                        reviewedToday = 1,
                        garden = sampleGarden,
                        todayTotal = 5,
                        todayCorrect = 4,
                    )

                    // 닉네임 길이 한계 — 입력 상한이 20자다(MyPageScreen 닉네임 다이얼로그).
                    // 홈 헤더는 "경제잔디" 로고와 인사말이 한 Row 에 있고 사이가 weight
                    // Spacer 라, 인사말이 길어지면 Spacer 가 0 으로 눌린다. 그 순간
                    // 인사말이 오른쪽 정렬을 잃는다 — 짧을 때와 다른 자리에 놓인다.
                    "nickname_len" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .verticalScroll(rememberScrollState()).statusBarsPadding(),
                    ) {
                        listOf(
                            "2자" to "유리",
                            "8자" to "경제잔디마스터",
                            "20자(상한)" to "가나다라마바사아자차카타파하가나다라마바",
                        ).forEach { (label, nick) ->
                            Text(label, color = Lime, modifier = Modifier.padding(start = 20.dp, top = 12.dp))
                            Box(Modifier.fillMaxWidth().height(150.dp)) {
                                HomeScreen(
                                    quizCount = 5, streak = 2, solvedToday = true, maxStreak = 15,
                                    weekLevels = listOf(2, 3, -1, -1, -1, -1, -1),
                                    isLoading = false, error = null,
                                    onStartQuiz = {}, onRetry = {},
                                    nickname = nick, reviewCount = 4, garden = ReviewGarden.EMPTY,
                                )
                            }
                        }
                    }

                    // 알림 실물 확인 — 서버 발송을 기다리지 않고 실제 알림을 띄운다.
                    // 작은 아이콘이 상태바에서 어떻게 깎이는지, setColor 가 알림 행에서
                    // 어디를 칠하는지는 **화면으로만** 알 수 있다. 계산이나 문서로 대신하면
                    // 틀린다 — One UI 는 AOSP 와 다르게 그린다.
                    "notification" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .statusBarsPadding().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        Text("알림 띄우기 — 상태바와 알림창을 직접 볼 것", color = Lime)
                        androidx.compose.material3.Button(onClick = {
                            com.finq.app.push.FinQMessagingService.showNotification(
                                ctx,
                                "오늘의 경제 퀴즈가 도착했어요",
                                "방금 나온 경제 뉴스로 만든 5문제, 지금 풀어보세요!",
                            )
                        }) { Text("알림 띄우기") }
                        Text(
                            "누른 뒤 상태바를 내려 확인한다. 라이트/다크 모드를 바꿔 각각 볼 것 — " +
                                "작은 아이콘은 시스템이 칠하고, 알림 행의 컬러 아이콘은 런처 아이콘이다.",
                            color = TextMutedIcon,
                        )
                    }

                    "store_icon" -> Box(
                        modifier = Modifier.fillMaxSize().background(BgBase),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.size(320.dp)) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_background),
                                contentDescription = null,
                                modifier = Modifier.size(320.dp),
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = "스토어 아이콘",
                                modifier = Modifier.size(320.dp),
                            )
                        }
                    }

                    // 런처 아이콘 후보 — **실제 앱 벡터**로 런처 표시 크기에 렌더한다.
                    // Tabler 목업으로는 실제 형태(잎 각도·수관 덩어리)를 못 봐서 헛다리를 짚는다.
                    "app_icon" -> Column(
                        Modifier.fillMaxSize().background(BgBase)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding().padding(20.dp),
                    ) {
                        val candidates = listOf(
                            "새싹(현행)" to R.drawable.ic_stage_sprout,
                            "잔디" to R.drawable.ic_stage_grass,
                            "나무 직전" to R.drawable.ic_stage_almost_tree,
                            "나무" to R.drawable.ic_stage_tree,
                        )
                        // 운영 vs 디버그 — 홈화면에서 두 아이콘이 실제로 갈리는지 본다.
                        // 디버그 에셋은 파일명이 `_debug` 라 main 을 가리지 않으므로,
                        // 디버그 빌드에서도 왼쪽은 **진짜 운영 아이콘**이다.
                        Text(text = "운영 / 디버그 나란히", color = Lime, modifier = Modifier.padding(bottom = 8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp),
                        ) {
                            val pairs = listOf(
                                "운영" to (R.drawable.ic_launcher_background to R.drawable.ic_launcher_foreground),
                                // 디버그는 배경만 앰버로 갈린다 — 전경은 운영과 같은 파일.
                                "디버그" to (R.drawable.ic_launcher_background_debug to R.drawable.ic_launcher_foreground),
                            )
                            pairs.forEach { (label, res) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // 런처 실제 표시 크기(≈48dp)와 크게 본 것을 같이 둔다 —
                                    // 축소해서 뭉개지는지는 96dp 로는 절대 안 보인다.
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        listOf(96.dp, 48.dp, 32.dp).forEach { sz ->
                                            Box(modifier = Modifier.size(sz).clip(CircleShape)) {
                                                Image(painterResource(res.first), null, Modifier.size(sz))
                                                Image(painterResource(res.second), label, Modifier.size(sz))
                                            }
                                        }
                                    }
                                    Text(text = label, color = TextMutedIcon, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }

                        // 실제 런처 에셋 — 컬러/모노크롬(테마 아이콘) 둘 다.
                        Text(text = "런처 실제 에셋", color = Lime, modifier = Modifier.padding(bottom = 8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp),
                        ) {
                            listOf(96.dp, 48.dp).forEach { sz ->
                                Box(
                                    modifier = Modifier.size(sz).clip(CircleShape),
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher_background),
                                        contentDescription = null,
                                        modifier = Modifier.size(sz),
                                    )
                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher_foreground),
                                        contentDescription = "런처 아이콘",
                                        modifier = Modifier.size(sz),
                                    )
                                }
                            }
                            // 모노크롬 — 시스템이 단색으로 칠한 상태를 흉내 낸다.
                            listOf(96.dp, 48.dp).forEach { sz ->
                                Box(
                                    modifier = Modifier
                                        .size(sz)
                                        .clip(CircleShape)
                                        .background(TextMutedIcon),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher_monochrome),
                                        contentDescription = "테마 아이콘",
                                        colorFilter = ColorFilterIcon.tint(BgBase),
                                        modifier = Modifier.size(sz),
                                    )
                                }
                            }
                        }

                        listOf(120.dp, 60.dp, 36.dp).forEach { size ->
                            Text(
                                text = "${size.value.toInt()}dp",
                                color = Lime,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                candidates.forEach { (label, res) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(size)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(BgElevatedIcon, BgBase),
                                                    ),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Image(
                                                painter = painterResource(res),
                                                contentDescription = label,
                                                // 안전영역(72/108)을 꽉 채운다 — 현행은 여기에
                                                // scale 0.72 를 또 걸어 절반 이하로 줄었다.
                                                modifier = Modifier.size(size * 0.66f),
                                            )
                                        }
                                        if (size > 50.dp) {
                                            Text(
                                                text = label,
                                                color = TextMutedIcon,
                                                modifier = Modifier.padding(top = 6.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(22.dp))
                        }
                    }

                    // 맛보기 문제 — 로그인 전 첫 화면. 고르면 같은 화면에서 결과+로그인.
                    "taste" -> TasteQuizScreen(onKakaoLogin = {}, onGoogleLogin = {})

                    // 홈 1회성 피드백 배너 — 첫 실행 +3일 뒤 상태.
                    "home_feedback" -> HomeScreen(
                        quizCount = 3, streak = 7, solvedToday = true, maxStreak = 15,
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                        isLoading = false, error = null,
                        onStartQuiz = {}, onRetry = {}, nickname = "유리",
                        reviewCount = 3, garden = sampleGarden,
                        showFeedbackBanner = true,
                    )

                    // 버전 게이트 — 실서버는 min=1 이라 네트워크로는 재현되지 않는다.
                    // 다이얼로그만 직접 렌더해 문구·닫힘 경로를 확인한다.
                    "version_gate" -> {
                        HomeScreen(
                            quizCount = 3, streak = 7, solvedToday = true, maxStreak = 15,
                            weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                            isLoading = false, error = null,
                            onStartQuiz = {}, onRetry = {}, nickname = "유리",
                            reviewCount = 3, garden = sampleGarden,
                        )
                        ForcedUpdateDialog(
                            storeUrl = "https://play.google.com/store/apps/details?id=com.finq.app",
                            context = this,
                        )
                    }

                    // 공지 — 닫으면 배경(홈)만 남는다. "다시 열기" 로 닫힘 경로까지 한 화면에서.
                    "notice" -> {
                        var open by remember { mutableStateOf(true) }
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "다시 열기",
                                color = Lime,
                                modifier = Modifier.clickable { open = true },
                            )
                        }
                        if (open) {
                            NoticeDialog(
                                notice = "8월 5일 새벽 2시~4시에 점검이 있어요. 그동안은 문제를 받을 수 없어요.",
                                onDismiss = { open = false },
                            )
                        }
                    }

                    else -> HomeScreen(
                        quizCount = 3,
                        streak = 7,
                        solvedToday = true,
                        maxStreak = 15,
                        weekLevels = listOf(2, 0, 4, 1, 3, -1, -1),
                        isLoading = false,
                        error = null,
                        onStartQuiz = {},
                        onRetry = {},
                        nickname = "유리",
                        reviewCount = 3,
                        garden = sampleGarden,
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

    /** 마이페이지 기록 밴드용 — 자라는 중 5, 그중 곧 나무 1. */
    private val sampleGardenGrowing: ReviewGarden by lazy {
        ReviewGarden(
            growing = listOf(
                gardenSample(1, ReviewStage.SPROUT),
                gardenSample(2, ReviewStage.GRASS),
                gardenSample(3, ReviewStage.ALMOST_TREE),
                gardenSample(4, ReviewStage.SPROUT),
                gardenSample(5, ReviewStage.GRASS),
            ),
            graduated = emptyList(),
            graduatedTrees = 4,
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

    /**
     * 실사용에서 문제가 된 모양: 25/43(58.14%)·7/12(58.33%) 는 화면에서 **둘 다 58%** 인데
     * 서버 weakest 는 하나만 내려준다. 배너가 둘을 함께 지목하는지 본다.
     */
    private val tiedConcepts: ConceptStats by lazy {
        val cats = listOf(
            ConceptStat("INTEREST_RATE", "금리", 20, 17, 0.85f),
            ConceptStat("EXCHANGE_RATE", "환율", 43, 25, 25f / 43),
            ConceptStat("REAL_ESTATE", "부동산", 12, 7, 7f / 12),
            ConceptStat("INFLATION", "물가", 2, 0, 0f), // 표본 부족 — 최저지만 지목 제외
        )
        ConceptStats(categories = cats, weakest = cats[1])
    }

    /**
     * 임계값 경계 — 60% 미만만 빨강. 전부 기준 이상이면 최저(72%)여도 빨강도 배너도 없다.
     * "이 목록에서 최저"라는 상대 기준이었다면 72% 가 빨갰을 것.
     */
    private val allAboveBarConcepts: ConceptStats by lazy {
        val cats = listOf(
            ConceptStat("INTEREST_RATE", "금리", 20, 17, 0.85f),
            ConceptStat("EXCHANGE_RATE", "환율", 10, 6, 0.6f), // 정확히 60% — 빨강 아님
            ConceptStat("STOCK", "증시", 18, 13, 0.72f),
            ConceptStat("REAL_ESTATE", "부동산", 10, 5, 0.59f), // 59% — 빨강
        )
        ConceptStats(categories = cats, weakest = cats[3])
    }

    /** 표본이 있는 개념이 전부 기준 이상 — 지목 대신 칭찬 한 줄. */
    private val allGoodConcepts: ConceptStats by lazy {
        val cats = listOf(
            ConceptStat("INTEREST_RATE", "금리", 20, 17, 0.85f),
            ConceptStat("EXCHANGE_RATE", "환율", 15, 10, 10f / 15),
            ConceptStat("STOCK", "증시", 18, 13, 13f / 18),
        )
        ConceptStats(categories = cats, weakest = cats[1])
    }

    /**
     * 미달 넷 — 이름을 나열하면 줄이 넘치므로 **개수로** 말한다("4개 개념이 흔들려요").
     * 기준 이상(금리)이 있으므로 막대의 빨강은 남는다 — 아직 구별해주기 때문.
     */
    private val flatLowConcepts: ConceptStats by lazy {
        val cats = listOf(
            ConceptStat("INTEREST_RATE", "금리", 20, 17, 0.85f),
            ConceptStat("EXCHANGE_RATE", "환율", 10, 5, 0.5f),
            ConceptStat("STOCK", "증시", 10, 5, 0.5f),
            ConceptStat("REAL_ESTATE", "부동산", 10, 5, 0.5f),
            ConceptStat("INFLATION", "물가", 10, 5, 0.5f),
        )
        ConceptStats(categories = cats, weakest = cats[1])
    }

    /**
     * **전부 미달** — 화면이 통째로 붉어지면 빨강이 아무것도 구별해주지 못하고 질책만 남는다.
     * 이때만 경고색을 완전히 끄고 "아직 익숙해지는 중이에요" 한 줄로 받는다.
     */
    private val allLowVariedConcepts: ConceptStats by lazy {
        val cats = listOf(
            ConceptStat("INTEREST_RATE", "금리", 27, 16, 16f / 27), // 59%
            ConceptStat("EXCHANGE_RATE", "환율", 32, 17, 17f / 32), // 53%
            ConceptStat("STOCK", "증시", 22, 10, 10f / 22), // 45%
            ConceptStat("REAL_ESTATE", "부동산", 17, 7, 7f / 17), // 41%
            ConceptStat("INFLATION", "물가", 19, 9, 9f / 19), // 47%
        )
        ConceptStats(categories = cats, weakest = cats[3])
    }

    /** 홈 정원 히어로 케이스용 샘플 정원. */
    private val sampleGarden: ReviewGarden by lazy {
        ReviewGarden(
            growing = listOf(
                gardenSample(1, ReviewStage.SPROUT),
                gardenSample(2, ReviewStage.GRASS),
                gardenSample(3, ReviewStage.ALMOST_TREE),
                gardenSample(4, ReviewStage.SPROUT),
            ),
            graduated = listOf(
                gardenSample(101, ReviewStage.ALMOST_TREE).copy(graduatedAtIso = "2026-07-19T12:00:00"),
                gardenSample(102, ReviewStage.ALMOST_TREE).copy(graduatedAtIso = "2026-07-20T12:00:00"),
            ),
            graduatedTrees = 5,
        )
    }

    /** 다수 나무 케이스 — 뒷줄 실루엣 숲 + 언덕 상승 스케일링 확인용. */
    private val manyTreesGarden: ReviewGarden by lazy {
        ReviewGarden(
            growing = listOf(
                gardenSample(1, ReviewStage.SPROUT),
                gardenSample(2, ReviewStage.GRASS),
                gardenSample(3, ReviewStage.ALMOST_TREE),
            ),
            graduated = (101L..112L).map {
                gardenSample(it, ReviewStage.ALMOST_TREE)
                    .copy(graduatedAtIso = "2026-07-%02dT12:00:00".format((it - 100)))
            },
            graduatedTrees = 23,
        )
    }

    /** 정원 캔버스 케이스용 샘플 항목 — quizId·단계만 다르게. */
    private fun gardenSample(id: Long, stage: ReviewStage) = GardenItem(
        quizId = id, categoryLabel = "경제", question = "q$id", keyword = null,
        stage = stage, dueDate = null, waterCount = 2, absorbedCount = 1, graduatedAtIso = null,
    )

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

    /** 긴 지문·긴 선지 — 실제 서버 문제 길이에 가깝게. 줄바꿈과 타이포 검증용. */
    private val longQuiz = Quiz(
        id = 91L,
        category = Category.INTEREST_RATE,
        question = "한국은행 금융통화위원회가 기준금리를 0.25%p 인상했다고 발표했습니다. " +
            "이 결정이 시중은행의 예금·대출 금리와 가계 이자 부담에 미치는 영향으로 " +
            "가장 적절한 설명은 무엇일까요?",
        options = listOf(
            QuizOption(1, 1, "예금 금리와 대출 금리가 모두 올라 이자 부담이 커진다"),
            QuizOption(2, 2, "예금 금리만 오르고 대출 금리는 그대로 유지된다"),
            QuizOption(3, 3, "대출 금리만 오르고 예금 금리는 오히려 내려간다"),
            QuizOption(4, 4, "기준금리는 시중 금리와 무관하므로 아무 변화가 없다"),
        ),
    )

    /** 네트워크 없는 북마크 토글 스텁 — solo_answer 케이스에서 ⭐ 아이콘 렌더 확인용. */
    private val stubLibraryRepository = object : com.finq.app.data.repository.LibraryRepository {
        override suspend fun getAttempts() = emptyList<AttemptItem>()
        override suspend fun getWrongNotes() = emptyList<AttemptItem>()
        override suspend fun getBookmarks() = emptyList<AttemptItem>()
        override suspend fun getAttemptDetail(quizId: Long) = sampleAttempt(correct = true)
        override suspend fun addBookmark(quizId: Long) = true
        override suspend fun removeBookmark(quizId: Long) = false
    }

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
        // 서버 keyword 는 "용어 — 설명" 한 필드로 온다(Quiz.keyword, 최대 500자).
        // 예전 샘플이 단어만 담고 있어서 설명 줄이 안 보였다 → 실제 모양으로 맞춘다.
        keyword = "기준금리 — 한국은행이 정하는 정책금리. 시중 예금·대출 금리의 기준이 된다.",
        article = null,
        bookmarked = correct,
        solvedAtIso = "2026-07-08T09:00:00",
    )
}
