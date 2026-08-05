# 복습 나무 가시화 (review-tree-visibility) 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 백엔드 "복습 나무 가시화" 스펙(`/Users/iyr/SSAFY/PinQ-backend/docs/decisions/review-tree-visibility-frontend.md`)을 프론트에 연동한다 — 물 이력(waterCount/absorbedCount) 노출, 채점 응답 확장(기사·졸업 연출·404), 정원(garden) 화면 신설, 오답노트 복습 뱃지.

**Architecture:** 기존 복습 스택(ReviewApi → ReviewDtos → ApiReviewRepository → ReviewSessionViewModel → FinQNavigation REVIEW_GRAPH)에 필드를 기본값으로 추가해 하위 호환을 유지하고, garden 은 신규 route(`garden`) + 전용 ViewModel/Screen 으로 추가한다. 오답노트는 AttemptItem 에 nullable `review` 를 붙여 카드 뱃지만 확장한다.

**Tech Stack:** Kotlin, Jetpack Compose, Retrofit + Moshi(리플렉션), Navigation Compose. 유닛테스트 관례 없음 — 검증은 `./gradlew compileDebugKotlin`/`assembleDebug` + debug ShowcaseActivity 렌더.

## Global Constraints (브리프 원문 그대로)

1. **색은 테마 토큰만** (`ui/theme/Color.kt`). raw `Color(0x…)` 금지. 유채색은 ①Lime(포인트) ②Grass 램프(잔디 전용) ③Error(오답 전용) 3그룹뿐 — 새 색 만들지 않는다.
2. **카테고리 파싱은 `Category.fromServer(raw)`** — 스펙 예시의 "ECONOMY" 같은 미등록 값은 UNKNOWN("기타") 폴백. displayName은 서버 문자열 우선.
3. **DTO는 Moshi 리플렉션 + 어노테이션 없는 data class + 신규 필드는 기본값** (구서버 호환). 서버가 null 마스킹하는 필드는 nullable.
4. **총 나무 수는 항상 `graduatedTrees` 카운터를 신뢰** — garden의 graduated 목록 길이와 다를 수 있음(스펙 §3 주의).
5. **졸업 문제 재채점 404 → "이미 졸업한 문제" 처리** (크래시/일반 에러 금지).
6. 복습은 스트릭·정답률에 영향 없음(기존 안내 문구 유지). 잔디≠스트릭 축 분리 유지.
7. 기존 화면 호출부를 깨지 않기: 시그니처 확장은 기본값 있는 파라미터로.
8. 검증: `./gradlew assembleDebug` 통과 + **ShowcaseActivity**(`app/src/debug/java/com/finq/app/debug/ShowcaseActivity.kt`)에 신규/변경 상태 케이스 추가해 렌더 확인 가능하게. 이 레포는 유닛테스트 관례가 없음 — 순수 매퍼/분기 로직에 한해 가벼운 JUnit 테스트는 허용, UI 테스트는 Showcase 렌더로 갈음.
9. 커밋 메시지 한국어. `.idea/`, `.gitignore`, `app/build.gradle.kts` versionCode는 커밋에 포함 금지.

## 설계 판단 (확정)

- **garden 배치: (b) 마이페이지 잔디밭 카드 진입.** `GrassCalendarCard` 헤더에 이미 "🌳 키운 나무 N그루"(graduatedTrees)가 노출되어 있어 "숫자 탭 → 상세 정원" 동선이 가장 자연스럽고, 내 공부 탭 4페이지 추가(LibraryViewModel 확장)나 홈 카드 이중 액션보다 네비 변경이 최소다. route `garden` 은 bottomNavRoutes 에 넣지 않아 풀스크린으로 뜬다.
- **물 이력 노출 위치: 3곳 전부.** ① 복습 풀이 화면 headerNote 앞부분, ② 채점 후 화면 nextReviewText 뒤, ③ garden 항목 카드. (문구는 각 태스크에 확정 명시)
- **졸업 연출:** `QuizAnswerScreen` 에 `graduatedMessage: String? = null` 파라미터를 추가하고, waterCount·totalGraduatedTrees 가 있으면 `"물 N번 준 나무가 완성됐어요 — 당신의 M번째 나무"` 를, 없으면 기존 문구를 쓴다.
- **404 처리:** 채점 404 시 스낵바 안내(`"이미 졸업한 문제예요 — 복습 목록을 새로 불러올게요"`) 후 `loadReviews()` 로 목록 재동기화. 크래시/일반 에러 박스 금지.

---

## Task 1: 복습 DTO·도메인·매퍼 확장 (today 물 이력 + 채점 응답 확장 필드)

**Files:**
- Modify: `app/src/main/java/com/finq/app/data/remote/dto/ReviewDtos.kt`
- Modify: `app/src/main/java/com/finq/app/data/repository/ReviewRepository.kt`
- Modify: `app/src/main/java/com/finq/app/data/repository/ApiReviewRepository.kt`

**Interfaces:**
- Consumes: 기존 `ReviewApi`, `ArticleApiResponse`(`data/remote/dto/AnswerApiResponse.kt`: id/title/url/source/category/categoryDisplayName/publishedAt), `RelatedArticle`(`data/model/Quiz.kt`).
- Produces (후속 태스크가 의존):
  - `ReviewItem` 에 `val waterCount: Int`, `val absorbedCount: Int` (Task 2 헤더 문구가 사용)
  - `ReviewAnswer` 에 `val stage: Int`, `val waterCount: Int`, `val absorbedCount: Int`, `val totalGraduatedTrees: Int?`, `val article: RelatedArticle?` (Task 2·3 이 사용)

- [ ] **Step 1: ReviewDtos.kt 확장** — 신규 필드는 전부 기본값(구서버 호환, Global Constraint 3).

`ReviewApiResponse` 에 추가:

```kotlin
data class ReviewApiResponse(
    val quizId: Long,
    val category: String,
    val categoryDisplayName: String,
    val question: String,
    val choices: List<ReviewChoiceApiResponse>,
    val stage: Int,
    val dueDate: String?,
    /** 물 준 총 횟수 (복습 채점 총 시도 수). 구서버엔 없음 → 기본 0. */
    val waterCount: Int = 0,
    /** 그중 맞힌 횟수. waterCount ≥ absorbedCount. */
    val absorbedCount: Int = 0,
)
```

`ReviewAnswerApiResponse` 에 추가 (기존 필드 유지):

```kotlin
data class ReviewAnswerApiResponse(
    val quizId: Long,
    val correct: Boolean,
    val correctChoiceId: Long,
    val explanation: String,
    val keyword: String?,
    val graduated: Boolean,
    val nextDueDate: String?,
    /** 채점 반영 후 단계. 구서버엔 없음 → 기본 0. */
    val stage: Int = 0,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    /** 졸업 시에만 숫자, 비졸업이면 null. */
    val totalGraduatedTrees: Int? = null,
    /** 일반 채점 화면과 동일 구조의 관련 기사. 구서버엔 없음 → null. */
    val article: ArticleApiResponse? = null,
)
```

파일 상단 import 에 `ArticleApiResponse` 는 같은 패키지(`com.finq.app.data.remote.dto`)라 import 불필요.

- [ ] **Step 2: ReviewRepository.kt 도메인 확장**

`ReviewItem` (기존 필드 뒤에 기본값으로 추가 — 호출부 안 깨짐):

```kotlin
data class ReviewItem(
    val quizId: Long,
    val categoryLabel: String,
    val question: String,
    val options: List<QuizOption>,
    val stage: ReviewStage,
    val dueDate: LocalDate?,
    /** 물 준 총 횟수. */
    val waterCount: Int = 0,
    /** 흡수(정답) 횟수. */
    val absorbedCount: Int = 0,
)
```

`ReviewAnswer`:

```kotlin
data class ReviewAnswer(
    val quizId: Long,
    val isCorrect: Boolean,
    val correctOptionId: Long,
    val explanation: String,
    val keyword: String?,
    val graduated: Boolean,
    val nextDueDate: LocalDate?,
    val stage: Int = 0,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    /** 졸업 시에만 값. "당신의 N번째 나무" 연출용. */
    val totalGraduatedTrees: Int? = null,
    /** 채점 후 관련 기사. 구서버/기사 없음이면 null. */
    val article: com.finq.app.data.model.RelatedArticle? = null,
)
```

(파일 상단에 `import com.finq.app.data.model.RelatedArticle` 를 추가하고 타입은 `RelatedArticle?` 로 짧게 써도 된다.)

- [ ] **Step 3: ApiReviewRepository.kt 매퍼 반영**

`ReviewApiResponse.toDomain()` 끝에 추가:

```kotlin
    waterCount = waterCount,
    absorbedCount = absorbedCount,
```

`ReviewAnswerApiResponse.toDomain()` 끝에 추가:

```kotlin
    stage = stage,
    waterCount = waterCount,
    absorbedCount = absorbedCount,
    totalGraduatedTrees = totalGraduatedTrees,
    article = article?.let {
        RelatedArticle(
            title = it.title,
            url = it.url,
            source = it.source,
            id = it.id,
            category = it.category,
            categoryDisplayName = it.categoryDisplayName,
            publishedAt = it.publishedAt,
        )
    },
```

import 추가: `com.finq.app.data.model.RelatedArticle`, `com.finq.app.data.remote.dto` 의 `ArticleApiResponse` 는 이미 같은 참조 경로.

- [ ] **Step 4: 컴파일 검증**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/finq/app/data/remote/dto/ReviewDtos.kt app/src/main/java/com/finq/app/data/repository/ReviewRepository.kt app/src/main/java/com/finq/app/data/repository/ApiReviewRepository.kt
git commit -m "feat: 복습 DTO·도메인에 물 이력(waterCount/absorbedCount)·졸업 연출 필드 추가"
```

---

## Task 2: 채점 응답 연동 — 기사 표시 + 물 이력 문구 + 졸업 404 처리

**Files:**
- Modify: `app/src/main/java/com/finq/app/ui/review/ReviewAdapters.kt`
- Modify: `app/src/main/java/com/finq/app/ui/review/ReviewSessionViewModel.kt`
- Modify: `app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt` (REVIEW_QUIZ / REVIEW_ANSWER composable)

**Interfaces:**
- Consumes: Task 1 의 `ReviewItem.waterCount/absorbedCount`, `ReviewAnswer.article/waterCount/absorbedCount`.
- Produces: `ReviewSessionUiState.notice: String?` + `ReviewSessionViewModel.clearNotice()` (이 태스크 안에서만 사용되지만 Task 6 검증 대상).

- [ ] **Step 1: ReviewAdapters.kt — 기사 전달**

`toAnswerResult` 를 다음으로 교체 (KDoc 의 "복습에는 관련 기사가 없으므로" 문장도 함께 갱신):

```kotlin
/**
 * 복습 채점 결과를 기존 정답 화면이 먹는 [AnswerResult] 로 변환한다.
 * 서버가 기사를 내려주면 그대로 노출하고, 없으면(구서버 포함) [RelatedArticle.EMPTY]
 * — 정답 화면이 기사 섹션을 자동으로 숨긴다.
 */
fun ReviewAnswer.toAnswerResult(selectedOptionId: Long): AnswerResult = AnswerResult(
    quizId = quizId,
    selectedOptionId = selectedOptionId,
    isCorrect = isCorrect,
    correctOptionId = correctOptionId,
    explanation = explanation,
    keyword = keyword,
    relatedArticle = article ?: RelatedArticle.EMPTY,
)
```

- [ ] **Step 2: ReviewSessionViewModel — notice 필드 + 404 처리**

`ReviewSessionUiState` 에 필드 추가:

```kotlin
    /** 일회성 안내(스낵바용). 예: 이미 졸업한 문제 404. */
    val notice: String? = null,
```

`submitAnswer()` 의 `.onFailure` 블록을 다음으로 교체:

```kotlin
.onFailure { e ->
    if (e is retrofit2.HttpException && e.code() == 404) {
        // 이미 졸업한 문제(캐시된 화면에서 낡은 요청) — 목록을 재동기화한다.
        _uiState.update {
            it.copy(
                isSubmitting = false,
                notice = "이미 졸업한 문제예요 — 복습 목록을 새로 불러올게요",
            )
        }
        loadReviews()
    } else {
        _uiState.update {
            it.copy(isSubmitting = false, error = e.message ?: "채점에 실패했어요")
        }
    }
}
```

클래스에 메서드 추가:

```kotlin
    fun clearNotice() {
        _uiState.update { it.copy(notice = null) }
    }
```

(파일 상단 import 는 `retrofit2.HttpException` 를 추가하고 본문에서 `is HttpException` 로 써도 된다.)

- [ ] **Step 3: FinQNavigation.kt — REVIEW_QUIZ 에 notice 스낵바 + 물 이력 헤더 문구**

`REVIEW_QUIZ` composable 안, `BackHandler` 아래에 추가 (`snackbarHostState` 는 `FinQNavHost` 스코프에 이미 있음):

```kotlin
LaunchedEffect(state.notice) {
    state.notice?.let {
        snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        vm.clearNotice()
    }
}
```

`QuizScreen(...)` 호출의 `headerNote` 를 다음으로 교체 — 물 이력을 앞에 붙이되 기존 문구는 유지(Global Constraint 6):

```kotlin
headerNote = buildString {
    if (item.waterCount > 0) append("💧 물 ${item.waterCount}번 · 흡수 ${item.absorbedCount}번 · ")
    if (item.stage.isFinalStage) append("한 번 더 맞히면 나무가 돼요 · ")
    append("복습은 기록에 영향 없어요")
},
```

- [ ] **Step 4: FinQNavigation.kt — REVIEW_ANSWER 에 물 이력 + 기사 클릭**

`REVIEW_ANSWER` composable 의 `QuizAnswerScreen(...)` 에서:

`nextReviewText` 를 다음으로 교체:

```kotlin
nextReviewText = answer.nextDueDate?.let {
    "다음 물 주기: ${it.format(reviewDueDateFormat)} · 💧 물 ${answer.waterCount}번 · 흡수 ${answer.absorbedCount}번"
},
```

`onArticleClick = {}` 를 일반 세션(AnswerRoute)과 동일한 브라우저 열기로 교체. composable 상단에 `val localContext = LocalContext.current` 를 두고:

```kotlin
onArticleClick = { article ->
    val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
    try {
        localContext.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(localContext, "기사를 열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show()
    }
},
```

(필요 import — `android.content.Intent`, `android.content.ActivityNotFoundException`, `android.widget.Toast`, `androidx.core.net.toUri` — 는 파일 상단에 이미 전부 있다.)

- [ ] **Step 5: 컴파일 검증**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add app/src/main/java/com/finq/app/ui/review/ReviewAdapters.kt app/src/main/java/com/finq/app/ui/review/ReviewSessionViewModel.kt app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt
git commit -m "feat: 복습 채점에 관련 기사·물 이력 노출 + 졸업 404를 목록 재동기화로 처리"
```

---

## Task 3: 졸업 연출 강화 — "물 N번 준 나무가 완성됐어요 — 당신의 M번째 나무"

**Files:**
- Modify: `app/src/main/java/com/finq/app/ui/screen/QuizAnswerScreen.kt`
- Modify: `app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt` (REVIEW_ANSWER composable)

**Interfaces:**
- Consumes: Task 1 의 `ReviewAnswer.waterCount/totalGraduatedTrees`.
- Produces: `QuizAnswerScreen` 신규 파라미터 `graduatedMessage: String? = null` (Task 6 Showcase 가 사용).

- [ ] **Step 1: QuizAnswerScreen 파라미터 추가**

시그니처의 `graduated: Boolean = false` 바로 아래에 파라미터 추가:

```kotlin
    /** 졸업 배너 문구 override. null 이면 기본 "이 문제를 완전히 익혔어요! 나무가 됐어요". */
    graduatedMessage: String? = null,
```

본문의 `GraduatedBanner()` 호출(약 195행)을 `GraduatedBanner(message = graduatedMessage)` 로 바꾸고, private 컴포저블을:

```kotlin
@Composable
private fun GraduatedBanner(message: String? = null) {
```

로 확장한 뒤, 내부의 고정 텍스트 `"이 문제를 완전히 익혔어요! 나무가 됐어요"` 를 `message ?: "이 문제를 완전히 익혔어요! 나무가 됐어요"` 로 교체한다. 색·레이아웃(Grass1 배경, 🌳)은 그대로.

- [ ] **Step 2: FinQNavigation REVIEW_ANSWER 에서 문구 조립**

`QuizAnswerScreen(...)` 호출에 파라미터 추가:

```kotlin
graduatedMessage = if (answer.graduated && answer.totalGraduatedTrees != null)
    "물 ${answer.waterCount}번 준 나무가 완성됐어요 — 당신의 ${answer.totalGraduatedTrees}번째 나무"
else null,
```

(구서버는 totalGraduatedTrees 가 null → 기본 문구로 폴백. Global Constraint 3·7 충족.)

- [ ] **Step 3: 컴파일 검증**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add app/src/main/java/com/finq/app/ui/screen/QuizAnswerScreen.kt app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt
git commit -m "feat: 졸업 배너에 물 횟수·N번째 나무 연출 추가"
```

---

## Task 4: 정원(garden) — 데이터층 + 화면 + 마이페이지 진입

**Files:**
- Modify: `app/src/main/java/com/finq/app/data/remote/ReviewApi.kt`
- Modify: `app/src/main/java/com/finq/app/data/remote/dto/ReviewDtos.kt`
- Modify: `app/src/main/java/com/finq/app/data/repository/ReviewRepository.kt`
- Modify: `app/src/main/java/com/finq/app/data/repository/ApiReviewRepository.kt`
- Create: `app/src/main/java/com/finq/app/ui/garden/GardenViewModel.kt`
- Create: `app/src/main/java/com/finq/app/ui/screen/GardenScreen.kt`
- Modify: `app/src/main/java/com/finq/app/ui/components/GrassCalendarCard.kt`
- Modify: `app/src/main/java/com/finq/app/ui/screen/MyPageScreen.kt`
- Modify: `app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt`

**Interfaces:**
- Consumes: Task 1 의 `ReviewStage`(SPROUT🌱/GRASS🌿/ALMOST_TREE🪴, `ReviewStage.of(stage)`), `NetworkModule.reviewApi`, 테마 토큰(`BgSurface/BgSubtle/Outline/Lime/TextPrimary/TextSecondary/TextMuted/Grass1`).
- Produces:
  - `ReviewApi.getGarden(): GardenApiResponse` (`@GET("api/reviews/garden")`)
  - 도메인 `ReviewGarden(growing: List<GardenItem>, graduated: List<GardenItem>, graduatedTrees: Int)`
  - `GardenItem(quizId: Long, categoryLabel: String, question: String, keyword: String?, stage: ReviewStage, dueDate: LocalDate?, waterCount: Int, absorbedCount: Int, graduatedAtIso: String?)`
  - `ReviewRepository.getGarden(): ReviewGarden`
  - route `FinQRoutes.GARDEN = "garden"`
  - `GardenScreen(garden: ReviewGarden?, isLoading: Boolean, error: String?, onRetry: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier)` (Task 6 Showcase 가 사용)
  - `GrassCalendarCard` 신규 파라미터 `onTreesClick: (() -> Unit)? = null`
  - `MyPageScreen`(및 내부 `MyPageContent`) 신규 파라미터 `onOpenGarden: () -> Unit = {}`

- [ ] **Step 1: ReviewDtos.kt 에 garden DTO 추가** (파일 끝)

```kotlin
/** `GET /api/reviews/garden` 응답 — 복습 나무 현황. */
data class GardenApiResponse(
    val growing: List<GardenItemApiResponse> = emptyList(),
    val graduated: List<GardenItemApiResponse> = emptyList(),
    /**
     * 나무 총계 카운터. 기능 배포 이전 졸업분은 graduated 목록에 없으므로
     * "총 몇 그루"는 항상 이 값을 신뢰한다 (graduated.size 와 다를 수 있음).
     */
    val graduatedTrees: Int = 0,
)

data class GardenItemApiResponse(
    val quizId: Long,
    val category: String,
    val categoryDisplayName: String,
    val question: String,
    val keyword: String? = null,
    val stage: Int,
    val dueDate: String? = null,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    val graduatedAt: String? = null,
)
```

- [ ] **Step 2: ReviewApi.kt 에 엔드포인트 추가**

```kotlin
    @GET("api/reviews/garden")
    suspend fun getGarden(): GardenApiResponse
```

(import `com.finq.app.data.remote.dto.GardenApiResponse` 추가.)

- [ ] **Step 3: ReviewRepository.kt 도메인 + 인터페이스**

파일 끝에 추가:

```kotlin
/** 정원의 나무/새싹 한 그루. */
data class GardenItem(
    val quizId: Long,
    /** 서버가 준 한글 라벨. */
    val categoryLabel: String,
    val question: String,
    val keyword: String?,
    val stage: ReviewStage,
    val dueDate: LocalDate?,
    val waterCount: Int,
    val absorbedCount: Int,
    /** 졸업 시각 ISO-8601. 자라는 중이면 null. */
    val graduatedAtIso: String?,
)

/**
 * 정원 전체. [graduatedTrees] 는 카운터가 진실 —
 * 배포 이전 졸업분은 [graduated] 목록에 없어 목록 길이와 다를 수 있다.
 */
data class ReviewGarden(
    val growing: List<GardenItem>,
    val graduated: List<GardenItem>,
    val graduatedTrees: Int,
) {
    companion object {
        val EMPTY = ReviewGarden(growing = emptyList(), graduated = emptyList(), graduatedTrees = 0)
    }
}
```

`interface ReviewRepository` 에 추가:

```kotlin
    suspend fun getGarden(): ReviewGarden
```

- [ ] **Step 4: ApiReviewRepository.kt 구현 + 매퍼**

클래스에 추가:

```kotlin
    override suspend fun getGarden(): ReviewGarden = api.getGarden().toDomain()
```

파일의 매퍼 구역에 추가:

```kotlin
private fun GardenApiResponse.toDomain(): ReviewGarden = ReviewGarden(
    growing = growing.map { it.toDomain() },
    graduated = graduated.map { it.toDomain() },
    graduatedTrees = graduatedTrees,
)

private fun GardenItemApiResponse.toDomain(): GardenItem = GardenItem(
    quizId = quizId,
    categoryLabel = categoryDisplayName,
    question = question,
    keyword = keyword,
    stage = ReviewStage.of(stage),
    dueDate = dueDate?.let(::parseDate),
    waterCount = waterCount,
    absorbedCount = absorbedCount,
    graduatedAtIso = graduatedAt,
)
```

(import `GardenApiResponse`, `GardenItemApiResponse` 추가. categoryLabel 은 서버 displayName 우선 — Global Constraint 2 의 "displayName은 서버 문자열 우선" 그대로. enum 파싱은 화면에서 안 쓰므로 생략.)

- [ ] **Step 5: GardenViewModel.kt 생성**

```kotlin
package com.finq.app.ui.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GardenUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val garden: ReviewGarden? = null,
)

class GardenViewModel(
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { reviewRepository.getGarden() }
                .onSuccess { garden ->
                    _uiState.update { it.copy(isLoading = false, garden = garden) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "정원을 불러오지 못했어요")
                    }
                }
        }
    }

    companion object {
        fun factory(reviewRepository: ReviewRepository) = viewModelFactory {
            initializer { GardenViewModel(reviewRepository) }
        }
    }
}
```

- [ ] **Step 6: GardenScreen.kt 생성** — 확정 문구를 그대로 쓸 것.

```kotlin
package com.finq.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val GARDEN_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d")

/**
 * 정원 — 자라는 복습(새싹/풀/나무 직전)과 완성된 나무 현황.
 *
 * "총 몇 그루"는 항상 [ReviewGarden.graduatedTrees] 카운터를 쓴다 —
 * 기능 배포 이전 졸업분은 graduated 목록에 없어 목록 길이와 다를 수 있다.
 */
@Composable
fun GardenScreen(
    garden: ReviewGarden?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase),
    ) {
        // ── 상단 바 ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(12.dp),
            )
            Text(
                text = "정원",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Lime)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRetry) { Text("다시 시도") }
                }
            }
            garden != null -> GardenContent(garden)
        }
    }
}

@Composable
private fun GardenContent(garden: ReviewGarden) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── 헤더: 나무 총계 (카운터가 진실) ──────────────────────
        item {
            Column {
                Text(
                    text = "🌳 키운 나무 ${garden.graduatedTrees}그루",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                )
                if (garden.graduatedTrees > garden.graduated.size) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (garden.graduated.isEmpty())
                            "예전에 완성한 나무는 목록에 나오지 않아요"
                        else
                            "그중 ${garden.graduated.size}그루는 아래에서 자세히 볼 수 있어요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── 빈 정원 ──────────────────────────────────────────────
        if (garden.growing.isEmpty() && garden.graduated.isEmpty() && garden.graduatedTrees == 0) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "🌱", fontSize = 44.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "아직 심은 나무가 없어요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "오답을 복습하면 나무가 자라요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        }

        // ── 자라는 중 (due 오름차순 — 서버 정렬 그대로) ──────────
        if (garden.growing.isNotEmpty()) {
            item { SectionTitle("자라는 중 ${garden.growing.size}") }
            items(garden.growing, key = { "g${it.quizId}" }) { item ->
                GardenItemCard(item = item, graduated = false)
            }
        }

        // ── 완성된 나무 (졸업 최신순 — 서버 정렬 그대로) ─────────
        if (garden.graduated.isNotEmpty()) {
            item { SectionTitle("완성된 나무 ${garden.graduated.size}") }
            items(garden.graduated, key = { "d${it.quizId}" }) { item ->
                GardenItemCard(item = item, graduated = true)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
    )
}

@Composable
private fun GardenItemCard(item: GardenItem, graduated: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (graduated) "🌳" else item.stage.emoji,
                fontSize = 24.sp,
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.keyword?.takeIf { it.isNotBlank() } ?: item.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.categoryLabel} · 💧 물 ${item.waterCount}번 · 흡수 ${item.absorbedCount}번",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = if (graduated) {
                    formatGraduatedDate(item.graduatedAtIso)?.let { "$it 완성" } ?: "완성"
                } else {
                    item.dueDate?.let { "물 주기 ${it.format(GARDEN_DATE_FORMAT)}" }
                        ?: item.stage.label
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (graduated) Lime else TextSecondary,
            )
        }
    }
}

/** "2026-07-19T14:32:00" → "7/19". 파싱 실패 시 null. */
private fun formatGraduatedDate(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(GARDEN_DATE_FORMAT)
    }.getOrNull()
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun GardenScreenPreview() {
    FinQTheme {
        GardenScreen(
            garden = ReviewGarden(
                growing = listOf(
                    GardenItem(
                        quizId = 101, categoryLabel = "주식", question = "PER이 낮다는 것은?",
                        keyword = "PER", stage = ReviewStage.GRASS,
                        dueDate = LocalDate.of(2026, 7, 24),
                        waterCount = 2, absorbedCount = 1, graduatedAtIso = null,
                    ),
                ),
                graduated = listOf(
                    GardenItem(
                        quizId = 88, categoryLabel = "경제", question = "기준금리 인상의 효과는?",
                        keyword = "기준금리", stage = ReviewStage.ALMOST_TREE,
                        dueDate = null, waterCount = 5, absorbedCount = 4,
                        graduatedAtIso = "2026-07-19T14:32:00",
                    ),
                ),
                graduatedTrees = 12,
            ),
            isLoading = false,
            error = null,
            onRetry = {},
            onBack = {},
        )
    }
}
```

- [ ] **Step 7: GrassCalendarCard 에 진입점 추가**

`GrassCalendarCard` 컴포저블 시그니처에 기본값 파라미터 추가:

```kotlin
    /** "🌳 키운 나무 N그루" 헤더 탭 → 정원 화면. null 이면 탭 불가(기존 동작). */
    onTreesClick: (() -> Unit)? = null,
```

헤더의 `"🌳 키운 나무 ${grass.graduatedTrees}그루"` Text(107행 부근)에 다음 modifier 를 적용한다 (기존 modifier 가 있으면 뒤에 체이닝):

```kotlin
modifier = Modifier.then(
    if (onTreesClick != null) Modifier.clickable(onClick = onTreesClick) else Modifier
),
```

그리고 onTreesClick 이 있을 때 텍스트 뒤에 ` →` 를 붙인다:

```kotlin
text = "🌳 키운 나무 ${grass.graduatedTrees}그루" + if (onTreesClick != null) " →" else "",
```

(import `androidx.compose.foundation.clickable` 필요 시 추가.)

- [ ] **Step 8: MyPageScreen 관통**

`MyPageScreen`(그리고 실제 GrassCalendarCard 를 그리는 내부 `MyPageContent` — 파일 내 실제 구조를 열어 GrassCalendarCard 호출부를 찾아서)에 기본값 파라미터를 추가하고 전달한다:

```kotlin
    onOpenGarden: () -> Unit = {},
```

GrassCalendarCard 호출부에 `onTreesClick = onOpenGarden,` 추가. (기본값 파라미터라 ShowcaseActivity 의 기존 `MyPageContent` 호출은 안 깨진다 — Global Constraint 7.)

- [ ] **Step 9: FinQNavigation.kt 라우팅**

`FinQRoutes` 에 추가:

```kotlin
    /** 복습 나무 정원 (마이페이지 잔디 카드에서 진입). */
    const val GARDEN = "garden"
```

MY_PAGE composable 의 `MyPageScreen(...)` 에 추가:

```kotlin
onOpenGarden = { navController.navigate(FinQRoutes.GARDEN) },
```

ATTEMPT_HISTORY composable 아래에 추가:

```kotlin
// ── 정원 (복습 나무 현황) ─────────────────────────────────
composable(FinQRoutes.GARDEN) {
    val gardenVm: com.finq.app.ui.garden.GardenViewModel = viewModel(
        factory = com.finq.app.ui.garden.GardenViewModel.factory(reviewRepository),
    )
    val state by gardenVm.uiState.collectAsState()
    GardenScreen(
        garden = state.garden,
        isLoading = state.isLoading,
        error = state.error,
        onRetry = gardenVm::load,
        onBack = { navController.popBackStack() },
    )
}
```

(import 는 `com.finq.app.ui.garden.GardenViewModel`, `com.finq.app.ui.screen.GardenScreen` 를 상단에 정식으로 추가해도 된다. GARDEN 은 `bottomNavRoutes`/`darkSessionRoutes` 에 넣지 않는다 — 하단바 없는 기본 배경 풀스크린. GardenScreen 자체가 BgBase 를 깐다.)

- [ ] **Step 10: 컴파일 검증**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: 커밋**

```bash
git add app/src/main/java/com/finq/app/data/remote/ReviewApi.kt app/src/main/java/com/finq/app/data/remote/dto/ReviewDtos.kt app/src/main/java/com/finq/app/data/repository/ReviewRepository.kt app/src/main/java/com/finq/app/data/repository/ApiReviewRepository.kt app/src/main/java/com/finq/app/ui/garden/GardenViewModel.kt app/src/main/java/com/finq/app/ui/screen/GardenScreen.kt app/src/main/java/com/finq/app/ui/components/GrassCalendarCard.kt app/src/main/java/com/finq/app/ui/screen/MyPageScreen.kt app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt
git commit -m "feat: 복습 나무 정원 화면 신설 — 마이페이지 나무 카운터에서 진입"
```

---

## Task 5: 오답노트/이력 복습 뱃지 — nullable `review` 연동

**Files:**
- Modify: `app/src/main/java/com/finq/app/data/remote/dto/AttemptItemApiResponse.kt`
- Modify: `app/src/main/java/com/finq/app/data/model/AttemptItem.kt`
- Modify: `app/src/main/java/com/finq/app/data/repository/ApiLibraryRepository.kt`
- Modify: `app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt`

**Interfaces:**
- Consumes: `ReviewStage.of(stage)` (`data/repository/ReviewRepository.kt`).
- Produces: `AttemptItem.review: ReviewStatus?` — `ReviewStatus(stage: Int, waterCount: Int, absorbedCount: Int, graduated: Boolean, dueDateIso: String?)` (Task 6 Showcase 가 사용).

- [ ] **Step 1: DTO 추가** — `AttemptItemApiResponse.kt`

`AttemptItemApiResponse` 마지막 필드 뒤에 추가:

```kotlin
    /** 복습(물 주기) 상태. 복습 큐에 오른 적 없는 문제면 null. 구서버도 null. */
    val review: ReviewStatusApi? = null,
```

파일 끝에 추가:

```kotlin
data class ReviewStatusApi(
    val stage: Int = 0,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    val graduated: Boolean = false,
    val dueDate: String? = null,
)
```

- [ ] **Step 2: 도메인 추가** — `AttemptItem.kt`

`AttemptItem` 마지막 필드 뒤에 추가:

```kotlin
    /** 복습(물 주기) 상태. null 이면 한 번도 복습 큐에 오른 적 없음. */
    val review: ReviewStatus? = null,
```

파일 끝에 추가:

```kotlin
/**
 * 오답노트/이력 항목의 복습 상태 요약.
 * [graduated] 면 다 키운 나무(🌳) — 복습 큐에 다시 나오지 않는다.
 */
data class ReviewStatus(
    val stage: Int,
    val waterCount: Int,
    val absorbedCount: Int,
    val graduated: Boolean,
    val dueDateIso: String?,
)
```

- [ ] **Step 3: 매퍼** — `ApiLibraryRepository.kt` 의 `AttemptItemApiResponse.toDomain()` 끝에 추가:

```kotlin
        review = review?.let {
            ReviewStatus(
                stage = it.stage,
                waterCount = it.waterCount,
                absorbedCount = it.absorbedCount,
                graduated = it.graduated,
                dueDateIso = it.dueDate,
            )
        },
```

(import: `com.finq.app.data.model.ReviewStatus`.)

- [ ] **Step 4: 카드 뱃지** — `AttemptItemCard.kt`

상단 행의 뱃지 영역에서, 기존 `else if (!item.correct) { … "오답" … }` 블록 **뒤에** 복습 뱃지를 추가한다 (`dateStr` 표시 앞):

```kotlin
// 복습(물 주기) 상태 뱃지 — 중립 톤, 다 키운 나무만 Lime 포인트.
item.review?.let { review ->
    Surface(
        shape = RoundedCornerShape(50),
        color = BgSubtle,
    ) {
        Text(
            text = if (review.graduated) "🌳 나무 완성"
                   else "${ReviewStage.of(review.stage).emoji} 물 ${review.waterCount}번",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (review.graduated) Lime else TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
```

(import 추가: `com.finq.app.data.repository.ReviewStage`. `BgSubtle`/`Lime`/`TextSecondary` 는 이미 import 되어 있음. review == null 이면 뱃지 자체를 그리지 않는다.)

- [ ] **Step 5: 컴파일 검증**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add app/src/main/java/com/finq/app/data/remote/dto/AttemptItemApiResponse.kt app/src/main/java/com/finq/app/data/model/AttemptItem.kt app/src/main/java/com/finq/app/data/repository/ApiLibraryRepository.kt app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt
git commit -m "feat: 오답노트·이력 카드에 복습 나무 상태 뱃지 추가"
```

---

## Task 6: ShowcaseActivity 케이스 추가 + 최종 검증

**Files:**
- Modify: `app/src/debug/java/com/finq/app/debug/ShowcaseActivity.kt`

**Interfaces:**
- Consumes: Task 3 `QuizAnswerScreen.graduatedMessage`, Task 4 `GardenScreen`/`ReviewGarden`/`GardenItem`, Task 5 `AttemptItem.review`/`ReviewStatus`.

- [ ] **Step 1: screen 목록 주석 갱신**

KDoc 의 screen 나열에 `garden` 추가:

```
 * screen: home | home_pending | home_zero | quiz | answer | solved_correct | solved_wrong | mypage | mypage_loading | mypage_grass_error | wrongnote | grass | review | review_graduated | review_next | garden | concept
```

- [ ] **Step 2: "review_graduated" 케이스에 신규 연출 반영**

기존 `"review_graduated"` 분기의 `QuizAnswerScreen(...)` 에 파라미터 추가:

```kotlin
graduatedMessage = "물 7번 준 나무가 완성됐어요 — 당신의 5번째 나무",
```

- [ ] **Step 3: "garden" 케이스 추가** — `when (screen)` 에 분기 추가:

```kotlin
// 정원 — 자라는 중 + 완성 나무 + 카운터 불일치(배포 이전 졸업분) 케이스
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
)
```

import 추가: `com.finq.app.ui.screen.GardenScreen`, `com.finq.app.data.repository.GardenItem`, `com.finq.app.data.repository.ReviewGarden`, `com.finq.app.data.repository.ReviewStage`.

- [ ] **Step 4: "wrongnote" 케이스의 샘플 AttemptItem 에 복습 뱃지 상태 추가**

기존 `"wrongnote"` 분기(파일 내 샘플 `AttemptItem` 생성부)에서 항목 2개에 `review` 를 각각 추가한다 — 하나는 자라는 중, 하나는 나무 완성:

```kotlin
review = ReviewStatus(stage = 1, waterCount = 2, absorbedCount = 1, graduated = false, dueDateIso = null),
```

```kotlin
review = ReviewStatus(stage = 2, waterCount = 7, absorbedCount = 4, graduated = true, dueDateIso = null),
```

import 추가: `com.finq.app.data.model.ReviewStatus`.

- [ ] **Step 5: 빌드 최종 검증**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: (기기 연결 시) Showcase 렌더 확인**

```bash
adb shell am start -n com.finq.app/com.finq.app.debug.ShowcaseActivity --es screen garden
adb shell am start -n com.finq.app/com.finq.app.debug.ShowcaseActivity --es screen review_graduated
adb shell am start -n com.finq.app/com.finq.app.debug.ShowcaseActivity --es screen wrongnote
```

Expected: garden — "🌳 키운 나무 12그루" + "그중 1그루…" 안내 + 두 섹션 렌더 / review_graduated — "물 7번 준 나무가 완성됐어요 — 당신의 5번째 나무" 배너 / wrongnote — "🌳 나무 완성"·"🌿 물 2번" 뱃지.

- [ ] **Step 7: 커밋**

```bash
git add app/src/debug/java/com/finq/app/debug/ShowcaseActivity.kt
git commit -m "chore: Showcase에 정원·졸업 연출·복습 뱃지 케이스 추가"
```
