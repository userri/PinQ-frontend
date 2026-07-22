package com.finq.app.data.remote.dto

/**
 * 목록(요약)과 단건 상세를 함께 담는 응답 DTO.
 *
 *  - 목록: `GET /api/me/attempts`, `/api/me/wrong-notes`, `/api/bookmarks`
 *    → 경량화 후 무거운 필드(choices/explanation/keyword/article)는 생략된다.
 *  - 상세: `GET /api/me/attempts/{quizId}` → 위 필드를 모두 포함한다.
 *
 * 요약 응답에 없는 필드는 전부 기본값을 둬 파싱이 깨지지 않게 한다
 * (구서버가 전체 필드를 계속 줘도 그대로 파싱됨 — 하위호환).
 * 백엔드 com.example.pinq_backend.user.dto.AttemptItemResponse 와 매핑.
 */
data class AttemptItemApiResponse(
    val quizId: Long,
    val category: String,
    val categoryDisplayName: String,
    val question: String,
    // 상세에만 존재 — 요약 응답엔 없음.
    val choices: List<ChoiceSummaryApi> = emptyList(),
    val selectedChoiceId: Long? = null,
    // 미풀이 북마크는 치팅 방지를 위해 정답 정보가 null 로 마스킹돼 온다.
    val correctChoiceId: Long? = null,
    val correct: Boolean = false,
    val explanation: String? = null,
    val keyword: String? = null,
    val article: ArticleApiResponse? = null,
    /**
     * 풀이 완료 여부. 요약 경량화로 correctChoiceId 가 사라져도
     * 이 플래그로 미풀이(마스킹) 여부를 확실히 판별한다. 구서버는 null.
     */
    val solved: Boolean? = null,
    val bookmarked: Boolean = false,
    val solvedAt: String? = null,
    /** 복습(물 주기) 상태. 복습 큐에 오른 적 없는 문제면 null. 구서버도 null. */
    val review: ReviewStatusApi? = null,
)

data class ChoiceSummaryApi(
    val id: Long,
    val orderNum: Int,
    val content: String,
)

data class ReviewStatusApi(
    val stage: Int = 0,
    val waterCount: Int = 0,
    val absorbedCount: Int = 0,
    val graduated: Boolean = false,
    val dueDate: String? = null,
)
