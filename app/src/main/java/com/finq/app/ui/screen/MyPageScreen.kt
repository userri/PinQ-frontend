package com.finq.app.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.material3.ButtonDefaults
import com.finq.app.ui.theme.FinQTheme
import java.util.Calendar
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.LimeFaint
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.streakColor

/**
 * 마이페이지 — Stateless View.
 *
 * @param streak          연속 학습 일수
 * @param totalSolved     누적 풀이 수
 * @param correctRate     정답률 0.0~1.0
 * @param activityGrid    최근 56일(8주×7일) 활동 강도.
 *                        index 0=55일 전, index 55=오늘.
 *                        0=활동 없음, 1=시도했으나 정답 0개, 2=1개 정답, 3=2개 정답, 4=3개 이상 정답.
 * @param appVersion      BuildConfig.VERSION_NAME
 * @param isLoading         통계 로딩 중 여부
 * @param error             통계 로드 실패 메시지 (null이면 정상)
 * @param onRetry           에러 상태에서 재시도 콜백
 * @param withdrawError     탈퇴 실패 메시지 (null이면 정상)
 * @param onClearWithdrawError 탈퇴 에러 다이얼로그 닫기 콜백
 */
@Composable
fun MyPageScreen(
    nickname: String,
    streak: Int,
    maxStreak: Int,
    totalSolved: Int,
    correctRate: Float,
    activityGrid: List<Int>,
    appVersion: String,
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {},
    isWithdrawing: Boolean = false,
    onWithdraw: () -> Unit = {},
    withdrawError: String? = null,
    onClearWithdrawError: () -> Unit = {},
    onLogout: () -> Unit = {},
    isUpdatingNickname: Boolean = false,
    nicknameUpdateError: String? = null,
    onUpdateNickname: (String) -> Unit = {},
    onClearNicknameUpdateError: () -> Unit = {},
    notificationsEnabled: Boolean = false,
    notificationTime: String = "09:00",
    isSavingNotification: Boolean = false,
    notificationError: String? = null,
    onToggleNotifications: (Boolean) -> Unit = {},
    onChangeNotificationTime: (String) -> Unit = {},
    onClearNotificationError: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("다시 시도")
                    }
                }
            }
        }
        else -> {
            MyPageContent(
                nickname = nickname,
                streak = streak,
                maxStreak = maxStreak,
                totalSolved = totalSolved,
                correctRate = correctRate,
                activityGrid = activityGrid,
                appVersion = appVersion,
                isWithdrawing = isWithdrawing,
                onWithdraw = onWithdraw,
                withdrawError = withdrawError,
                onClearWithdrawError = onClearWithdrawError,
                onLogout = onLogout,
                isUpdatingNickname = isUpdatingNickname,
                nicknameUpdateError = nicknameUpdateError,
                onUpdateNickname = onUpdateNickname,
                onClearNicknameUpdateError = onClearNicknameUpdateError,
                notificationsEnabled = notificationsEnabled,
                notificationTime = notificationTime,
                isSavingNotification = isSavingNotification,
                notificationError = notificationError,
                onToggleNotifications = onToggleNotifications,
                onChangeNotificationTime = onChangeNotificationTime,
                onClearNotificationError = onClearNotificationError,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun MyPageContent(
    nickname: String,
    streak: Int,
    maxStreak: Int,
    totalSolved: Int,
    correctRate: Float,
    activityGrid: List<Int>,
    appVersion: String,
    isWithdrawing: Boolean = false,
    onWithdraw: () -> Unit = {},
    withdrawError: String? = null,
    onClearWithdrawError: () -> Unit = {},
    onLogout: () -> Unit = {},
    isUpdatingNickname: Boolean = false,
    nicknameUpdateError: String? = null,
    onUpdateNickname: (String) -> Unit = {},
    onClearNicknameUpdateError: () -> Unit = {},
    notificationsEnabled: Boolean = false,
    notificationTime: String = "09:00",
    isSavingNotification: Boolean = false,
    notificationError: String? = null,
    onToggleNotifications: (Boolean) -> Unit = {},
    onChangeNotificationTime: (String) -> Unit = {},
    onClearNotificationError: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Android 13+ 알림 권한 — 토글을 켤 때 요청하고, 결과와 무관하게 서버 설정은 켠다.
    // (거부해도 설정은 저장되고, 이후 시스템 설정에서 권한을 켜면 바로 알림을 받는다)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onToggleNotifications(true) }
    val context = LocalContext.current
    val requestEnable: () -> Unit = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onToggleNotifications(true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // ── 헤더 ──────────────────────────────────────────────────
        Text(
            text = "마이페이지",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(20.dp))

        // ── 프로필 카드 ───────────────────────────────────────────
        ProfileCard(
            nickname = nickname,
            onEditClick = { showNicknameDialog = true },
        )

        Spacer(Modifier.height(20.dp))

        // ── 통계 카드 ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                label = "연속 학습",
                value = "${streak}일",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "최고 기록",
                value = if (maxStreak > 0) "${maxStreak}일" else "-",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "총 풀이",
                value = "${totalSolved}회",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "정답률",
                value = "${(correctRate * 100).toInt()}%",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── 풀이 활동 카드 ────────────────────────────────────────
        ActivityHeatmapCard(activityGrid = activityGrid)

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        // ── 앱 버전 정보 ──────────────────────────────────────────
        Text(
            text = "앱 정보",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        InfoRow(label = "버전", value = appVersion)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        // ── 알림 ──────────────────────────────────────────────────
        Text(
            text = "알림",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "데일리 퀴즈 알림",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "매일 설정한 시각에 오늘의 퀴즈를 알려드려요",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = notificationsEnabled,
                enabled = !isSavingNotification,
                onCheckedChange = { checked ->
                    if (checked) requestEnable() else onToggleNotifications(false)
                },
            )
        }
        if (notificationsEnabled) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSavingNotification) { showTimePickerDialog = true }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "알림 시각",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = notificationTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        // ── 계정 ──────────────────────────────────────────────────
        Text(
            text = "계정",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "로그아웃",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showWithdrawDialog = true },
            enabled = !isWithdrawing,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Error,
            ),
        ) {
            if (isWithdrawing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Error,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "탈퇴 처리 중...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(
                    text = "회원탈퇴",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "탈퇴 시 풀이 기록과 스트릭, 오답노트가 모두 삭제됩니다.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "로그아웃 하시겠어요?",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "로그아웃하면 다시 로그인해야 해요.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                ) {
                    Text(
                        text = "로그아웃",
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "취소")
                }
            },
        )
    }

    // 탈퇴 확인 다이얼로그
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = {
                Text(
                    text = "정말 탈퇴하시겠어요?",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "탈퇴하면 풀이 기록, 스트릭, 누적 통계가 모두 영구 삭제되고\n복구할 수 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWithdrawDialog = false
                        onWithdraw()
                    },
                ) {
                    Text(
                        text = "탈퇴하기",
                        color = Error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text(text = "취소")
                }
            },
        )
    }

    // 탈퇴 실패 에러 다이얼로그
    if (withdrawError != null) {
        AlertDialog(
            onDismissRequest = onClearWithdrawError,
            title = {
                Text(
                    text = "탈퇴에 실패했어요",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = withdrawError,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onClearWithdrawError) {
                    Text(text = "확인")
                }
            },
        )
    }

    // 닉네임 수정 다이얼로그
    if (showNicknameDialog) {
        var inputNickname by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNicknameDialog = false },
            title = {
                Text(text = "닉네임 변경", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputNickname,
                        onValueChange = { if (it.length <= 20) inputNickname = it },
                        label = { Text("새 닉네임") },
                        placeholder = { Text(nickname) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (inputNickname.isNotBlank()) {
                                onUpdateNickname(inputNickname)
                                showNicknameDialog = false
                            }
                        }),
                        supportingText = { Text("${inputNickname.length}/20") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputNickname.isNotBlank()) {
                            onUpdateNickname(inputNickname)
                            showNicknameDialog = false
                        }
                    },
                    enabled = inputNickname.isNotBlank() && !isUpdatingNickname,
                ) {
                    if (isUpdatingNickname) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(text = "변경", fontWeight = FontWeight.Bold, color = Lime)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showNicknameDialog = false }) {
                    Text(text = "취소")
                }
            },
        )
    }

    // 알림 시각 선택 다이얼로그 (30분 단위 슬롯)
    if (showTimePickerDialog) {
        NotificationTimePickerDialog(
            currentTime = notificationTime,
            onSelect = { time ->
                showTimePickerDialog = false
                if (time != notificationTime) onChangeNotificationTime(time)
            },
            onDismiss = { showTimePickerDialog = false },
        )
    }

    // 알림 설정 실패 에러 다이얼로그
    if (notificationError != null) {
        AlertDialog(
            onDismissRequest = onClearNotificationError,
            title = { Text(text = "알림 설정 실패", fontWeight = FontWeight.Bold) },
            text = { Text(text = notificationError, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = onClearNotificationError) { Text("확인") }
            },
        )
    }

    // 닉네임 수정 실패 에러 다이얼로그
    if (nicknameUpdateError != null) {
        AlertDialog(
            onDismissRequest = onClearNicknameUpdateError,
            title = { Text(text = "닉네임 변경 실패", fontWeight = FontWeight.Bold) },
            text = { Text(text = nicknameUpdateError, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = onClearNicknameUpdateError) { Text("확인") }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 서브 컴포넌트
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    nickname: String,
    onEditClick: () -> Unit,
) {
    val initial = nickname.firstOrNull()?.toString() ?: "?"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Lime),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnLime,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${nickname}님",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "닉네임을 변경할 수 있어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onEditClick) {
                Text(
                    text = "변경",
                    style = MaterialTheme.typography.labelMedium,
                    color = Lime,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// 강도 → 색 변환은 주간(홈) 그리드와 공유한다: ui/theme/StreakColors.kt 의 streakColor()

/**
 * 풀이 활동 카드 — 8주×7일 GitHub 스타일 히트맵.
 *
 * activityGrid: index 0 = 55일 전, index 55 = 오늘 (날짜순 나열).
 * 값: 0=활동 없음, 1=시도했으나 정답 0개, 2=1개 정답, 3=2개 정답, 4=3개 이상 정답.
 *
 * 레이아웃 규칙:
 *  - 마지막 열(week=7) · 오늘 요일 행이 항상 "오늘" 셀.
 *  - daysAgo  = (weeks-1 - week)*7 + (todayDow - day)
 *  - gridIdx  = (totalCells - 1) - daysAgo
 *  - daysAgo < 0  → 미래(이번 주 오늘 이후 요일)
 *  - gridIdx < 0  → 범위 밖(56일 보다 더 과거) → 빈 셀
 */
@Composable
private fun ActivityHeatmapCard(activityGrid: List<Int>) {
    val weeks = 8
    val days = 7
    val totalCells = weeks * days   // 56
    val gap = 4.dp
    val labelWidth = 20.dp
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    // 오늘이 몇 번째 요일 (0=월 ~ 6=일)
    val todayDow = remember {
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    data class CellState(
        val intensity: Int,
        val isToday: Boolean,
        val isFuture: Boolean,
        val inRange: Boolean,
    )

    val cells = remember(activityGrid, todayDow) {
        Array(weeks) { week ->
            Array(days) { day ->
                val daysAgo = (weeks - 1 - week) * 7 + (todayDow - day)
                val gridIdx = (totalCells - 1) - daysAgo
                val isFuture = daysAgo < 0
                val inRange = gridIdx in activityGrid.indices
                CellState(
                    intensity = if (inRange) activityGrid[gridIdx] else 0,
                    isToday = daysAgo == 0,
                    isFuture = isFuture,
                    inRange = inRange,
                )
            }
        }
    }

    // 범례용 강도 단계
    val legendIntensities = listOf(0, 1, 2, 3, 4)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "풀이 활동",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "지난 8주",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(14.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cellGapTotal = (weeks - 1) * gap
                val gridWidth = maxWidth - labelWidth - gap - cellGapTotal
                val cellSize = gridWidth / weeks

                Row(verticalAlignment = Alignment.Top) {
                    // 요일 라벨 열
                    Column(
                        modifier = Modifier.width(labelWidth),
                        verticalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        dayLabels.forEach { label ->
                            Box(
                                modifier = Modifier.size(cellSize),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(gap))

                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        for (week in 0 until weeks) {
                            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                                for (day in 0 until days) {
                                    val cell = cells[week][day]

                                    val bgColor = when {
                                        cell.isFuture && !cell.isToday -> Color.Transparent
                                        else -> streakColor(cell.intensity)
                                    }

                                    // 오늘 셀: 진한 테두리 강조
                                    val borderMod = when {
                                        cell.isToday && cell.intensity == 0 ->
                                            Modifier.border(
                                                width = 2.dp,
                                                color = Lime,
                                                shape = RoundedCornerShape(4.dp),
                                            )
                                        cell.isToday && cell.intensity > 0 ->
                                            Modifier.border(
                                                width = 2.dp,
                                                color = Lime,
                                                shape = RoundedCornerShape(4.dp),
                                            )
                                        else -> Modifier
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(cellSize)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(bgColor)
                                            .then(borderMod),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 오늘 표시 안내
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BgElevated)
                            .border(
                                width = 2.dp,
                                color = Lime,
                                shape = RoundedCornerShape(3.dp),
                            ),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "오늘",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // 활동 강도 범례 (5단계)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "적게",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    legendIntensities.forEach { intensity ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(streakColor(intensity)),
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "많이",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "ⓘ 문제는 1회만 풀 수 있어요.\n오늘 시도에서 맞힌 문제 수에 따라 색이 진해져요.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 알림 시각 선택 다이얼로그 — 백엔드가 30분 단위(HH:00/HH:30)만 허용하므로
 * 자유 입력 타임피커 대신 48개 슬롯 목록에서 고른다.
 */
@Composable
private fun NotificationTimePickerDialog(
    currentTime: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val slots = remember {
        (0 until 24).flatMap { hour ->
            listOf("%02d:00".format(hour), "%02d:30".format(hour))
        }
    }
    val selectedIndex = slots.indexOf(currentTime).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - 2).coerceAtLeast(0),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "알림 시각 선택", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
            ) {
                items(slots) { slot ->
                    val selected = slot == currentTime
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) LimeFaint else Color.Transparent)
                            .clickable { onSelect(slot) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = slot,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Lime else MaterialTheme.colorScheme.onSurface,
                        )
                        if (selected) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Lime,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MyPageScreenPreview() {
    // 강도 0~4 섞어서 테스트
    val activityGrid = List(56) { i ->
        when {
            i % 7 == 0 -> 4
            i % 5 == 0 -> 3
            i % 3 == 0 -> 2
            i % 2 == 0 -> 1
            else       -> 0
        }
    }
    FinQTheme {
        MyPageContent(
            nickname = "유리",
            streak = 7,
            maxStreak = 14,
            totalSolved = 28,
            correctRate = 0.75f,
            activityGrid = activityGrid,
            appVersion = "1.0",
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MyPageEmptyPreview() {
    FinQTheme {
        MyPageContent(
            nickname = "유저123456",
            streak = 0,
            maxStreak = 0,
            totalSolved = 0,
            correctRate = 0f,
            activityGrid = emptyList(),
            appVersion = "1.0",
        )
    }
}
