# 미리 연습 + 성장 근접 UI — 설계 스펙

> 2026-07-22 브레인스토밍 산출물 (deep-research 근거 기반). 전 작업 **PinQ-frontend 프론트 온리, 백엔드/네비/딥링크 변경 0, 서버 호출·상태 기록 0.**

## 배경 / 문제

오답노트가 "물 준 횟수"를 보여주자 사용자는 자연히 **"이 한 문제만 빨리 나무로 만들고 싶다"**가 되는데, 복습은 하루 배치(3/7/14일 간격)라 단일 문제를 가속할 경로가 없다 → **보이는 진척 vs 할 수 있는 행동의 불일치**(게이미피케이션 마찰, sandbagging 유발; Naavik Me+ 'Perfect Days' 사례와 동형).

## 리서치 근거 (요지)

- **실제 간격(3/7/14일)을 앞당기는 단일 항목 가속은 금지** — 몰아 반복(massed)은 장기 파지를 유의하게 해침(Cepeda 2006, 317개 실험). 성숙한 SRS 앱(Anki preview/필터덱)은 집중 반복을 허용하되 **실제 스케줄과 격리**한다.
- **goal-gradient / endowed-progress** — 목표 근접성을 강조하면 완료율↑(Kivetz 2006; Nunes & Drèze 2006 세차장 34% vs 19%).
- 결론: **간격은 보존, 가속 욕구는 (a) 격리된 순수 연습 + (b) 근접성 UI 로 흡수.**

## 확정 결정 (브레인스토밍)

| 항목 | 결정 |
|---|---|
| 방향 | 연습 모드(행동) + 근접성 UI(동기) 둘 다 |
| 졸업 반영 | **순수 연습** — waterCount/stage/due 전부 불변. Anki preview 모델. **"물주기에 포함 안 됨"을 명시적으로 안내(필수)** |
| 연습 진입점 | 오답노트 카드 안 |
| 연습 렌더 | **A안 인라인** — 카드 펼친 상태에서 선지 선택 모드, 로컬 채점 |
| 기대 전환 UI | 근접성(stage) + 다음 물주기 시점(D-day) 함께 |
| 범위 | 오답노트 카드(`AttemptItemCard.kt`) 내부. 백엔드·네비·딥링크 변경 0 |

## 컴포넌트 설계

### 1. 성장 스트립 (복습중 항목만)

- 대상: `review != null && !review.graduated`. 카드 펼침 여부와 무관하게(요약 영역) 한 줄 표시.
- 내용: `{stage 이모지} {stage+1}/3단계 · {다음 물주기}` 형태.
  - 근접성: stage 0~2 → "1/3 · 2/3 · 3/3(마지막!)". `ReviewStage.of(stage)` 재사용.
  - 다음 시점: `review.dueDateIso` 파싱 → 오늘 기준 D-day.
    - 오늘 이하(due): `오늘 물 줄 수 있어요 →` (탭 시 오늘의 복습 딥링크 — 기존 REVIEW_GRAPH 경로. 없으면 텍스트만).
    - 미래: `D-{n} 후 물 줄 수 있어요` 또는 `{M/d} 물 주기`.
    - dueDateIso null/파싱실패: 시점 부분 생략, 근접성만.
- 졸업 카드: 기존 `🌳 나무 완성` 뱃지 유지, 스트립 없음. legacy(review==null): 스트립 없음.
- 색: 중립 톤(TextSecondary), 마지막 단계(2→3)만 Lime 포인트 1개. 새 raw 색 금지.

### 2. 미리 연습 (인라인, 모든 오답 카드)

- 위치: 카드 펼친(expanded) 영역, 해설/키워드 아래.
- 버튼: `미리 연습 (물주기 아님)`. 탭 → 카드 내 연습 상태 진입.
- 연습 상태: 선지들이 **선택 가능**하게 렌더(기존 `item.choices`). 선택 시:
  - `item.correctChoiceId` 와 비교해 **로컬 채점** O/X 즉시 표시 + 이미 있는 `item.explanation` 노출.
  - **서버 호출 0, 상태 기록 0.** waterCount/stage/due/absorbedCount 전부 불변.
  - `remember(item.quizId)` 로 카드별 연습 상태 보관(펼침처럼). 다시 접으면 초기화 허용.
- 명시 안내(필수 문구): 연습 영역에 항상 보이는 한 줄 —
  `연습은 나무 성장에 반영되지 않아요. 물은 예정일에 복습으로 줄 수 있어요.`
- unsolved(미풀이 북마크, correctChoiceId==null) 항목: 정답이 마스킹돼 채점 불가 → 연습 버튼 숨김(기존 "풀러 가기" 경로 유지).

### 3. 카피 원칙

"막힘"이 아니라 "예약된 다음 단계"로 읽히게: 시점은 D-day, 근접성은 단계로. 부정 문구("아직 안 됨") 대신 가능/예정 문구.

## 데이터 흐름

전부 기존 `AttemptItem`/`ReviewStatus` 필드로 충족: `choices, correctChoiceId, explanation, review.stage, review.graduated, review.dueDateIso`. **신규 API·DTO·리포지토리 변경 없음.**

## 에러/로딩

없음(추가 네트워크 없음). dueDate 파싱 실패는 스트립 시점 생략으로 흡수.

## 테스트

- 성장 스트립 문구 로직 순수 함수로 분리(`growthStripText(stage, graduated, dueDateIso, today)` → 표시 문자열/상태): 단계별·D-day별·null 분기 단위 테스트.
- 로컬 채점 순수 함수(`isPracticeCorrect(selectedId, correctChoiceId)`): 정답/오답/마스킹(null) 분기.
- 연습이 상태를 안 바꾼다는 것은 설계상 로컬 전용이라 "서버 호출 없음"을 코드 리뷰로 보장(호출 자체가 없음).
- 시각: @Preview — 복습중(각 stage)·졸업·legacy·연습 진행 상태.

## 프로세스

- 설계 fable → 구현 opus → 검증 fable (서브에이전트 디스패치 모델로 지정).
- 구현 시 frontend-design 스킬로 스트립·연습 UI 품질 확보.
- 필터칩이 더 필요하면 이때 추가(현재 전체/복습중/졸업으로 충분 판단, 필요 시 플랜에서).
- 커밋 후 push 는 Claude 가 직접.
