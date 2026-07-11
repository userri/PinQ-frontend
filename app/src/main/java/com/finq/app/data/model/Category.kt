package com.finq.app.data.model

/**
 * 퀴즈 카테고리.
 * 백엔드 enum 과 1:1 매칭되도록 이름은 영문 대문자로 유지하고,
 * 화면에 노출되는 한글 라벨은 [displayName] 으로 따로 관리한다.
 */
enum class Category(val displayName: String) {
    INTEREST_RATE("금리"),
    EXCHANGE_RATE("환율"),
    STOCK("증시"),
    REAL_ESTATE("부동산"),
    INFLATION("물가"),

    /**
     * 클라이언트가 모르는 카테고리 폴백 — 서버에 새 카테고리가 추가돼도
     * 구버전 앱이 크래시하거나 엉뚱한 카테고리(과거엔 STOCK 폴백)로 오분류되지 않게 한다.
     * 필터 칩 등 사용자 선택 UI 에는 노출하지 않는다.
     */
    UNKNOWN("기타"),
    ;

    companion object {
        /**
         * 서버 문자열 → enum 안전 파싱. 모르는 값은 [UNKNOWN].
         * 카테고리는 앞으로도 늘어날 수 있으므로 반드시 valueOf 대신 이걸 쓸 것.
         */
        fun fromServer(raw: String?): Category =
            entries.firstOrNull { it.name == raw } ?: UNKNOWN

        /** 사용자에게 선택지로 노출할 실제 카테고리 목록 (UNKNOWN 제외). */
        val selectable: List<Category> get() = entries.filter { it != UNKNOWN }
    }
}
