package com.finq.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.repository.QuizRepository
import com.finq.app.data.repository.ReviewRepository
import com.finq.app.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class HomeViewModel(
    private val quizRepository: QuizRepository,
    private val statsRepository: UserStatsRepository,
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadQuizInfo()
    }

    fun loadQuizInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 퀴즈 로드: 실패 시 홈 화면 전체 에러
            val quizzes = try {
                quizRepository.getTodayQuizzes()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "알 수 없는 오류") }
                return@launch
            }

            // 통계 로드: 부가 정보이므로 실패해도 기본값으로 폴백, 퀴즈 진입은 막지 않음
            val stats = try {
                statsRepository.getStats()
            } catch (e: Exception) {
                null
            }

            // 복습 큐: 역시 부가 정보 — 실패하면 카드를 숨긴다(진입 자체를 막지 않음).
            val reviews = try {
                reviewRepository.getTodayReviews()
            } catch (e: Exception) {
                null
            }

            // 잔디: 주간 스트릭 동그라미가 grass 의 days[].level 을 그대로 쓴다(자체 계산 제거).
            val grass = try {
                statsRepository.getGrass()
            } catch (e: Exception) {
                null
            }

            // 정원: 히어로 미니 프리뷰 — 실패해도 빈 정원으로 그리면 되므로 폴백 null.
            val garden = try {
                reviewRepository.getGarden()
            } catch (e: Exception) {
                null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    nickname = stats?.nickname ?: "",
                    quizCount = quizzes.count { !it.solved },
                    streak = stats?.streak ?: 0,
                    solvedToday = stats?.solvedToday ?: false,
                    maxStreak = stats?.maxStreak ?: 0,
                    weekLevels = grass?.let(::weekLevelsFrom) ?: List(7) { 0 },
                    reviewCount = reviews?.items?.size ?: 0,
                    reviewedToday = reviews?.todayReviewed ?: 0,
                    grownToday = reviews?.todayCorrect ?: 0,
                    nextReviewDate = reviews?.nextDueDate,
                    todayTotal = quizzes.size,
                    todayCorrect = quizzes.count { it.correct == true },
                    garden = garden ?: it.garden,
                )
            }
        }
    }

    /**
     * 이번 주(월~일) 각 날짜의 잔디 level 을 grass 에서 그대로 뽑는다.
     * 오늘 이후(미래)는 -1 로 표시해 화면이 미래 칸을 구분할 수 있게 한다.
     */
    private fun weekLevelsFrom(grass: com.finq.app.data.repository.GrassCalendar): List<Int> {
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
        return (0 until 7).map { offset ->
            val date = monday.plusDays(offset.toLong())
            if (date > today) -1 else grass.levelAt(date)
        }
    }

    companion object {
        fun factory(
            quizRepository: QuizRepository,
            statsRepository: UserStatsRepository,
            reviewRepository: ReviewRepository,
        ) = viewModelFactory {
            initializer { HomeViewModel(quizRepository, statsRepository, reviewRepository) }
        }
    }
}
