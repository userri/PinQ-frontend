package com.example.pinq_frontend.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinq_frontend.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPageUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val streak: Int = 0,
    val totalSolved: Int = 0,
    val correctRate: Float = 0f,
    val activityGrid: List<Boolean> = emptyList(),
    val isWithdrawing: Boolean = false,
    val withdrawError: String? = null,
)

/** Phase 2: 인증이 없으므로 단일 demo 닉네임을 사용한다. */
private const val DEMO_NICKNAME = "demo"

class MyPageViewModel(private val statsRepository: UserStatsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    /** 탈퇴 완료 시 1회성 이벤트 — 화면에서 홈으로 이동 트리거. */
    private val _withdrawEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val withdrawEvents: SharedFlow<Unit> = _withdrawEvents.asSharedFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { statsRepository.getStats() }
                .onSuccess { stats ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            streak = stats.streak,
                            totalSolved = stats.totalSolved,
                            correctRate = stats.correctRate,
                            activityGrid = stats.activityGrid,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "통계를 불러오지 못했어요")
                    }
                }
        }
    }

    /**
     * 회원탈퇴 — 성공 시 [withdrawEvents] 로 1회성 이벤트를 발행한다.
     * Phase 2 에서는 demo 유저 1명 운영이므로 탈퇴 후 다음 요청에서 다시 생성된다.
     */
    fun withdraw() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWithdrawing = true, withdrawError = null) }
            runCatching { statsRepository.withdraw(DEMO_NICKNAME) }
                .onSuccess {
                    _uiState.update { it.copy(isWithdrawing = false) }
                    _withdrawEvents.tryEmit(Unit)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isWithdrawing = false,
                            withdrawError = e.message ?: "탈퇴에 실패했어요",
                        )
                    }
                }
        }
    }

    fun clearWithdrawError() {
        _uiState.update { it.copy(withdrawError = null) }
    }

    companion object {
        fun factory(repository: UserStatsRepository) = viewModelFactory {
            initializer { MyPageViewModel(repository) }
        }
    }
}
