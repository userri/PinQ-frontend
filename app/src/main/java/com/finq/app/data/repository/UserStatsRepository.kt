package com.finq.app.data.repository

import java.time.LocalDate

data class UserStats(
    val nickname: String,
    val streak: Int,
    val maxStreak: Int,
    val totalSolved: Int,
    val correctRate: Float,
    val activityGrid: List<Int>,
)

/**
 * 연간 잔디밭.
 *
 * 서버는 활동한 날만 내려주므로(sparse), 없는 날짜는 [levelAt] 이 0(활동 없음)으로 메꾼다.
 * level 은 [com.finq.app.ui.theme.streakColor] 의 강도 규약(0~4)과 1:1 이다.
 */
data class GrassCalendar(
    val from: LocalDate,
    val to: LocalDate,
    val totalActiveDays: Int,
    val perfectDays: Int,
    val currentStreak: Int,
    val maxStreak: Int,
    val levelByDate: Map<LocalDate, Int>,
) {
    fun levelAt(date: LocalDate): Int = levelByDate[date] ?: 0

    companion object {
        val EMPTY = GrassCalendar(
            from = LocalDate.now(),
            to = LocalDate.now(),
            totalActiveDays = 0,
            perfectDays = 0,
            currentStreak = 0,
            maxStreak = 0,
            levelByDate = emptyMap(),
        )
    }
}

/** 카테고리별 정답률. [displayName] 은 서버가 준 라벨을 그대로 쓴다(클라 enum 미등록 카테고리 대비). */
data class ConceptStat(
    val category: String,
    val displayName: String,
    val total: Int,
    val correct: Int,
    val correctRate: Float,
)

/** [weakest] 가 null 이면 표본 부족 — 진단 대신 안내 문구를 띄운다. */
data class ConceptStats(
    val categories: List<ConceptStat>,
    val weakest: ConceptStat?,
) {
    companion object {
        val EMPTY = ConceptStats(categories = emptyList(), weakest = null)
    }
}

interface UserStatsRepository {
    suspend fun getStats(): UserStats

    /** 연간 잔디밭. */
    suspend fun getGrass(): GrassCalendar

    /** 카테고리별 정답률 + 취약 개념. */
    suspend fun getConceptStats(): ConceptStats

    /**
     * 닉네임 수정. 성공 시 변경된 닉네임 반환.
     */
    suspend fun updateNickname(nickname: String): String

    /**
     * 회원탈퇴 — JWT 인증 기반, 본인 계정 삭제.
     */
    suspend fun withdraw()
}
