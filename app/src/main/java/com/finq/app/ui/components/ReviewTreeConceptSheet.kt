package com.finq.app.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** 같은 시트를 쓰되 히어로만 성격에 맞게 갈아 끼운다. */
enum class ReviewTreeConceptVariant {
    /** 정원 "?" — 언제든 다시 열어보는 참조. 4단계 타임라인 전체를 펼친다. */
    REFERENCE,

    /** 첫 오답 직후 1회 — 방금 태어난 새싹을 축하하고 앞으로를 예고한다. */
    CELEBRATION,
}

/** 시트 전체의 답이 되는 한 줄. WaterGrassCard 의 "3번 맞히면" 과 어휘를 맞춘다. */
private const val CONCEPT_SUMMARY = "틀린 문제가 나무가 될 때까지 세 번 물을 주는 거예요."

private data class ConceptPoint(
    @DrawableRes val iconRes: Int,
    val title: String,
    val body: String,
)

/** 제목 3개만 훑어도 요지가 잡히도록 쓴다 — 일정 규칙·불안 해소·보상 순. */
private val ConceptPoints = listOf(
    ConceptPoint(
        R.drawable.ic_water_drop,
        "물은 정해진 날에만",
        "간격을 두고 다시 만나야 오래 기억에 남아요",
    ),
    ConceptPoint(
        R.drawable.ic_shield_check,
        "틀려도 안전해요",
        "복습은 스트릭·정답률에 영향을 주지 않아요",
    ),
    ConceptPoint(
        R.drawable.ic_stage_tree,
        "완성한 나무는 정원으로",
        "차곡차곡 쌓여요",
    ),
)

/**
 * 복습 나무 개념 설명 시트 — 정원 "?" 버튼과 첫 오답 인트로가 공유한다.
 *
 * 특정 화면 위치가 아니라 "복습 나무가 무엇인지"라는 개념만 설명한다 —
 * UI 레이아웃이 바뀌어도 이 문구는 유지보수가 필요 없다.
 *
 * 다이얼로그가 아니라 바텀시트인 이유: M3 다이얼로그는 좌우 인셋이 있어
 * 4노드 가로 타임라인이 눌린다. 시트는 3변이 화면에 붙어 폭을 온전히 쓴다.
 * 드래그 핸들만으로 닫기를 유도하지 않도록 상단에 명시적 닫기(X)를 둔다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewTreeConceptSheet(
    title: String,
    confirmLabel: String = "알겠어요",
    variant: ReviewTreeConceptVariant = ReviewTreeConceptVariant.REFERENCE,
    onDismiss: () -> Unit,
) {
    // 설명 시트는 한 번에 다 보여야 한다 — 반만 열어 CTA 를 접어두면 드래그를 강요한다.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // 닫기(X)·CTA 는 내려가는 애니메이션을 마친 뒤에 호출부 상태를 정리한다.
    // 스크림 탭·뒤로가기는 시트가 이미 내려간 뒤 onDismissRequest 로 들어온다.
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // 다이얼로그·시트용 토큰. BgSubtle 은 눌림/선택 상태 전용이라 여기 쓰면 안 된다.
        containerColor = BgElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Outline) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            // 명시적 닫기 — 드래그 핸들은 제스처가 모호해 단독 닫기 수단이 될 수 없다.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = close) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "닫기",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = CONCEPT_SUMMARY,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
            )

            Spacer(Modifier.height(24.dp))
            when (variant) {
                ReviewTreeConceptVariant.REFERENCE -> ReviewStageTimeline()
                ReviewTreeConceptVariant.CELEBRATION -> ReviewStageBirthHero()
            }
            Spacer(Modifier.height(24.dp))

            HorizontalDivider(color = Outline)
            Spacer(Modifier.height(18.dp))

            ConceptPoints.forEachIndexed { i, point ->
                if (i > 0) Spacer(Modifier.height(16.dp))
                ConceptPointRow(point)
            }

            Spacer(Modifier.height(26.dp))
            Button(
                onClick = close,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lime,
                    contentColor = OnLime,
                ),
            ) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ConceptPointRow(point: ConceptPoint) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        // 설명이 2줄로 넘어가도 아이콘은 제목 첫 줄에 붙어 있어야 한다 —
        // 블록 전체 세로 중앙에 맞추면 제목과 설명 사이 허공에 뜬다.
        ScaledVectorIcon(
            res = point.iconRes,
            size = 24.dp,
            modifier = Modifier.padding(top = 2.dp),
            tint = Lime,
        )
        Spacer(Modifier.size(12.dp))
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = point.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = point.body,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
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
