# 정원 UI 재편 — 잔디+나무 통합 시각 보상 (설계 스펙)

> 2026-07-21 브레인스토밍 산출물. 백엔드 스펙(`PinQ-backend/docs/superpowers/specs/2026-07-21-review-tree-visibility-design.md`)의
> "후속 비전 — 정원 UI 재편" 절을 구체화한 것. **전 작업이 이 레포(PinQ-frontend) 프론트 온리, 백엔드 변경 0.**

## 배경 / 목표

- 현재 정원(GardenScreen)은 스크롤 목록인데, 목록 데이터는 오답노트(AttemptItem + ReviewStatus)의 부분집합이라 중복이고 카드도 안 열림.
- 방향: **목록 기능은 오답노트 필터칩으로 이관**, 정원은 **순수 시각 보상** — 잔디 위에 나무가 자란 그림 한 장으로 "경제잔디" 성장 메타포 완성.
- 잔디가 마이페이지에 있고 "안 예쁨" → 통합 뷰 안에서 스타일 재해석(격자 잔디 자체는 유지).

## 확정 결정 (브레인스토밍)

| 항목 | 결정 |
|---|---|
| 구현 레포 | `/Users/iyr/SSAFY/PinQ-frontend` (Android Compose) |
| 통합 위젯 위치 | 마이페이지 — 기존 GrassCalendarCard 자리를 통합 "정원 카드"로 교체 |
| 잔디 리디자인 범위 | 격자 잔디 유지, 통합 뷰 안에서 스타일만 재해석 |
| 오답노트 필터칩 | 이번 작업에 포함 (정원 목록 폐기의 전제) |
| 기존 GardenScreen | 풀스크린 시각 정원으로 재탄생 (목록 UI 제거) |
| 렌더 방식 | Compose Canvas 벡터 드로잉 (외부 일러스트 에셋 없음) |
| 구조 | 공유 `GardenCanvas` 컴포저블 — compact/full 두 모드 재사용 |

## 컴포넌트 설계

### 1. GardenCanvas (신규, `ui/components/garden/GardenCanvas.kt`)

- Compose Canvas 드로잉: 하늘 그라데이션 + 지평선 + 잔디밭(풀결 텍스처) 배경 위에
  자라는 항목(stage 0~2: 새싹→풀→나무직전 벡터 패스)과 완성 나무를 그린다.
- 색은 기존 테마(`streakColor` 램프, Lime, Bg*)만 사용 — 새 팔레트 금지.
- 데이터 입력: `ReviewGarden`(growing: stage 포함, graduated 목록, graduatedTrees 카운터).
  카운터 > 목록인 레거시 졸업분은 "이름 없는 나무"로 수량만 채워 그린다.
- **배치 결정성**: quizId 를 시드로 한 의사난수 + 겹침 방지 그리드 슬롯.
  같은 입력이면 항상 같은 좌표 — "내 정원"이라는 감각 유지. 항목 수가 슬롯을 넘으면
  최근 항목 우선으로 그리고 "+N" 표기.
- 모드:
  - **compact**: 고정 높이(~160dp), 히트테스트 없음, 전체 탭 → 풀스크린 진입 콜백.
  - **full**: 가용 영역 채움, 개별 항목 히트테스트 → `onItemTap(quizId)` 콜백.
    이름 없는 나무(레거시 졸업분)는 quizId 가 없으므로 탭 반응 없음.

### 2. 마이페이지 정원 카드 (`MyPageScreen.kt` 수정)

- 기존 GrassCalendarCard 자리에 통합 카드: 상단 헤더("정원 · 🌳 나무 N그루", 탭 → 풀스크린 정원)
  + GardenCanvas compact + 기존 GitHub 격자 잔디(요약칩·범례 포함)를 한 카드 프레임에 배치.
- GrassCalendarCard 내부 구현은 재사용하되 카드 프레임/헤더를 통합 카드로 이동
  (스타일 재해석 = 프레임·타이포·간격 통일, 격자 로직 불변).
- 로딩: 잔디·정원 두 fetch 를 병행, 부분 로딩 스켈레톤은 기존 패턴 재사용.

### 3. GardenScreen 재탄생 (기존 파일 재작성)

- 목록 UI(LazyColumn·GardenItemCard·SectionTitle) 제거.
- 구성: 상단 바(←, "정원") + 요약 한 줄("자라는 중 M · 키운 나무 N그루") + GardenCanvas full.
- 항목 탭 → 오답노트 딥링크(아래 5). GardenViewModel·`GET /api/reviews/garden` 그대로 재사용.
- 빈 정원: 기존 빈 상태 문구("오답을 복습하면 나무가 자라요")를 캔버스 위 오버레이로 유지.

### 4. 오답노트 필터칩 (`WrongNoteTabRoute.kt`, `LibraryScreens.kt` 수정)

- 칩 Row: **전체 / 오답만 / 복습중 / 졸업🌳**.
- `AttemptItem.review`(ReviewStatus) 기반 클라이언트 필터 — 서버 호출·DTO 불변.
  - 오답만: `review == null` (복습 큐 미진입 오답 포함, correct=false 는 목록 자체가 보장)
  - 복습중: `review != null && !review.graduated`
  - 졸업: `review?.graduated == true`
- 칩 상태는 LibraryViewModel state 에 보관, 탭 재진입 시 "전체"로 리셋.

### 5. 딥링크 (`FinQNavigation.kt` 수정)

- 정원 항목 탭 → 오답노트 탭으로 이동하며 `focusQuizId` 인자 전달.
- 오답노트 진입 시 focusQuizId 카드로 스크롤 + 카드 펼침(하이라이트 1회).
- 졸업 항목도 원래 오답이었으므로 오답노트 목록에 존재 — focus 진입 시 필터는 "전체".
- focusQuizId 가 목록에 없으면(엣지) 스크롤 생략, 에러 없이 목록만 표시.

## 에러 / 로딩

기존 패턴 그대로: 첫 로드 스켈레톤, 실패 시 재시도 카드(잔디·정원 각각). 신규 패턴 없음.

## 테스트

- 단위: 배치 결정성(같은 입력→같은 좌표), 슬롯 겹침 없음, 오답노트 필터 분기 3종.
- 시각: GardenCanvas compact/full @Preview, Showcase 에 정원 케이스 갱신(빈/성장/만원 정원).
- 수동: 마이페이지→풀스크린→나무 탭→오답노트 포커스 체인.

## 프로세스 (사용자 지정)

- 설계: fable → 구현: opus → 검증: fable (사용자가 /model 로 전환).
- 구현 단계에서 frontend-design 스킬로 Canvas 드로잉 품질 확보.
- 커밋 후 push 까지 직접 실행. 백엔드 배포 무관(프론트 온리).
