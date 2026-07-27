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
import com.finq.app.ui.library.AttemptItemCard
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.repository.AnswerResult
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.components.ConceptStatsCard
import com.finq.app.ui.components.garden.GardenCanvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import com.finq.app.ui.components.GrassCalendarCard
import com.finq.app.ui.components.WaterGrassCard
import com.finq.app.ui.screen.GardenScreen
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
 * screen: home | home_pending | home_zero | home_done_water | home_done | quiz | answer | solo_quiz | solo_answer | solved_correct | solved_wrong | mypage | mypage_loading | mypage_grass_error | filters | wrongnote | lazyload | grass | review | review_graduated | review_next | garden | garden_canvas | concept
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
                        categoryIconRes = ReviewStage.ALMOST_TREE.iconRes,
                        graduated = true,
                        graduatedMessage = "물 7번 준 나무가 완성됐어요 — 당신의 5번째 나무",
                        nextLabel = "복습 완료",
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
                        categoryIconRes = ReviewStage.GRASS.iconRes,
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

                    // 잔디밭 로딩 스켈레톤 (grass=null) — stale flash 수정 확인용
                    "mypage_loading" -> MyPageContent(
                        grass = null,
                        conceptStats = null,
                        nickname = "유리",
                        streak = 7,
                        maxStreak = 15,
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
                        streak = 7,
                        maxStreak = 15,
                        totalSolved = 28,
                        correctRate = 0.75f,
                        appVersion = "1.1.3",
                    )

                    "mypage" -> MyPageContent(
                        grass = sampleGrass,
                        conceptStats = sampleConcepts,
                        nickname = "유리",
                        streak = 7,
                        maxStreak = 15,
                        totalSolved = 28,
                        correctRate = 0.75f,
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
                        // 복습 나무 완성 뱃지 (graduated=true)
                        AttemptItemCard(
                            item = sampleAttempt(correct = true).copy(
                                review = ReviewStatus(stage = 2, waterCount = 7, absorbedCount = 4, graduated = true, dueDateIso = null),
                            ),
                            onToggleBookmark = {},
                        )
                        // 복습 자라는 중 뱃지 (graduated=false)
                        AttemptItemCard(
                            item = sampleAttempt(correct = false).copy(
                                review = ReviewStatus(stage = 1, waterCount = 2, absorbedCount = 1, graduated = false, dueDateIso = null),
                            ),
                            onToggleBookmark = {},
                        )
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

                    // 상세 지연 로드 — 목록 요약(선택지·해설 없음)을 펼치면 로더로 상세를 가져온다.
                    "lazyload" -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    ) {
                        // ① 성공: 요약 카드(빈 choices·빈 해설) → 펼치면 0.8초 뒤 상세 채워짐
                        AttemptItemCard(
                            item = sampleAttempt(correct = false).copy(
                                choices = emptyList(),
                                explanation = "",
                                keyword = null,
                                article = null,
                                solved = true,   // 푼 문제 → 펼침 시 지연 로드
                            ),
                            onToggleBookmark = {},
                            initialExpanded = true,
                            onLoadDetail = { id ->
                                kotlinx.coroutines.delay(800)
                                sampleAttempt(correct = false).copy(quizId = id)
                            },
                        )
                        // ② 실패: 로더가 예외 → "자세히 불러오지 못했어요 · 다시 시도"
                        AttemptItemCard(
                            item = sampleAttempt(correct = true).copy(
                                quizId = 77L,
                                choices = emptyList(),
                                explanation = "",
                                keyword = null,
                                solved = true,
                                question = "상세 로드 실패 시 재시도 UI 확인용 문제",
                            ),
                            onToggleBookmark = {},
                            initialExpanded = true,
                            onLoadDetail = {
                                kotlinx.coroutines.delay(600)
                                throw RuntimeException("네트워크 오류")
                            },
                        )
                    }

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
        keyword = "기준금리",
        article = null,
        bookmarked = correct,
        solvedAtIso = "2026-07-08T09:00:00",
    )
}
