package com.finq.app.data.remote.dto

/**
 * `GET /api/users/me/concept-stats` 응답 — 카테고리별 정답률 + 취약 개념.
 *
 * [weakest] 는 표본이 부족하면 null 로 온다(진단 불가).
 */
data class ConceptStatsApiResponse(
    val categories: List<ConceptStatApiResponse>,
    val weakest: ConceptStatApiResponse?,
)

/**
 * @param category    백엔드 enum 이름 (예: `INTEREST_RATE`)
 * @param displayName 화면 표시용 한글 라벨. 클라이언트 enum 에 없는 카테고리가 추가돼도
 *                    깨지지 않도록 **서버가 준 라벨을 그대로 쓴다.**
 * @param correctRate 0.0 ~ 1.0
 */
data class ConceptStatApiResponse(
    val category: String,
    val displayName: String,
    val total: Int,
    val correct: Int,
    val correctRate: Float,
)
