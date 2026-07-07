package com.finq.app.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.repository.NotificationRepository
import com.finq.app.data.repository.UserStatsRepository
import com.finq.app.push.FcmTokenManager
import retrofit2.HttpException
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
    val nickname: String = "",
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val totalSolved: Int = 0,
    val correctRate: Float = 0f,
    val activityGrid: List<Int> = emptyList(),
    val isWithdrawing: Boolean = false,
    val withdrawError: String? = null,
    val isUpdatingNickname: Boolean = false,
    val nicknameUpdateError: String? = null,
    // ── 푸시알림 설정 ─────────────────────────────────────────
    val notificationsEnabled: Boolean = false,
    val notificationTime: String = "09:00",       // "HH:mm" (30분 단위)
    val isSavingNotification: Boolean = false,
    val notificationError: String? = null,
)

class MyPageViewModel(
    private val statsRepository: UserStatsRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    /** 탈퇴 완료 시 1회성 이벤트 — 화면에서 홈으로 이동 트리거. */
    private val _withdrawEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val withdrawEvents: SharedFlow<Unit> = _withdrawEvents.asSharedFlow()

    /** 로그아웃 완료 시 1회성 이벤트 — 로그인 화면으로 이동 트리거. */
    private val _logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvents: SharedFlow<Unit> = _logoutEvents.asSharedFlow()

    init {
        loadStats()
        loadNotificationSettings()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { statsRepository.getStats() }
                .onSuccess { stats ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nickname = stats.nickname,
                            streak = stats.streak,
                            maxStreak = stats.maxStreak,
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
     * 회원탈퇴 — JWT 의 userId 로 본인 계정을 삭제한다.
     * 성공 시 [withdrawEvents] 로 1회성 이벤트를 발행한다.
     */
    fun withdraw() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWithdrawing = true, withdrawError = null) }
            runCatching { statsRepository.withdraw() }
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

    /**
     * 닉네임 수정 — 성공 시 UiState 의 nickname 을 갱신한다.
     */
    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingNickname = true, nicknameUpdateError = null) }
            runCatching { statsRepository.updateNickname(newNickname) }
                .onSuccess { updated ->
                    _uiState.update { it.copy(isUpdatingNickname = false, nickname = updated) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isUpdatingNickname = false,
                            nicknameUpdateError = when {
                                e is HttpException && e.code() == 409
                                    -> "이미 사용 중인 닉네임이에요"
                                else -> e.message ?: "닉네임 변경에 실패했어요"
                            },
                        )
                    }
                }
        }
    }

    fun clearNicknameUpdateError() {
        _uiState.update { it.copy(nicknameUpdateError = null) }
    }

    /** 로그아웃 — 세션 정리는 Navigation 레이어에서 처리한다. */
    fun logout() {
        _logoutEvents.tryEmit(Unit)
    }

    // ── 푸시알림 설정 ─────────────────────────────────────────────────────────

    fun loadNotificationSettings() {
        viewModelScope.launch {
            runCatching { notificationRepository.getSettings() }
                .onSuccess { settings ->
                    _uiState.update {
                        it.copy(
                            notificationsEnabled = settings.enabled,
                            notificationTime = settings.time,
                        )
                    }
                }
            // 실패는 조용히 무시 — 화면 진입만으로 에러 다이얼로그를 띄우지 않는다.
        }
    }

    /** 알림 on/off. 켤 때는 FCM 토큰도 재등록해 로그인 시 등록 실패를 보완한다. */
    fun setNotificationsEnabled(enabled: Boolean) {
        val previous = _uiState.value.notificationsEnabled
        viewModelScope.launch {
            _uiState.update {
                it.copy(notificationsEnabled = enabled, isSavingNotification = true, notificationError = null)
            }
            runCatching { notificationRepository.updateSettings(enabled = enabled) }
                .onSuccess { settings ->
                    _uiState.update {
                        it.copy(
                            isSavingNotification = false,
                            notificationsEnabled = settings.enabled,
                            notificationTime = settings.time,
                        )
                    }
                    if (enabled) FcmTokenManager.registerCurrentToken()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSavingNotification = false,
                            notificationsEnabled = previous,
                            notificationError = e.message ?: "알림 설정 변경에 실패했어요",
                        )
                    }
                }
        }
    }

    /** 알림 시각 변경 — time 은 "HH:mm" 30분 단위(HH:00/HH:30)만 허용. */
    fun setNotificationTime(time: String) {
        val previous = _uiState.value.notificationTime
        viewModelScope.launch {
            _uiState.update {
                it.copy(notificationTime = time, isSavingNotification = true, notificationError = null)
            }
            runCatching {
                notificationRepository.updateSettings(
                    enabled = _uiState.value.notificationsEnabled,
                    time = time,
                )
            }
                .onSuccess { settings ->
                    _uiState.update {
                        it.copy(
                            isSavingNotification = false,
                            notificationsEnabled = settings.enabled,
                            notificationTime = settings.time,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSavingNotification = false,
                            notificationTime = previous,
                            notificationError = e.message ?: "알림 시각 변경에 실패했어요",
                        )
                    }
                }
        }
    }

    fun clearNotificationError() {
        _uiState.update { it.copy(notificationError = null) }
    }

    companion object {
        fun factory(
            repository: UserStatsRepository,
            notificationRepository: NotificationRepository,
        ) = viewModelFactory {
            initializer { MyPageViewModel(repository, notificationRepository) }
        }
    }
}
