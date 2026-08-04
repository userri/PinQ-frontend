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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ButtonDefaults
import com.finq.app.R
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.data.repository.ConceptStats
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.ui.components.ConceptStatsSection
import com.finq.app.ui.components.garden.GardenSection
import com.finq.app.ui.components.garden.TreeRecordBlock
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import kotlin.math.roundToInt

/**
 * 마이페이지 — Stateless View.
 *
 * 연속/최고 스트릭은 일부러 받지 않는다. 통계 API 와 잔디밭 API 가 같은 개념을
 * 각각 들고 오던 탓에 화면에 같은 값이 두 벌 떠 있었고 서로 어긋날 수도 있었다.
 * 지금은 [grass] 만 그 값의 소유자다.
 *
 * @param totalSolved     누적 풀이 수 — 프로필 헤더가 소유
 * @param correctRate     정답률 0.0~1.0 — 프로필 헤더가 소유
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
    totalSolved: Int,
    correctRate: Float,
    /** 연간 잔디밭. null 이면 아직 로딩 전 — 스켈레톤을 그린다. */
    grass: GrassCalendar? = null,
    /** 잔디밭 첫 로드 실패 — 재시도 카드를 그린다. */
    grassFailed: Boolean = false,
    onRetryGrass: () -> Unit = {},
    onOpenGarden: () -> Unit = {},
    /** 정원(자라는 새싹/나무). null 이면 캔버스 자리표시. */
    garden: ReviewGarden? = null,
    /** 개념별 정답률. null 이거나 카테고리가 비면 섹션을 숨긴다. */
    conceptStats: ConceptStats? = null,
    appVersion: String,
    /** 온보딩 3장 다시 보기 — 앱 정보 줄과 같은 부수 정보 톤으로 둔다. */
    onOpenOnboarding: () -> Unit = {},
    /** 의견 보내기 — 외부 폼을 연다. 홈 배너를 닫아도 남는 상시 창구. */
    onOpenFeedback: () -> Unit = {},
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
                totalSolved = totalSolved,
                correctRate = correctRate,
                grass = grass,
                grassFailed = grassFailed,
                onRetryGrass = onRetryGrass,
                onOpenGarden = onOpenGarden,
                garden = garden,
                conceptStats = conceptStats,
                appVersion = appVersion,
                onOpenOnboarding = onOpenOnboarding,
                onOpenFeedback = onOpenFeedback,
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
    totalSolved: Int,
    correctRate: Float,
    /** 연간 잔디밭. null 이면 아직 로딩 전 — 스켈레톤을 그린다. */
    grass: GrassCalendar? = null,
    grassFailed: Boolean = false,
    onRetryGrass: () -> Unit = {},
    onOpenGarden: () -> Unit = {},
    /** 정원(자라는 새싹/나무). null 이면 캔버스 자리표시. */
    garden: ReviewGarden? = null,
    /** 개념별 정답률. null 이거나 카테고리가 비면 섹션을 숨긴다. */
    conceptStats: ConceptStats? = null,
    appVersion: String,
    /** 온보딩 3장 다시 보기. */
    onOpenOnboarding: () -> Unit = {},
    /** 의견 보내기 — 외부 폼을 연다. */
    onOpenFeedback: () -> Unit = {},
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

        Spacer(Modifier.height(24.dp))

        // ── 프로필 헤더 ───────────────────────────────────────────
        // 총 풀이·정답률은 여기서만 말한다. 연속/최고는 잔디밭 섹션이 단독으로 소유한다.
        ProfileHeader(
            nickname = nickname,
            totalSolved = totalSolved,
            correctRate = correctRate,
            onEditClick = { showNicknameDialog = true },
        )

        SectionDivider()

        // ── 잔디밭 (연간) ─────────────────────────────────────────
        // fetch 완료 전에는 스켈레톤 — 옛 데이터(구 8주 그리드)를 첫 프레임에 그리지 않는다.
        // 재진입은 VM 이 SWR 로 이전 grass 를 유지하므로 여기선 이전 값이 그대로 보인다.
        GardenSection(
            grass = grass,
            grassFailed = grassFailed,
            garden = garden,
            onRetryGrass = onRetryGrass,
            onOpenGarden = onOpenGarden,
        )

        // ── 복습 나무 ─────────────────────────────────────────────
        // 잔디밭의 하위가 아니라 동급 섹션이다 — 잔디(일일 활동)와 나무(복습)는
        // 도메인상 다른 축이고, 남의 섹션 안에 넣으면 구분하려고 박스를 쳐야 해서
        // 페이지에서 혼자 카드가 된다.
        if (grass != null) {
            SectionDivider()
            TreeRecordBlock(
                // 카운터가 진실 — 정원 목록이 아직 없어도 이 숫자는 바로 그릴 수 있다.
                graduatedTrees = grass.graduatedTrees,
                garden = garden,
                onOpenGarden = onOpenGarden,
            )
        }

        // ── 개념별 정답률 / 취약 개념 ─────────────────────────────
        // 표본이 아예 없으면(카테고리 비었음) 섹션 자체를 숨긴다.
        if (conceptStats != null && conceptStats.categories.isNotEmpty()) {
            SectionDivider()
            ConceptStatsSection(stats = conceptStats)
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        // ── 앱 버전 정보 ──────────────────────────────────────────
        Text(
            text = "앱 정보",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
        )
        Spacer(Modifier.height(8.dp))
        InfoRow(label = "버전", value = appVersion)
        // 온보딩 재열람 진입점. 값 대신 셰브론이 들어간 것 말고는 위 줄과 구조가 같다 —
        // 화면 위계를 흔들지 않도록 부수 정보 톤을 유지한다.
        NavRow(label = "앱 소개 다시 보기", onClick = onOpenOnboarding)
        // 의견 창구 — 홈 배너는 한 번 닫으면 끝이라 상시 입구가 여기 남아야 한다.
        // 종이비행기 아이콘은 홈 배너에서만 쓴다. 이 목록은 라벨+셰브론 리듬이고
        // "의견 보내기"라는 말이 이미 아이콘이 할 말을 다 한다.
        NavRow(label = "의견 보내기", onClick = onOpenFeedback)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        // ── 알림 ──────────────────────────────────────────────────
        Text(
            text = "알림",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
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
            color = TextMuted,
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

/**
 * 프로필 헤더 — 정체성(아바타·이름)과 누적 성취(정답률·풀이 수)를 한 줄로 읽는 자리.
 *
 * 예전엔 카드 안에 「아바타 + 이름 + "닉네임을 변경할 수 있어요" + [변경]」 뿐이었다.
 * 페이지 최상단이라는 가장 좋은 자리를 저가치 액션 안내가 차지하고 있었던 셈이라,
 * 이름 변경은 조용한 어포던스(이름 옆 `›` + 영역 탭)로 내리고 성취 요약을 올렸다.
 *
 * 이 화면에서 라임을 크게 쓰는 곳은 여기 정답률 하나뿐이다 — 잔디밭 통계는 전부 중립.
 * (아래 [TreeRecordBlock] 의 라임은 별도 톤 밴드 안이라 구역이 다르다.)
 *
 * 아직 한 문제도 안 푼 사용자에겐 큰 `0%` 를 띄우지 않는다. 음수 정보를 주역으로
 * 세우는 대신 다음 행동을 말한다 — [TreeRecordBlock] 의 0그루 처리와 같은 규칙.
 */
@Composable
private fun ProfileHeader(
    nickname: String,
    totalSolved: Int,
    correctRate: Float,
    onEditClick: () -> Unit,
) {
    val initial = nickname.firstOrNull()?.toString() ?: "?"
    val hasRecord = totalSolved > 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                // 어포던스는 작지만 탭 영역은 아바타~이름 전체다.
                .clickable(onClickLabel = "닉네임 변경", onClick = onEditClick)
                .heightIn(min = 48.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${nickname}님",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        // 드로어블 기본색이 text_primary 라 tint 생략 시 라벨 색과 어긋난다.
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = if (hasRecord) "${totalSolved}문제 풀이" else "첫 문제를 풀어보세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
        }

        if (hasRecord) {
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${(correctRate * 100).roundToInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Lime,
                )
                Text(
                    text = "정답률",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
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
                            .background(if (selected) BgSubtle else Color.Transparent)
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

/**
 * 섹션 사이 구분 — 카드 테두리 대신 여백+선으로만 나눈다.
 * 페이지 하단(앱 정보·알림·계정)이 원래 쓰던 리듬을 상단에도 그대로 적용한 것.
 */
@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(20.dp))
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

/**
 * [InfoRow] 와 같은 줄에 값 대신 셰브론이 들어간 형태 — 누르면 다른 화면으로 간다.
 * 배경·테두리를 두르지 않는다(면을 늘리지 않고 타이포·여백으로만 구분).
 */
@Composable
private fun NavRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MyPageScreenPreview() {
    FinQTheme {
        MyPageContent(
            nickname = "유리",
            totalSolved = 28,
            correctRate = 0.75f,
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
            totalSolved = 0,
            correctRate = 0f,
            appVersion = "1.0",
        )
    }
}
