# 문서 색인

이 레포의 문서는 **성격이 네 가지**뿐이다. 폴더가 아니라 이 표에서 찾는다.
백엔드 레포에도 같은 색인이 있다 → [`../PinQ-backend/docs/README.md`](../PinQ-backend/docs/README.md)

> **다른 레포 파일을 가리키는 법 — 형제 상대경로로 쓴다.**
> `../PinQ-backend/docs/...` (○) / `PinQ-backend/docs/...` (✗)
> 레포 이름부터 쓰면 **현재 열려 있는 레포 기준**으로 해석돼 파일을 못 찾는다. 두 레포는 `SSAFY/` 아래 형제라 `../` 하나면 닿는다.
> `/Users/...` 절대경로는 커밋되는 문서에 쓰지 않는다 — 다른 머신에서 깨진다. 추적되지 않는 `CLAUDE.md` 에만 쓴다.

| 성격 | 뜻 | 어디 |
|---|---|---|
| 📏 **규칙 (SSOT)** | 지금 참인 것. 코드와 어긋나면 **코드를 고친다** | `ui-rules.md` |
| 🧭 **결정 기록** | 왜 이렇게 됐나 / 뭘 기각했나 | `decisions/` |
| 📦 **작업 산출물** | 일회성. 완료되면 참고용 | `specs/`, `plans/`, `handoff/` |
| 🏪 **스토어 자산** | Play Console 에 올리는 것 | `../store-assets/` |

> 대기 작업의 단일 진실은 **백엔드 레포**에 있다 → [`../PinQ-backend/docs/PENDING.md`](../PinQ-backend/docs/PENDING.md).
> 프론트 온리 작업이어도 거기 적는다.

---

## 📏 규칙 (SSOT)

| 문서 | 무엇 |
|---|---|
| [ui-rules.md](ui-rules.md) | 프론트 표현 규칙. **프론트가 소유한 규칙만** 담는다 — 서버와 무관하므로 낡지 않는다 |

> 성장 지표(잔디·스트릭·나무)의 규칙은 프론트가 아니라 백엔드가 소유한다
> → [`../PinQ-backend/docs/rules/grass-and-streak.md`](../PinQ-backend/docs/rules/grass-and-streak.md)

## 🧭 결정 기록

| 문서 | 결정 | 상태 |
|---|---|---|
| [decisions/wrong-notes-lightweight-request.md](decisions/wrong-notes-lightweight-request.md) | 오답노트/이력/북마크 목록 경량화 — 목록은 요약만, 상세는 단건 조회 | 완료 (프론트 `09a4863`) |

> 백엔드 API 관련 결정은 백엔드 레포에 모여 있다 → [`../PinQ-backend/docs/decisions/`](../PinQ-backend/docs/decisions/) (5건)

## 📦 작업 산출물 — 완료, 참고용

배포가 끝난 문서다. **"그때 왜 그렇게 정했나"가 궁금할 때만** 연다.
`specs/` 가 설계 결정, `plans/` 가 그 구현 계획서다 — 짝을 이룬다.

| 설계 (specs) | 구현 계획 (plans) | 무엇 | 상태 |
|---|---|---|---|
| [specs/2026-07-21-garden-ui-redesign-design.md](specs/2026-07-21-garden-ui-redesign-design.md) | [plans/2026-07-21-garden-ui-redesign.md](plans/2026-07-21-garden-ui-redesign.md) | 정원 UI 재편 — 잔디+나무 통합 시각 보상. 목록은 오답노트 필터칩으로 이관 | ✅ 완료·병합 `5fcf04f`. **프론트 온리, 백엔드 변경 0** |
| [specs/2026-07-22-practice-and-growth-nudge-design.md](specs/2026-07-22-practice-and-growth-nudge-design.md) | [plans/2026-07-22-practice-and-growth-nudge.md](plans/2026-07-22-practice-and-growth-nudge.md) | 미리 연습 + 성장 근접 UI — 격리된 순수 연습(기록 0) + goal-gradient | ✅ 완료. **서버 호출·상태 기록 0** |
| (백엔드가 소유) | [plans/review-tree-plan.md](plans/review-tree-plan.md) | 복습 나무 프론트 연동 — 물 이력, 채점 응답 확장, 정원 화면 신설, 복습 뱃지 | ✅ 완료. 설계는 [`../PinQ-backend/docs/decisions/review-tree-visibility-frontend.md`](../PinQ-backend/docs/decisions/review-tree-visibility-frontend.md) |

### 핸드오프 (handoff)

세션이 끝난 시점의 상태와 맥락. **대기 작업 자체는 여기 없다** — `PENDING.md` 가 SSOT다.

| 문서 | 무엇 |
|---|---|
| [handoff/2026-08-05-concept-diagnosis-and-garden.md](handoff/2026-08-05-concept-diagnosis-and-garden.md) | 정원 후광(배지와 개수 불일치) · 개념 진단 다섯 상태 · 규칙 문서 소유자 분담 |

## 🏪 스토어 자산

| 문서 | 무엇 | 주의 |
|---|---|---|
| [../store-assets/README.md](../store-assets/README.md) | Play Console 이미지 — 아이콘·피처 그래픽·스크린샷 | **APK 와 별개.** 앱을 배포해도 자동으로 안 바뀌고 콘솔에서 따로 교체해야 함 |
| [../store-assets/listing.md](../store-assets/listing.md) | 스토어 등록정보 문구. 복사해 붙이는 용도 | 적힌 내용은 전부 실제 동작과 맞춘 것 — 어긋나면 리뷰에서 먼저 지적당함 |

---

## 레포 밖 · 폴더 밖

| 위치 | 무엇 |
|---|---|
| [../README.md](../README.md) | 발표자료 · 시연영상 링크 |
| [../CLAUDE.md](../CLAUDE.md) | 백엔드 레포 경로 안내 |
| `portfolio-drafts/` | 포트폴리오 초안. **gitignore 됨** — 레포 문서가 아니라 로컬 작업물 |
