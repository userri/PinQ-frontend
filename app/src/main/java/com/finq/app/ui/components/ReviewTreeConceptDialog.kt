package com.finq.app.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary

/**
 * 복습 나무 개념 설명 다이얼로그 — 정원 "?" 버튼과 첫 오답 인트로가 공유한다.
 *
 * 특정 화면 위치가 아니라 "복습 나무가 무엇인지"라는 개념만 설명한다 —
 * UI 레이아웃이 바뀌어도 이 문구는 유지보수가 필요 없다.
 */
@Composable
fun ReviewTreeConceptDialog(
    title: String,
    confirmLabel: String = "알겠어요",
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(confirmLabel, color = Lime, fontWeight = FontWeight.SemiBold)
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        },
        text = {
            Column {
                ConceptLine("틀린 문제는 '복습 나무'가 돼요. 복습할 때마다 물을 주고, 충분히 주면 나무로 완성돼요(졸업).")
                Spacer(Modifier.height(10.dp))
                ConceptLine("물은 정해진 날에 복습으로 줄 수 있어요. 간격을 두고 여러 번 만나야 오래 기억에 남아요.")
                Spacer(Modifier.height(10.dp))
                ConceptLine("복습은 스트릭·정답률에 영향을 주지 않아요. 편하게 다시 풀어보세요.")
                Spacer(Modifier.height(10.dp))
                ConceptLine("완성된 나무는 정원에 차곡차곡 쌓여요.")
            }
        },
        containerColor = BgSubtle,
    )
}

@Composable
private fun ConceptLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
    )
}

private const val PREFS_NAME = "finq_intro"
private const val KEY_REVIEW_TREE_INTRO_SEEN = "review_tree_intro_seen"

/** 첫 오답 인트로를 이미 봤는가. */
fun hasSeenReviewTreeIntro(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_REVIEW_TREE_INTRO_SEEN, false)

/** 첫 오답 인트로를 봤다고 기록한다. */
fun markReviewTreeIntroSeen(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_REVIEW_TREE_INTRO_SEEN, true).apply()
}
