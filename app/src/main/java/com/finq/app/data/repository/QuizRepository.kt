package com.finq.app.data.repository

import com.finq.app.data.model.Quiz

/**
 * 퀴즈 데이터에 대한 추상 계층.
 *
 * ViewModel 은 이 인터페이스에만 의존한다.
 * 구현체: [ApiQuizRepository] — Retrofit 으로 백엔드를 호출한다.
 * (로컬 더미 구현은 제거됨 — 모든 화면은 실 API 에 연결된다.)
 */
interface QuizRepository {

    /** 오늘의 퀴즈 4문제를 반환. */
    suspend fun getTodayQuizzes(): List<Quiz>

    /** 사용자가 선택한 옵션을 채점하고 해설/관련 기사를 함께 반환. */
    suspend fun submitAnswer(quizId: Long, selectedOptionId: Long): AnswerResult
}
