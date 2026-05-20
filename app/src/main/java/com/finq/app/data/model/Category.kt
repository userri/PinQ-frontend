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
}
