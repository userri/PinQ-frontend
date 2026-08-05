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

    /**
     * 넷이 같은 50% — 지목 대상이 [WEAK_GROUP_MAX] 를 넘어 **배너를 숨긴다**.
     * 전반이 낮은 것이지 특정 개념이 약한 게 아니라 개념 진단으로 답할 문제가 아니다.
     * 다만 **막대의 빨강은 남는다** — 순위가 아니라 기준 미달을 뜻하므로 여전히 참이다.
     */
    private val flatLowConcepts: ConceptStats by lazy {
        val cats = listOf(
            ConceptStat("INTEREST_RATE", "금리", 10, 5, 0.5f),
            ConceptStat("EXCHANGE_RATE", "환율", 10, 5, 0.5f),
            ConceptStat("STOCK", "증시", 10, 5, 0.5f),
            ConceptStat("REAL_ESTATE", "부동산", 10, 5, 0.5f),
        )
        ConceptStats(categories = cats, weakest = cats[0])
    }

    /**
     * 다섯이 전부 기준 미달이지만 값이 제각각 — 최저(41%) 하나만 지목된다.
     * "다섯 개를 nn%, nn%, nn% … 로 나열하지 않는다"의 실제 모습.
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
