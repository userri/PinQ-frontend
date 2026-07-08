package com.finq.app.data.repository

import com.finq.app.data.remote.UserApi
import com.finq.app.data.remote.dto.ConceptStatApiResponse
import com.finq.app.data.remote.dto.ConceptStatsApiResponse
import com.finq.app.data.remote.dto.GrassApiResponse
import com.finq.app.data.remote.dto.UpdateNicknameRequest
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ApiUserStatsRepository(private val api: UserApi) : UserStatsRepository {

    override suspend fun getStats(): UserStats {
        val dto = api.getUserStats()
        return UserStats(
            nickname = dto.nickname,
            streak = dto.streak,
            maxStreak = dto.maxStreak,
            totalSolved = dto.totalSolved,
            correctRate = dto.correctRate,
            activityGrid = dto.activityGrid,
        )
    }

    override suspend fun getGrass(): GrassCalendar = api.getGrass().toDomain()

    override suspend fun getConceptStats(): ConceptStats = api.getConceptStats().toDomain()

    override suspend fun updateNickname(nickname: String): String {
        val response = api.updateNickname(UpdateNicknameRequest(nickname))
        return response.nickname
    }

    override suspend fun withdraw() {
        api.withdraw()
    }
}

// ── DTO → 도메인 ─────────────────────────────────────────────────────────────

private fun GrassApiResponse.toDomain(): GrassCalendar {
    val fromDate = parseDate(from) ?: LocalDate.now()
    // to 가 깨져 있으면 오늘로 보정. from 보다 앞서면 날짜 범위 계산이 무너지므로 되돌린다.
    val toDate = (parseDate(to) ?: LocalDate.now()).coerceAtLeast(fromDate)

    // 날짜가 파싱되지 않는 항목은 조용히 버린다 — 서버가 이상한 값을 줘도 그리드는 그려져야 한다.
    val levels = days.mapNotNull { day ->
        val date = parseDate(day.date) ?: return@mapNotNull null
        date to day.level.coerceIn(0, MAX_GRASS_LEVEL)
    }.toMap()

    return GrassCalendar(
        from = fromDate,
        to = toDate,
        totalActiveDays = totalActiveDays,
        perfectDays = perfectDays,
        currentStreak = currentStreak,
        maxStreak = maxStreak,
        levelByDate = levels,
    )
}

private fun ConceptStatsApiResponse.toDomain(): ConceptStats = ConceptStats(
    categories = categories.map { it.toDomain() },
    weakest = weakest?.toDomain(),
)

private fun ConceptStatApiResponse.toDomain(): ConceptStat = ConceptStat(
    category = category,
    displayName = displayName,
    total = total,
    correct = correct,
    correctRate = correctRate.coerceIn(0f, 1f),
)

/** streakColor(0..4) 규약 상한. */
private const val MAX_GRASS_LEVEL = 4

/** ISO-8601 `yyyy-MM-dd`. 파싱 실패 시 null — 호출부가 폴백을 정한다. */
private fun parseDate(raw: String): LocalDate? =
    try {
        LocalDate.parse(raw)
    } catch (e: DateTimeParseException) {
        null
    }
