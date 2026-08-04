package com.finq.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.finq.app.BuildConfig
import com.finq.app.data.local.hasSeenNotice
import com.finq.app.data.local.markNoticeSeen
import com.finq.app.data.repository.ApiAppConfigRepository
import com.finq.app.data.repository.AppConfig

/**
 * 앱 전역 설정 게이트 — 실행 시 1회 `GET /api/app/config` 를 보고 두 가지를 띄운다.
 *
 *  1. **강제 업데이트**: `versionCode < minVersionCode` → 닫을 수 없는 다이얼로그.
 *     뒤로가기·바깥 탭 모두 막고 출구는 스토어 버튼뿐이다.
 *  2. **공지**: `notice` 가 있으면 닫을 수 있는 다이얼로그 1회.
 *     같은 문구는 다시 뜨지 않는다(해시 저장, [hasSeenNotice]).
 *
 * 조회 실패는 **조용히 통과**한다 — 근거 없이 앱을 막으면 서버 장애가 곧
 * "앱이 안 켜짐"이 된다. 판단 근거가 없을 땐 막지 않는 쪽이 안전하다.
 *
 * 두 다이얼로그는 ShowcaseActivity 가 직접 렌더할 수 있게 internal 이다 —
 * 실서버가 min=1 이라 강제 업데이트 화면은 네트워크로는 재현되지 않는다.
 *
 * 화면을 가리는 대신 [content] 위에 얹는다. 강제 업데이트 상태에서도 뒤의 화면은
 * 그려지지만 다이얼로그를 닫을 수 없으므로 조작은 불가능하다.
 */
@Composable
fun AppConfigGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var config by remember { mutableStateOf<AppConfig?>(null) }
    var noticeToShow by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val loaded = ApiAppConfigRepository().getAppConfig() ?: return@LaunchedEffect
        config = loaded
        val notice = loaded.notice
        if (notice != null && !hasSeenNotice(context, notice)) {
            noticeToShow = notice
        }
    }

    content()

    val blocking = config?.let { BuildConfig.VERSION_CODE < it.minVersionCode } == true
    if (blocking) {
        ForcedUpdateDialog(storeUrl = config?.storeUrl, context = context)
    } else {
        noticeToShow?.let { notice ->
            NoticeDialog(
                notice = notice,
                onDismiss = {
                    markNoticeSeen(context, notice)
                    noticeToShow = null
                },
            )
        }
    }
}

/**
 * 닫을 수 없는 업데이트 안내. `dismissOnBackPress/ClickOutside = false` 로
 * 출구를 스토어 버튼 하나로 좁힌다 — 이 상태의 앱은 서버와 계약이 어긋나 있다.
 */
@Composable
internal fun ForcedUpdateDialog(storeUrl: String?, context: Context) {
    AlertDialog(
        onDismissRequest = { /* 닫히지 않는다 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Text(text = "업데이트가 필요해요", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = "새 버전에서 이어서 쓸 수 있어요. 스토어에서 업데이트해 주세요.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            if (storeUrl != null) {
                TextButton(onClick = { openStore(context, storeUrl) }) {
                    Text(text = "스토어로 이동", fontWeight = FontWeight.Bold)
                }
            }
        },
    )
}

@Composable
internal fun NoticeDialog(notice: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "안내", fontWeight = FontWeight.Bold) },
        text = { Text(text = notice, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "확인", fontWeight = FontWeight.Bold)
            }
        },
    )
}

/** 스토어 앱이 없는 기기도 있으므로 실패해도 앱이 죽지 않게 감싼다. */
private fun openStore(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
