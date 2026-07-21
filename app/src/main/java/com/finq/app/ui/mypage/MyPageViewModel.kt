package com.finq.app.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finq.app.data.repository.ConceptStats
import com.finq.app.data.repository.GrassCalendar
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
    /**
     * 첫 데이터 로딩 중 — 전체 화면 스피너.
     * 재진입 갱신은 stale-while-revalidate: [loadedOnce] 후에는 이 값을 다시 올리지 않고
     * 이전 값을 보여준 채 백그라운드로 새로 받는다 (깜빡임 방지).
     */
    val isLoading: Boolean = true,
    /** 통계를 한 번이라도 성공적으로 받았는가 — SWR 판별용. */
    val loadedOnce: Boolean = false,
    val error: String? = null,
    val nickname: String = "",
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val totalSolved: Int = 0,
    val correctRate: Float = 0f,
    /** 연간 잔디밭. null 이면 아직 로딩 전 — 화면은 스켈레톤을 그린다(옛 8주 폴백 제거됨). */
    val grass: GrassCalendar? = null,
    /** 잔디밭 첫 로드가 실패했는가 — 스켈레톤 대신 재시도 카드를 보여준다. */
    val grassFailed: Boolean = false,
    /** 카테고리별 정답률 + 취약 개념. 못 받으면 null — 섹션을 숨긴다. */
    val conceptStats: ConceptStats? = null,
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
        refresh()
        loadNotificationSettings()
    }

    /**
     * 화면 진입/재진입 시 호출 — stats·잔디·개념 통계를 함께 갱신한다.
     * 데이터가 이미 있으면(loadedOnce) 스피너 없이 백그라운드 갱신(SWR).
     */
    fun refresh() {
        loadStats()
        loadGrass()
        loadConceptStats()
    }

    /**
     * 연간 잔디밭. 부가 정보이므로 실패해도 화면 전체를 에러로 만들지 않는다
     * (null 로 두면 MyPageScreen 이 기존 8주 히트맵으로 폴백한다).
     */
    fun loadGrass() {
        viewModelScope.launch {
            runCatching { statsRepository.getGrass() }
                .onSuccess { grass ->
                    _uiState.update { it.copy(grass = grass, grassFailed = false) }
                }
                .onFailure {
                    // SWR: 이전 값이 있으면 그대로 두고(조용한 실패), 첫 로드 실패만 재시도 카드.
                    _uiState.update { s ->
                        if (s.grass == null) s.copy(grassFailed = true) else s
                    }
                }
        }
    }

    /** 취약 개념 진단. 실패하면 섹션을 숨긴다(null). */
    fun loadConceptStats() {
        viewModelScope.launch {
            runCatching { statsRepository.getConceptStats() }
                .onSuccess { stats -> _uiState.update { it.copy(conceptStats = stats) } }
                // SWR: 실패해도 이전 값을 지우지 않는다 — 섹션이 사라졌다 나타나는 깜빡임 방지.
                .onFailure { }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            // SWR: 첫 로드만 전체 스피너. 이후 재진입 갱신은 이전 값을 보여준 채 조용히.
            _uiState.update { it.copy(isLoading = !it.loadedOnce, error = null) }
            runCatching { statsRepository.getStats() }
                .onSuccess { stats ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadedOnce = true,
                            nickname = stats.nickname,
                            streak = stats.streak,
                            maxStreak = stats.maxStreak,
                            totalSolved = stats.totalSolved,
                            correctRate = stats.correctRate,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // SWR: 데이터가 이미 있으면 에러 화면으로 덮지 않고 이전 값 유지.
                            error = if (it.loadedOnce) it.error
                            else e.message ?: "통계를 불러오지 못했어요",
                        )
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
