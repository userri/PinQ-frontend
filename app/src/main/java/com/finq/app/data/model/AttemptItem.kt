package com.finq.app.data.model

/**
 * 풀이 이력 / 오답노트 / 북마크 화면이 공통으로 사용하는 항목.
 *
 * 백엔드 AttemptItemResponse 와 1:1 매핑되며,
 * 한 문제에 대한 "내 풀이 결과 + 정답 + 해설 + 북마크 여부" 를 한꺼번에 담는다.
 *
 *  - selectedChoiceId : 사용자가 첫 시도에 고른 선택지. legacy(=서버 도입 이전) 데이터면 null.
 *  - solvedAtIso     : 첫 풀이 시각 (ISO-8601 문자열). null 가능 — 정렬 표시에 사용.
 *  - bookmarked      : 화면에서 즉시 토글 UI 를 그릴 수 있도록 포함.
 */
data class AttemptItem(
    val quizId: Long,
    val category: Category,
    val question: String,
    val choices: List<QuizOption>,
    val selectedChoiceId: Long?,
    /** 미풀이 북마크는 서버가 정답 정보를 null 로 마스킹한다(치팅 방지). */
    val correctChoiceId: Long?,
    val correct: Boolean,
    val explanation: String,
    val keyword: String?,
    val article: RelatedArticle?,
    val bookmarked: Boolean,
    val solvedAtIso: String?,
) {
    /** 화면 표시용 — 내가 고른 선택지의 텍스트. 없으면 "기록 없음". */
    val myAnswerText: String
        get() = selectedChoiceId
            ?.let { id -> choices.firstOrNull { it.id == id }?.text }
            ?: "기록 없음"

    /**
     * 아직 풀지 않은 문제인가 — 미풀이 북마크(치팅 방지 마스킹) 판별.
     *
     * 서버가 미풀이 항목의 정답 정보(correctChoiceId·explanation·keyword)를 null 로 마스킹하므로
     * [correctChoiceId] 유무가 가장 확실한 신호다. selectedChoiceId/solvedAt 는
     *  - 세션 직후 오답노트(WrongNoteScreen)가 solvedAt 을 null 로 두거나
     *  - 레거시 데이터가 selectedChoiceId 를 null 로 두는
     * 경우가 있어 단독으로는 오판을 낸다.
     */
    val unsolved: Boolean
        get() = correctChoiceId == null

    /** 화면 표시용 — 정답 선택지의 텍스트. 미풀이(마스킹)면 "-". */
    val correctAnswerText: String
        get() = correctChoiceId
            ?.let { id -> choices.firstOrNull { it.id == id }?.text }
            ?: "-"

    /** 표시용 카테고리 라벨 (예: "금리"). 알 수 없는 enum 이면 그대로 영문. */
    val categoryDisplay: String get() = category.displayName
}
