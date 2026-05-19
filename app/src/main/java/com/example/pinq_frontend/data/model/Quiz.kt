package com.example.pinq_frontend.data.model

/**
 * 한 개의 4지선다 퀴즈.
 *
 * 백엔드 `GET /api/quizzes/today` 응답과 매핑된다.
 * 정답 정보(correctOptionId, explanation, relatedArticle) 는 의도적으로 미노출이므로
 * API 모드에서 이 필드들은 placeholder 로 채워지고 실제 화면 그리기엔 사용되지 않는다.
 * (그 정보는 정답 제출 직후 [com.example.pinq_frontend.data.repository.AnswerResult] 로만 들어온다.)
 */
data class Quiz(
    val id: Long,
    val category: Category,
    val question: String,
    val options: List<QuizOption>,
    /** 사용자가 이미 풀었는지 여부. 백엔드 UserQuizAttempt 기준. */
    val solved: Boolean = false,
    /** Phase 2 부터 API 모드에서는 의미 없음 (placeholder). 더미 모드 전용. */
    val correctOptionId: Long = 0L,
    /** Phase 2 부터 API 모드에서는 의미 없음 (placeholder). */
    val explanation: String = "",
    /** Phase 2 부터 API 모드에서는 의미 없음 (placeholder). */
    val relatedArticle: RelatedArticle = RelatedArticle.EMPTY,
)

data class QuizOption(
    val id: Long,
    val optionNumber: Int, // 1~4 번호 표시용
    val text: String,
)

/**
 * 관련 뉴스 기사 정보.
 *
 * 백엔드 NewsArticle 엔티티에 대응. 더미 모드와 API 모드 양쪽을 만족시키기 위해
 * 핵심 필드(title/url/source) 외에는 nullable 로 둔다.
 */
data class RelatedArticle(
    val title: String,
    val url: String,
    val source: String,
    val id: Long? = null,
    val category: String? = null,
    val categoryDisplayName: String? = null,
    val publishedAt: String? = null,
) {
    companion object {
        val EMPTY = RelatedArticle(title = "", url = "", source = "")
    }
}
