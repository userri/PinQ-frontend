package com.finq.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        scope.launch { FcmTokenManager.registerToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "오늘의 경제 퀴즈가 도착했어요!"
        showNotification(title, body)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun showNotification(title: String, body: String) {
        ensureChannel(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = NotificationManagerCompat.from(this)
        if (manager.areNotificationsEnabled()) {
            // POST_NOTIFICATIONS 권한이 런타임에 회수될 수 있으므로 방어적으로 감싼다.
            runCatching {
                manager.notify(DAILY_QUIZ_NOTIFICATION_ID, notification)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_quiz"
        private const val DAILY_QUIZ_NOTIFICATION_ID = 1001

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
