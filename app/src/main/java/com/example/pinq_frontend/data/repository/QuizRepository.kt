package com.example.pinq_frontend.data.repository

import com.example.pinq_frontend.data.model.Quiz

/**
 * 퀴즈 데이터에 대한 추상 계층.
 *
 * ViewModel 은 이 인터페이스에만 의존한다.
 * 구현체:
 *  - Phase 1: [DummyQuizRepository] — 로컬 더미 데이터
 *  - Phase 2: ApiQuizRepository — Retrofit 으로 백엔드 호출 (예정)
 *
 * suspend 로 선언해 두면 추후 네트워크 호출로 교체할 때 시그니처가 그대로 유지된다.
 */
interface QuizRepository {

    /** 오늘의 퀴즈 4문제를 반환. */
    suspend fun getTodayQuizzes(): List<Quiz>

    /** 사용자가 선택한 옵션을 채점하고 해설/관련 기사를 함께 반환. */
    suspend fun submitAnswer(quizId: Long, selectedOptionId: Long): AnswerResult
}
