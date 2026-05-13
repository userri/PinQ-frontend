package com.example.pinq_frontend.data

import com.example.pinq_frontend.data.model.Category
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.QuizOption
import com.example.pinq_frontend.data.model.RelatedArticle

/**
 * Phase 1 더미 퀴즈 시드.
 * - 4개 카테고리(금리/환율/증시/부동산)를 각각 1문제씩 커버
 * - 옵션 id 는 1..4 로 단순화 (백엔드 DTO 매핑 시에도 충돌 없음)
 * - 해설/기사 텍스트는 의도적으로 "더미"라는 사실이 드러나게 작성
 */
object DummyQuizData {

    val todayQuizzes: List<Quiz> = listOf(
        Quiz(
            id = 1L,
            category = Category.INTEREST_RATE,
            question = "한국은행 기준금리를 결정하는 회의체의 이름은 무엇일까요?",
            options = listOf(
                QuizOption(id = 1L, optionNumber = 1, text = "금융통화위원회"),
                QuizOption(id = 2L, optionNumber = 2, text = "한국통화정책회의"),
                QuizOption(id = 3L, optionNumber = 3, text = "기획재정부 금리위원회"),
                QuizOption(id = 4L, optionNumber = 4, text = "한국금융감독원"),
            ),
            correctOptionId = 1L,
            explanation = "AI 해설이 여기 들어갑니다. (Phase 2 에서 실제 해설로 대체)",
            relatedArticle = RelatedArticle(
                title = "한은 금통위, 기준금리 동결 결정...연 3.25% 유지",
                url = "https://example.com/news/interest-rate",
                source = "더미경제신문",
            ),
        ),
        Quiz(
            id = 2L,
            category = Category.EXCHANGE_RATE,
            question = "원/달러 환율이 1,400원에서 1,300원으로 떨어졌다면, 원화의 가치는 어떻게 변했을까요?",
            options = listOf(
                QuizOption(id = 1L, optionNumber = 1, text = "원화 가치가 하락했다"),
                QuizOption(id = 2L, optionNumber = 2, text = "원화 가치가 상승했다"),
                QuizOption(id = 3L, optionNumber = 3, text = "원화 가치는 변화 없다"),
                QuizOption(id = 4L, optionNumber = 4, text = "달러 가치도 함께 상승했다"),
            ),
            correctOptionId = 2L,
            explanation = "AI 해설이 여기 들어갑니다. (Phase 2 에서 실제 해설로 대체)",
            relatedArticle = RelatedArticle(
                title = "원/달러 환율 1,300원대 진입...수출 기업 비상",
                url = "https://example.com/news/exchange-rate",
                source = "더미경제신문",
            ),
        ),
        Quiz(
            id = 3L,
            category = Category.STOCK,
            question = "코스피 지수가 전일 대비 2% 상승했다고 할 때, 이는 무엇을 의미할까요?",
            options = listOf(
                QuizOption(id = 1L, optionNumber = 1, text = "코스피 상장 종목 모두가 2% 올랐다"),
                QuizOption(id = 2L, optionNumber = 2, text = "거래량이 2% 증가했다"),
                QuizOption(id = 3L, optionNumber = 3, text = "코스피 지수의 가중평균이 2% 올랐다"),
                QuizOption(id = 4L, optionNumber = 4, text = "외국인 매수세가 2% 늘었다"),
            ),
            correctOptionId = 3L,
            explanation = "AI 해설이 여기 들어갑니다. (Phase 2 에서 실제 해설로 대체)",
            relatedArticle = RelatedArticle(
                title = "코스피, 외국인 매수에 2% 급등...2,700선 회복",
                url = "https://example.com/news/stock",
                source = "더미경제신문",
            ),
        ),
        Quiz(
            id = 4L,
            category = Category.REAL_ESTATE,
            question = "주택담보대출의 LTV(Loan To Value)는 무엇을 의미할까요?",
            options = listOf(
                QuizOption(id = 1L, optionNumber = 1, text = "주택 가격 대비 대출 한도 비율"),
                QuizOption(id = 2L, optionNumber = 2, text = "연소득 대비 대출 한도 비율"),
                QuizOption(id = 3L, optionNumber = 3, text = "보증금 대비 월세 비율"),
                QuizOption(id = 4L, optionNumber = 4, text = "주택 면적 대비 대출 금액"),
            ),
            correctOptionId = 1L,
            explanation = "AI 해설이 여기 들어갑니다. (Phase 2 에서 실제 해설로 대체)",
            relatedArticle = RelatedArticle(
                title = "정부, 수도권 LTV 규제 완화 검토...실수요자 숨통 트일까",
                url = "https://example.com/news/real-estate",
                source = "더미경제신문",
            ),
        ),
    )

    fun findById(id: Long): Quiz? = todayQuizzes.firstOrNull { it.id == id }
}
