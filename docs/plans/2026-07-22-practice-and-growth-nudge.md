# 미리 연습 + 성장 근접 UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 오답노트 카드에 (1) 성장 근접 스트립(stage+다음 물주기 D-day)과 (2) 격리된 순수 "미리 연습"(로컬 채점, 상태 기록 0)을 추가해 "이 문제 빨리 나무로" 마찰을 SRS 무결성을 깨지 않고 해소한다. 스펙: `docs/specs/2026-07-22-practice-and-growth-nudge-design.md`.

**Architecture:** 표시 문구와 채점을 순수 Kotlin 함수(`PracticeGrowth.kt`)로 분리해 단위 테스트하고, `AttemptItemCard.kt` 안에서 스트립과 인라인 연습 UI를 조립한다. 백엔드/네비/딥링크/서버호출 전부 변경 0 — 기존 `AttemptItem`/`ReviewStatus` 필드만 사용.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), JUnit4 (`testImplementation(libs.junit)`).

## Global Constraints

- 레포 `/Users/iyr/SSAFY/PinQ-frontend`. **백엔드/서버 호출·DTO·네비게이션·딥링크 변경 금지. 연습은 어떤 상태도 기록하지 않는다(로컬 전용).**
- 색: `ui/theme/Color.kt` 역할 토큰만. 유채색은 Lime / Grass 램프 / Error 만. 새 raw 초록/파랑 Color(0x…) 금지. 순백 텍스트 금지. Lime 위 텍스트 OnLime.
- 필수 안내 문구(연습 영역, verbatim): `연습은 나무 성장에 반영되지 않아요. 물은 예정일에 복습으로 줄 수 있어요.`
- 연습 버튼 라벨(verbatim): `미리 연습 (물주기 아님)`
- 주석·UI 문구 한국어, 기존 파일 톤.
- 커밋: 한국어 `feat:` … 끝에 `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`.
- 검증: 컴파일 `./gradlew :app:compileDebugKotlin`, 테스트 `./gradlew :app:testDebugUnitTest`.
- 날짜 비교는 주입된 `today: LocalDate` 인자로(테스트 결정성). 표시 로직에 `LocalDate.now()` 직접 호출 금지 — UI 호출부에서만 `LocalDate.now()`를 넘긴다.

## 기존 코드 참고 (구현자 필독)

- `AttemptItemCard.kt`: `var expanded by remember(item.quizId) { mutableStateOf(initialExpanded) }` (66행). 펼침 시 내 답/정답/해설/키워드/관련기사 렌더(200~307행). 복습 뱃지(139~153행)는 `item.review?.let { ... "🌳 나무 완성" / "{emoji} 물 {n}번" }`.
- `AttemptItem`(data/model/AttemptItem.kt): `choices: List<QuizOption>`, `correctChoiceId: Long?`(미풀이 마스킹 시 null), `explanation: String`, `review: ReviewStatus?`, `unsolved: Boolean`(= correctChoiceId==null).
- `QuizOption`(data/model): `id: Long`, `optionNumber: Int`, `text: String`.
- `ReviewStatus`: `stage: Int`, `graduated: Boolean`, `dueDateIso: String?`, `waterCount: Int`, `absorbedCount: Int`.
- `ReviewStage`(data/repository/ReviewRepository.kt): `enum { SPROUT("새싹","🌱"), GRASS("풀","🌿"), ALMOST_TREE("나무 직전","🪴") }`, `ReviewStage.of(stage: Int): ReviewStage`(0~2 클램프), `.emoji`, `.label`.
- 색 토큰: `Lime`, `TextSecondary`, `TextMuted`, `Grass1`, `BgSubtle`, `MaterialTheme.colorScheme.*`.

---

### Task 1: 표시/채점 순수 로직 (`PracticeGrowth.kt`) + 단위 테스트

**Files:**
- Create: `app/src/main/java/com/finq/app/ui/library/PracticeGrowth.kt`
- Test: `app/src/test/java/com/finq/app/ui/library/PracticeGrowthTest.kt`

**Interfaces:**
- Produces (Task 2·3 이 소비):
  - `data class GrowthStrip(val stageText: String, val dueText: String?, val dueToday: Boolean, val finalStage: Boolean)`
  - `fun growthStrip(stage: Int, graduated: Boolean, dueDateIso: String?, today: LocalDate): GrowthStrip?` — graduated면 null.
  - `fun isPracticeCorrect(selectedChoiceId: Long, correctChoiceId: Long?): Boolean`
- Consumes: `ReviewStage`(기존).

- [ ] **Step 1: 실패하는 테스트 작성**

`app/src/test/java/com/finq/app/ui/library/PracticeGrowthTest.kt`:

```kotlin
package com.finq.app.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PracticeGrowthTest {

    private val today = LocalDate.of(2026, 7, 22)

    @Test
    fun `졸업이면 스트립 없음`() {
        assertNull(growthStrip(stage = 2, graduated = true, dueDateIso = "2026-07-25", today = today))
    }

    @Test
    fun `stage 0 은 1 of 3 단계 · 새싹`() {
        val s = growthStrip(stage = 0, graduated = false, dueDateIso = null, today = today)!!
        assertEquals("🌱 1/3단계", s.stageText)
        assertFalse(s.finalStage)
    }

    @Test
    fun `stage 2 는 마지막 단계 플래그`() {
        val s = growthStrip(stage = 2, graduated = false, dueDateIso = "2026-07-24", today = today)!!
        assertEquals("🪴 3/3단계", s.stageText)
        assertTrue(s.finalStage)
    }

    @Test
    fun `due 가 오늘이면 오늘 물 줄 수 있어요 · dueToday`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "2026-07-22", today = today)!!
        assertEquals("오늘 물 줄 수 있어요", s.dueText)
        assertTrue(s.dueToday)
    }

    @Test
    fun `due 가 과거여도 오늘 물 줄 수 있어요로 취급`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "2026-07-20", today = today)!!
        assertEquals("오늘 물 줄 수 있어요", s.dueText)
        assertTrue(s.dueToday)
    }

    @Test
    fun `due 가 미래면 D-n 물 주기`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "2026-07-25", today = today)!!
        assertEquals("D-3 · 7/25 물 주기", s.dueText)
        assertFalse(s.dueToday)
    }

    @Test
    fun `due 파싱 실패면 시점 생략`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "not-a-date", today = today)!!
        assertNull(s.dueText)
        assertFalse(s.dueToday)
    }

    @Test
    fun `due null 이면 시점 생략`() {
        val s = growthStrip(stage = 0, graduated = false, dueDateIso = null, today = today)!!
        assertNull(s.dueText)
    }

    @Test
    fun `채점 - 정답`() = assertTrue(isPracticeCorrect(selectedChoiceId = 5L, correctChoiceId = 5L))

    @Test
    fun `채점 - 오답`() = assertFalse(isPracticeCorrect(selectedChoiceId = 5L, correctChoiceId = 7L))

    @Test
    fun `채점 - 마스킹(null)이면 오답 취급`() =
        assertFalse(isPracticeCorrect(selectedChoiceId = 5L, correctChoiceId = null))
}
```

- [ ] **Step 2: 실패 확인**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew :app:testDebugUnitTest --tests "com.finq.app.ui.library.PracticeGrowthTest" 2>&1 | tail -15`
Expected: 컴파일 FAIL — `growthStrip`/`isPracticeCorrect` 미정의.

- [ ] **Step 3: 구현**

`app/src/main/java/com/finq/app/ui/library/PracticeGrowth.kt`:

```kotlin
package com.finq.app.ui.library

import com.finq.app.data.repository.ReviewStage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 오답노트 카드 "성장 스트립" 표시 데이터.
 *
 *  - [stageText]  : "🌿 2/3단계" — 나무까지 근접성(goal-gradient).
 *  - [dueText]    : "오늘 물 줄 수 있어요" / "D-3 · 7/25 물 주기" / null(시점 불명).
 *  - [dueToday]   : due 가 오늘 이하 — 오늘의 복습으로 물 줄 수 있음(CTA 강조용).
 *  - [finalStage] : 다음에 맞히면 졸업하는 마지막 단계 — Lime 포인트 1개만.
 */
data class GrowthStrip(
    val stageText: String,
    val dueText: String?,
    val dueToday: Boolean,
    val finalStage: Boolean,
)

private val DUE_MONTH_DAY = DateTimeFormatter.ofPattern("M/d")

/**
 * 복습중(자라는) 오답의 성장 스트립. 졸업이면 null(스트립 없음).
 *
 * 실제 간격을 앞당기는 정보는 담지 않는다 — 근접성과 "예정된 다음 시점"만 보여준다.
 */
fun growthStrip(stage: Int, graduated: Boolean, dueDateIso: String?, today: LocalDate): GrowthStrip? {
    if (graduated) return null

    val clamped = stage.coerceIn(0, 2)
    val emoji = ReviewStage.of(clamped).emoji
    val stageText = "$emoji ${clamped + 1}/3단계"
    val finalStage = clamped == 2

    val due = dueDateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val (dueText, dueToday) = when {
        due == null -> null to false
        !due.isAfter(today) -> "오늘 물 줄 수 있어요" to true
        else -> {
            val days = ChronoUnit.DAYS.between(today, due)
            "D-$days · ${due.format(DUE_MONTH_DAY)} 물 주기" to false
        }
    }
    return GrowthStrip(stageText = stageText, dueText = dueText, dueToday = dueToday, finalStage = finalStage)
}

/** 미리 연습 로컬 채점. 정답 정보가 마스킹(null)이면 채점 불가 → 오답 취급(연습 버튼 자체가 숨겨짐). */
fun isPracticeCorrect(selectedChoiceId: Long, correctChoiceId: Long?): Boolean =
    correctChoiceId != null && selectedChoiceId == correctChoiceId
```

주의(구현자): `dueDateIso`는 서버가 `"2026-07-25"`(ISO_LOCAL_DATE) 형식으로 준다. `LocalDate.parse`(기본 ISO_LOCAL_DATE)로 파싱한다. `"...T..."` 같은 시각 포함 형식이 올 수 있으면 `runCatching`이 이미 흡수하지만, 실제 값이 date-time이면 테스트가 `not-a-date` 케이스처럼 dueText=null이 되므로 — 실서버 값이 date-time이면 이 함수의 파싱을 `LocalDate.parse(it.substring(0,10))`로 바꾸고 테스트도 맞춘다. 먼저 `AttemptItemApiResponse`/`ReviewDtos`에서 dueDate 형식을 확인할 것.

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.finq.app.ui.library.PracticeGrowthTest" 2>&1 | tail -6`
Expected: `BUILD SUCCESSFUL`, 11 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/finq/app/ui/library/PracticeGrowth.kt app/src/test/java/com/finq/app/ui/library/PracticeGrowthTest.kt
git commit -m "feat: 오답노트 성장 스트립·연습 채점 순수 로직 + 테스트"
```

---

### Task 2: 성장 스트립 UI (복습중 카드)

**Files:**
- Modify: `app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt`

**Interfaces:**
- Consumes: `growthStrip(...)`, `GrowthStrip` (Task 1).
- Produces (Task 3 이 같은 파일에서 이어 씀): 카드 요약 영역에 스트립 렌더. 신규 파라미터 없음.

- [ ] **Step 1: 구현** (Compose UI — TDD 대상 아님, 컴파일+Preview로 검증)

`AttemptItemCard.kt` 의 질문 텍스트(`item.question` Text, 181~188행) **직전**에 성장 스트립을 삽입한다. `import java.time.LocalDate`는 이미 있음. 추가:

```kotlin
            // 성장 근접 스트립 — 복습중(자라는) 오답만. 졸업/legacy 는 growthStrip 이 null.
            item.review?.let { review ->
                growthStrip(
                    stage = review.stage,
                    graduated = review.graduated,
                    dueDateIso = review.dueDateIso,
                    today = LocalDate.now(),
                )?.let { strip ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strip.stageText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            // 마지막 단계만 Lime 포인트, 그 외 중립.
                            color = if (strip.finalStage) Lime else TextSecondary,
                        )
                        if (strip.dueText != null) {
                            Text(
                                text = "  ·  ${strip.dueText}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (strip.dueToday) Lime else TextMuted,
                                fontWeight = if (strip.dueToday) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
```

import 추가: `import com.finq.app.ui.theme.TextMuted` (없으면). `Row`, `Alignment`, `Spacer`, `Text`, `FontWeight`, `MaterialTheme`, `Lime`, `TextSecondary` 는 이미 있음.

주의: 스트립은 카드가 접혀 있어도 보인다(요약 영역). `dueToday`의 CTA(오늘의 복습 딥링크)는 이번 범위에서 텍스트 강조까지만 — 탭 네비게이션은 딥링크 변경 금지 제약에 따라 넣지 않는다(스펙의 "없으면 텍스트만" 경로).

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -4`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt
git commit -m "feat: 오답노트 카드 성장 근접 스트립(단계+다음 물주기)"
```

---

### Task 3: 미리 연습 인라인 UI (펼친 카드)

**Files:**
- Modify: `app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt`

**Interfaces:**
- Consumes: `isPracticeCorrect(...)` (Task 1), `item.choices`(`QuizOption`), `item.correctChoiceId`, `item.explanation`, `item.unsolved`.
- Produces: 없음(카드 내부 자기완결).

- [ ] **Step 1: 구현** (Compose UI — 컴파일+Preview 검증)

`AttemptItemCard.kt` 의 펼친 영역(`if (expanded) { ... }`) 안, 관련 기사 블록(`if (hasArticle)`, 256행) **직전**에 연습 섹션을 삽입한다. 미풀이(unsolved)면 정답이 마스킹돼 채점 불가 → 연습 숨김.

먼저 연습 상태를 카드 상태로 추가한다 (`expanded` 선언, 66행 근처):

```kotlin
    var expanded by remember(item.quizId) { mutableStateOf(initialExpanded) }
    // 미리 연습 로컬 상태 — 서버/졸업과 완전 분리. 선택한 선지 id, 없으면 미채점.
    var practiceOpen by remember(item.quizId) { mutableStateOf(false) }
    var practicePick by remember(item.quizId) { mutableStateOf<Long?>(null) }
```

펼친 영역 관련기사 직전 삽입:

```kotlin
                // ── 미리 연습 (순수 연습 · 물주기와 무관) ──────────────────
                if (!item.unsolved) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))

                    if (!practiceOpen) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BgSubtle,
                            modifier = Modifier.clickable {
                                practiceOpen = true
                                practicePick = null
                            },
                        ) {
                            Text(
                                text = "미리 연습 (물주기 아님)",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Lime,
                            )
                        }
                    } else {
                        Text(
                            text = "연습은 나무 성장에 반영되지 않아요. 물은 예정일에 복습으로 줄 수 있어요.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        item.choices.forEach { option ->
                            val picked = practicePick == option.id
                            val isAnswer = option.id == item.correctChoiceId
                            // 선택 후에만 정답/오답 색을 드러낸다.
                            val bg = when {
                                practicePick == null -> BgSubtle
                                isAnswer -> Grass1
                                picked -> MaterialTheme.colorScheme.errorContainer
                                else -> BgSubtle
                            }
                            val fg = when {
                                practicePick == null -> MaterialTheme.colorScheme.onSurface
                                isAnswer -> Lime
                                picked -> MaterialTheme.colorScheme.onErrorContainer
                                else -> TextMuted
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .clickable(enabled = practicePick == null) { practicePick = option.id },
                            ) {
                                Text(
                                    text = option.text,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = fg,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        if (practicePick != null) {
                            val correct = isPracticeCorrect(practicePick!!, item.correctChoiceId)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (correct) "정답이에요 (연습이라 물은 안 줬어요)"
                                       else "오답이에요 · 예정일에 복습으로 다시 만나요",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (correct) Lime else MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = "다시 연습",
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable { practicePick = null },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Lime,
                            )
                        }
                    }
                }
```

import 확인: `fillMaxWidth`, `clickable`, `Surface`, `HorizontalDivider`, `BgSubtle`, `Grass1`, `Lime`, `TextMuted`, `RoundedCornerShape` 모두 이미 있거나 Task 2에서 추가됨. 없으면 추가.

해설은 이미 펼친 영역 상단(216~230행)에 항상 노출되므로 연습 블록에서 중복 표시하지 않는다(스펙의 "해설 노출"은 기존 해설로 충족).

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -4`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt
git commit -m "feat: 오답노트 카드 미리 연습(로컬 채점·기록 0) 인라인 UI"
```

---

### Task 4: Preview 갱신 + 최종 검증

**Files:**
- Modify: `app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt` (파일 하단에 @Preview 없으면 추가)

- [ ] **Step 1: @Preview 추가/갱신**

`AttemptItemCard.kt` 하단에, 복습중(stage 1, 미래 due)·졸업·legacy(review=null) 3개 상태를 보여주는 `@Preview` 컴포저블을 추가한다(파일에 기존 Preview가 있으면 그 패턴을 따르고, 없으면 아래 형태). `FinQTheme`로 감싸고 `AttemptItemCard(item = ..., onToggleBookmark = {})` 호출. QuizOption/AttemptItem/ReviewStatus 는 인라인 더미로 구성(필수 필드: quizId, category, question, choices, selectedChoiceId, correctChoiceId, correct, explanation, keyword, article, bookmarked, solvedAtIso, review). `Category`는 `Category.selectable.first()` 사용.

- [ ] **Step 2: 컴파일 + 전체 테스트**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest 2>&1 | tail -5`
Expected: `BUILD SUCCESSFUL`, PracticeGrowthTest 11 pass, 기존 테스트 회귀 없음.

- [ ] **Step 3: 수동/시각 확인** (에뮬레이터 가능 시)

1. 오답노트 복습중 카드 상단에 `🌿 2/3단계 · D-n · M/d 물 주기` 스트립 보이는가
2. 카드 펼침 → `미리 연습 (물주기 아님)` → 선지 선택 → O/X + 안내 문구, 물 카운터 불변
3. 졸업 카드엔 스트립 없고 `🌳 나무 완성` 뱃지 유지, legacy 카드엔 스트립 없음
4. 미풀이 북마크 카드엔 연습 버튼 없음(기존 "풀러 가기" 유지)

에뮬레이터 불가 시 @Preview 렌더로 대체하고 그 사실을 보고.

- [ ] **Step 4: Commit + Push**

```bash
git add app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt
git commit -m "feat: 미리 연습·성장 스트립 Preview 추가"
git push
```
