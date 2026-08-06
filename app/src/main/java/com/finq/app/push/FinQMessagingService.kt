package com.finq.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.finq.app.MainActivity
import com.finq.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * FCM 수신 서비스.
 *
 * - onNewToken: 토큰이 갱신되면 로그인 상태일 때 백엔드에 재등록한다.
 * - onMessageReceived: 데일리 퀴즈 알림을 표시한다. (포그라운드 수신 시,
 *   그리고 data 메시지일 때 백그라운드 수신 시 호출된다)
 */
class FinQMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Log.i(TAG, "onNewToken — 토큰 갱신됨")
        scope.launch { FcmTokenManager.registerToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // 도착 자체를 남긴다. "푸시가 안 온다"가 ⓐ 서버가 안 보냄 ⓑ 도착했는데
        // 표시에서 막힘 중 어느 쪽인지, 이 한 줄이 있고 없고로 갈린다.
        Log.i(
            TAG,
            "onMessageReceived — notification=${message.notification != null} " +
                "dataKeys=${message.data.keys}",
        )
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "오늘의 경제 퀴즈가 도착했어요!"
        showNotification(this, title, body)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "FinQMessaging"
        const val CHANNEL_ID = "daily_quiz"
        private const val DAILY_QUIZ_NOTIFICATION_ID = 1001

    /**
     * 알림을 만들어 띄운다.
     *
     * companion 으로 뺀 이유: 디버그 Showcase 가 **같은 알림**을 서버 발송 없이 띄울 수
     * 있어야 한다. 작은 아이콘이 상태바에서 어떻게 깎이는지, setColor 가 어디를 칠하는지는
     * 화면으로만 알 수 있는데, 매번 서버 발송 시각을 기다릴 수는 없다. 복사본을 두면
     * 곧 어긋나므로 한 벌만 둔다.
     */
    fun showNotification(context: Context, title: String, body: String) {
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // 작은 아이콘은 알파만 쓰인다 — 색은 아래 setColor 가 맡는다.
            // 종(ic_bell)은 "알림"을 뜻할 뿐 이 앱을 가리키지 않아 새싹으로 바꿨다.
            .setSmallIcon(R.drawable.ic_notification_sprout)
            // 알림 배지·앱 이름 틴트.
            // ⚠️ 라임은 흰 배경 대비가 1.35:1 이라 라이트 모드에서 앱 이름이 흐려질 수
            // 있다(짙은 초록 grass_2 는 5.3:1). 실기기에서 양쪽 모드를 찍어 보고 라임을
            // 유지하기로 했다 — 바꿀 일이 생기면 계산이 아니라 화면을 근거로 바꿀 것.
            .setColor(ContextCompat.getColor(context, R.color.lime))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            // 여기서 조용히 끝나면 "도착은 했는데 화면에 없다"가 되어 원인을 못 찾는다.
            Log.w(TAG, "알림 표시 생략 — OS 레벨에서 앱 알림이 꺼져 있음")
            return
        }
        // POST_NOTIFICATIONS 권한이 런타임에 회수될 수 있으므로 방어적으로 감싼다.
        runCatching { manager.notify(DAILY_QUIZ_NOTIFICATION_ID, notification) }
            .onFailure { Log.w(TAG, "알림 표시 실패", it) }
    }

        /** 알림 채널 생성 (Android 8+, 멱등). */
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "데일리 퀴즈 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "설정한 시각에 오늘의 경제 퀴즈를 알려드려요"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
